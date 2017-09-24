package jp.co.nulab.challenge;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Session;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class CacooAuthProviderImpl implements CacooAuthProvider {
	
	private static String OAUTH_CONSUMER_SESSION_KEY = "OAuthConsumer";

	private final OAuthProvider provider;

	private final Vertx vertx;
	private final String consumerKey;
	private final String consumerSecret;
	private final String siteUrl;

	public CacooAuthProviderImpl(Vertx vertx, String siteUrl, String consumerKey, String consumerSecret) {
		this.vertx = vertx;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.siteUrl = siteUrl;
		
        provider = new DefaultOAuthProvider(siteUrl+"/oauth/request_token", siteUrl+"/oauth/access_token", siteUrl+"/oauth/authorize");		
	}

	@Override
	public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
		resultHandler.handle(Future.failedFuture("OAuth2 cannot be used for AuthN (the implementation is a Client Relay only)"));
	}

	@Override
	public String authorizeURL(Session session, JsonObject params) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
		final String callbackUrl = params.getString("callbackUrl", OAuth.OUT_OF_BAND);

		final OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
		session.put(OAUTH_CONSUMER_SESSION_KEY, consumer);
		return provider.retrieveRequestToken(consumer, callbackUrl);
	}

	@Override
	public void getToken(Session session, JsonObject params, Handler<AsyncResult<OAuthToken>> handler) {
		final String verifier = params.getString("verifier");
		try {
			final OAuthConsumer userConsumer = session.get(OAUTH_CONSUMER_SESSION_KEY);
			provider.retrieveAccessToken(userConsumer, verifier);
			session.remove(OAUTH_CONSUMER_SESSION_KEY);			

			final OAuthToken token = new OAuthToken(userConsumer.getToken(), userConsumer.getTokenSecret());
			handler.handle(Future.succeededFuture(token));
		} catch (Exception e) {
			handler.handle(Future.failedFuture(e));
		}
		
	}

	@Override
	public CacooAuthProvider api(HttpMethod method, String path, JsonObject params, OAuthToken token, Handler<AsyncResult<JsonObject>> handler) {
		final OAuthConsumer userConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		userConsumer.setTokenWithSecret(token.getAccessToken(), token.getTokenSecret());
		
		vertx.executeBlocking(future -> {
			try {
				final HttpRequestBase request;
				switch(method) {
					case GET: {
						request = new HttpGet(siteUrl+path);
						break;
					}
//					case POST: {
//						request = new HttpPost(siteUrl+path);
//						break;
//					}
//					case DELETE: {
//						request = new HttpDelete(siteUrl+path);
//						break;
//					}
//					case PUT: {
//						request = new HttpPut(siteUrl+path);
//						break;
//					}
					default: {
						future.fail("Unsupported http method");
						return;
					}
				} 
				
				userConsumer.sign(request);
				final HttpClient client = new DefaultHttpClient();
				final HttpResponse response = client.execute(request);
				
				final JsonObject json = Buffer.buffer(EntityUtils.toByteArray(response.getEntity())).toJsonObject();
				future.complete(json);
				
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);

		return this;
	}

}