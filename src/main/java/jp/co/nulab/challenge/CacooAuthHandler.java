package jp.co.nulab.challenge;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.AuthHandler;

public interface CacooAuthHandler extends AuthHandler {

	/**
	 * Create a OAuth2 auth handler with host pinning
	 *
	 * @param authProvider the auth provider to use
	 * @param callbackURL the callback URL you entered in your provider admin console, usually it should be something like: `https://myserver:8888/callback`
	 * @return the auth handler
	 */
	static CacooAuthHandler create(CacooAuthProvider authProvider, String callbackURL) {
		return new CacooAuthHandlerImpl(authProvider, callbackURL);
	}

	/**
	 * add the callback handler to a given route.
	 * 
	 * @param route a given route e.g.: `/callback`
	 * @return self
	 */
	CacooAuthHandler setupCallback(Route route);

}
