package jp.co.nulab.challenge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;

public class HttpServerVerticle extends AbstractVerticle {
	
	final static Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

	final static int PORT = 8080;
	
	TemplateEngine engine;
	
	@Override
	public void start() throws Exception {
		super.start();
		
		final int port = config().getInteger("port", PORT);
		final String callbackUrl = config().getString("callbackUrl", "http://localhost:"+port);
		
		final HttpServer server = vertx.createHttpServer();
		final Router router = Router.router(vertx);
		
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));
		router.route().handler(BodyHandler.create().setMergeFormAttributes(true));
		router.route().handler(ResponseTimeHandler.create());

		router.get("/").handler(this::index);
		router.get("/assets/*").handler(StaticHandler.create().setDirectoryListing(true));

		final CacooAuthProvider cacooProvider = CacooAuthProvider.create(vertx, "https://cacoo.com", "NaLagAyazESxDrGYoWTgAY", "EldqqYqtMOolcBVTIEvLAUNnqCjerFrOsawWPGozyC");
		final CacooAuthHandler cacooAuthHandler = CacooAuthHandler.create(cacooProvider, callbackUrl).setupCallback(router.route("/oauth/callback"));
		engine = HandlebarsTemplateEngine.create();

		router.route().handler(cacooAuthHandler);
		router.get("/diagrams").handler(this::listDiagrams);
		
		server
			.requestHandler(router::accept)
			.listen(port, result -> {
				logger.info("Http Server running on port "+port);
			});
	}

	private void index(RoutingContext ctx) {
		engine.render(ctx, "templates", "/index", template -> {
			if (template.succeeded()) 
				ctx.response().end(template.result());
            else 
            	ctx.fail(template.cause());				
		});
	}

	private void listDiagrams(RoutingContext ctx) {
		CacooUser user = (CacooUser) ctx.user();
		
		final MultiMap params = ctx.request().params();
		
		final Integer offset = params.contains("offset") ? Integer.parseInt(params.get("offset")) : 0;
		final Integer limit = params.contains("limit") ? Integer.parseInt(params.get("limit")) : 10;
		final String type = params.get("type");
		final String sortOn = params.get("sortOn");
		final String sortType = params.get("sortType");
		final Integer folderId = params.contains("folderId") ? Integer.parseInt(params.get("folderId")) : null;
		
		user.getDiagrams(offset, limit, type, sortOn, sortType, folderId, result -> {
			ctx.put("diagrams", result.result());
			
			engine.render(ctx, "templates", "/diagrams", template -> {
				if (template.succeeded()) 
					ctx.response().end(template.result());
                else 
                	ctx.fail(template.cause());				
			});
		});
	}
	
	
}
