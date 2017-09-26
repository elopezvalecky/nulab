package jp.co.nulab.challenge;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.utils.URIUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
		
		final String consumerKey = config().getString("consumer.key");
		final String consumerSecret = config().getString("consumer.secret");
		
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

		final CacooAuthProvider cacooProvider = CacooAuthProvider.create(vertx, "https://cacoo.com", consumerKey, consumerSecret);
		final CacooAuthHandler cacooAuthHandler = CacooAuthHandler.create(cacooProvider, callbackUrl).setupCallback(router.route("/oauth/callback"));
		engine = HandlebarsTemplateEngine.create();

		router.route().handler(cacooAuthHandler);
		router.get("/diagrams").handler(this::listDiagrams);
		router.get("/diagrams/:diagramId").handler(this::showDiagram);
		router.get("/diagrams/:diagramId/edit").handler(this::editDiagram);
		router.get("/diagrams/:diagramId/move").handler(this::moveDiagram);
		router.get("/diagrams/:diagramId/copy").handler(this::copyDiagram);
		router.delete("/diagrams/:diagramId").handler(this::deleteDiagram);
		
		server
			.requestHandler(router::accept)
			.listen(port, result -> {
				logger.info("Http Server running on port "+port);
			});
	}

	// Home page
	private void index(RoutingContext ctx) {
		engine.render(ctx, "templates", "/index", template -> {
			if (template.succeeded()) 
				ctx.response().end(template.result());
            else 
            	ctx.fail(template.cause());				
		});
	}

	// Diagrams page
	private void listDiagrams(RoutingContext ctx) {
		final CacooAPI api = (CacooAPI) ctx.user();
		
		final MultiMap params = ctx.request().params();
		
		final Integer offset = params.contains("offset") ? Integer.parseInt(params.get("offset")) : 0;
		final Integer limit = params.contains("limit") ? Integer.parseInt(params.get("limit")) : 10;
		final String type = params.contains("type") ? params.get("type") : "all";
		final String sortOn = params.get("sortOn");
		final String sortType = params.get("sortType");
		final Integer folderId = params.contains("folderId") ? Integer.parseInt(params.get("folderId")) : null;
		
		final String typeKey = type.substring(0, 1).toUpperCase() + type.substring(1); 
		ctx.put("is"+typeKey, true);
		
		api.getDiagrams(offset, limit, type, sortOn, sortType, folderId, result -> {
			ctx.put("diagrams", result.result());
			
			engine.render(ctx, "templates", "/diagrams", template -> {
				if (template.succeeded()) 
					ctx.response().end(template.result());
                else 
                	ctx.fail(template.cause());				
			});
		});
	}
	
	// Diagram Details page
	private void showDiagram(RoutingContext ctx) {}
	
	// Diagram edit action
	private void editDiagram(RoutingContext ctx) {
		try {
			final CacooAPI api = (CacooAPI) ctx.user();
			final String id = ctx.request().getParam("diagramId");
	
			final URI currentUrl = new URI(ctx.request().absoluteURI());
			final String callbackUrl = URIUtils.createURI(currentUrl.getScheme(), currentUrl.getHost(), currentUrl.getPort(), "/diagrams", null, null).toString();
			
			final JsonObject params = new JsonObject()
					.put("callbackUrl", callbackUrl)
					.put("buttons", new JsonArray()
							.add(new JsonObject()
									.put("label", "Normal")
									.put("callbackParam", "N")
									.put("action", "normal"))
							.add(new JsonObject()
									.put("label", "Exit")
									.put("callbackParam", "E")
									.put("action", "exit"))
							.add(new JsonObject()
									.put("label", "Save & Exit")
									.put("callbackParam", "SE")
									.put("action", "saveAndExit")));
			
			api.editorToken(id, result -> {
				if (result.succeeded()) {
					try {
						final StringBuilder editorUrl = new StringBuilder();
						editorUrl.append("https://cacoo.com/diagrams/")
							.append(id)
							.append("/edit?editorToken=")
							.append(result.result())
							.append("&parameter=")
							.append(URLEncoder.encode(params.toString(), StandardCharsets.UTF_8.name()));
	
						ctx.response()
							.setStatusCode(302)
							.putHeader(HttpHeaders.LOCATION, editorUrl.toString())
							.end("Redirecting to Editor");
					} catch (Exception e) {
						ctx.fail(e);
					}
				} else
					ctx.fail(result.cause());
			});
		} catch (Exception e) {
			ctx.fail(e);
		}
	}
	
	// Diagram delete action
	private void deleteDiagram(RoutingContext ctx) {}
	
	// Diagram move action
	private void moveDiagram(RoutingContext ctx) {}
	
	// Diagram copy action
	private void copyDiagram(RoutingContext ctx) {
		final CacooAPI api = (CacooAPI) ctx.user();
		final String id = ctx.request().getParam("diagramId");
		
		api.getDiagram(id, result -> {
			if (result.succeeded()) {
				final JsonObject original = result.result();
				
				api.copyDiagram(id, original.getInteger("folderId"), original.getString("title")+" (Copy)", original.getString("description"), original.getString("security"), result2 -> {
					if (result2.succeeded())
						ctx.response()
							.setStatusCode(302)
							.putHeader(HttpHeaders.LOCATION, "/diagrams")
							.end();
					else
						ctx.fail(result2.cause());
				});
			} else
				ctx.fail(result.cause());
		});
	}
	
}
