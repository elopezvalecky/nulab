package jp.co.nulab.challenge;

import java.net.MalformedURLException;
import java.net.URL;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class CacooAuthHandlerImpl extends AuthHandlerImpl implements CacooAuthHandler {
	
	private static String TOKEN = "token";

	private static Logger logger = LoggerFactory.getLogger(CacooAuthHandlerImpl.class);
	
	private final String host;
	private final String callbackPath;

	private JsonObject extraParams = new JsonObject();

	private Route callback;

	public CacooAuthHandlerImpl(CacooAuthProvider authProvider, String callbackURL) {
		super(authProvider);
		try {
			final URL url = new URL(callbackURL);
			this.host = url.getProtocol() + "://" + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort());
			this.callbackPath = url.getPath();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handle(RoutingContext ctx) {
		User user = ctx.user();
		if (user != null) {
			// Already authenticated.
			ctx.next();
		} else {
			Session session = ctx.session();
			if (session != null) {
				session.regenerateId();
				OAuthToken token = session.get(TOKEN);
				if (token != null) {
					((CacooAuthProvider) authProvider).api(HttpMethod.GET, "/api/v1/account.json", new JsonObject(), token, result -> {
		                if (result.failed()) {
		                    ctx.response().end(result.cause().getMessage());
		                    ctx.fail(401);
		                    return;
		                }
		                ctx.setUser(new CacooUser((CacooAuthProvider) authProvider, result.result(), token));
		                // continue
		                ctx.next();
					});
					return;
				}
			}
			
			// redirect request to the oauth server
			try {
				ctx.response()
					.putHeader("Location", authURI(session, host, ctx.normalisedPath()))
					.setStatusCode(302)
					.end();
			} catch (Exception e) {
				logger.error("Something went wrong.", e);
				ctx.fail(e);
			}
		}
	}

	private String authURI(Session session, String host, String redirectURL) throws OAuthMessageSignerException,
			OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
		if (callback == null) {
			throw new NullPointerException("callback is null");
		}

		final JsonObject params = new JsonObject().put("callbackUrl", host + callback.getPath());
		return ((CacooAuthProvider) authProvider).authorizeURL(session, params);
	}

	@Override
	public CacooAuthHandler setupCallback(Route route) {
		callback = route;

		if (!"".equals(callbackPath)) {
			callback.path(callbackPath);
		}
		callback.method(HttpMethod.GET);

		// Handle the callback of the flow
		route.handler(ctx -> {
			final String oauthToken = ctx.request().getParam("oauth_token");
			
			// code is a require value
			if (oauthToken == null) {
				ctx.fail(400);
				return;
			}

			final String oauthVerifier = ctx.request().getParam("oauth_verifier");
			
			final JsonObject params = new JsonObject()
					.put("token", oauthToken)
					.put("verifier", oauthVerifier);

			Session session = ctx.session();
			if (session != null) {
				session.regenerateId();

				((CacooAuthProvider) authProvider).getToken(session, params.mergeIn(extraParams), res -> {
					if (res.failed()) {
						ctx.fail(res.cause());
					} else {
						OAuthToken token = res.result();
						session.put(TOKEN, token);
						ctx.response()
							// disable all caching
							.putHeader("Cache-Control", "no-cache, no-store, must-revalidate")
							.putHeader("Pragma", "no-cache").putHeader("Expires", "0")
							// redirect
							.putHeader("Location", "/").setStatusCode(302)
							.end("Redirecting to /.");
						
					}
				});				
			} else {
				// there is no session object so we cannot keep state
				ctx.reroute("/");
			}
		});

		return this;
	}

}
