package jp.co.nulab.challenge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class HttpServerVerticle extends AbstractVerticle {
	
	final static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

	final static int PORT = 8080;
	
	CacooAuthProvider cacooProvider;
	
	@Override
	public void start() throws Exception {
		super.start();
		
		final int port = config().getInteger("port", PORT);
		final String callbackUrl = config().getString("callbackUrl", "http://localhost:"+port);
		
		final HttpServer server = vertx.createHttpServer();
		final Router router = Router.router(vertx);
		
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

		cacooProvider = CacooAuthProvider.create(vertx, "https://cacoo.com", "NaLagAyazESxDrGYoWTgAY", "EldqqYqtMOolcBVTIEvLAUNnqCjerFrOsawWPGozyC");
		final CacooAuthHandler cacooAuthHandler = CacooAuthHandler.create(cacooProvider, callbackUrl).setupCallback(router.route("/oauth/callback"));

		router.route("/").handler(this::index);
		router.route().handler(cacooAuthHandler);
		router.route("/diagrams").handler(this::diagrams);
		
		server
			.requestHandler(router::accept)
			.listen(port, result -> {
				logger.info("Http Server running on port "+port);
			});
	}

	private void index(RoutingContext ctx) {
		ctx.response().end("Welcome");
	}

	private void diagrams(RoutingContext ctx) {
		CacooUser user = (CacooUser) ctx.user();
		
		user.getDiagrams(null, result -> {
			ctx.response().end(result.result().toString());
		});
	}
	
	
}
