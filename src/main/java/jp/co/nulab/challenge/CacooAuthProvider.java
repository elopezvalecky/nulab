package jp.co.nulab.challenge;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Session;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public interface CacooAuthProvider extends AuthProvider {

	/**
	 * Create a OAuth2 auth provider
	 *
	 * @param vertx the Vertx instance
	 * @return the auth provider
	 */
	static CacooAuthProvider create(Vertx vertx, String siteUrl, String consumerKey, String consumerSecret) {
		return new CacooAuthProviderImpl(vertx, siteUrl, consumerKey, consumerSecret);
	}

	void getToken(Session session, JsonObject params, Handler<AsyncResult<OAuthToken>> handler);
	
	String authorizeURL(Session session, JsonObject params) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException;

	CacooAuthProvider api(HttpMethod method, String path, JsonObject params, OAuthToken token, Handler<AsyncResult<JsonObject>> handler);

}