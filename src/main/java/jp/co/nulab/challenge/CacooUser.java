package jp.co.nulab.challenge;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class CacooUser implements User, CacooAPI {

	final JsonObject principal;
	final OAuthToken token;

	CacooAuthProvider provider;

	public CacooUser(CacooAuthProvider provider, JsonObject principal, OAuthToken token) {
		this.provider = provider;
		this.principal = principal;
		this.token = token;
	}
	
	@Override
	public User isAuthorised(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User clearCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject principal() {
		return principal;
	}

	@Override
	public void setAuthProvider(AuthProvider authProvider) {
		this.provider = (CacooAuthProvider) authProvider;
	}

	@Override
	public void getDiagrams(JsonObject params, Handler<AsyncResult<JsonObject>> handler) {
		provider.api(HttpMethod.GET, "/api/v1/diagrams.json", params, token, handler);
	}

}
