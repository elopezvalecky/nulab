package jp.co.nulab.challenge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

	final static Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	
	@Override
	public void start() throws Exception {
		super.start();
		logger.info("Starting server.");
		
		final DeploymentOptions opts = new DeploymentOptions();
		opts.setConfig(config().getJsonObject("http", new JsonObject()));
		
		vertx.deployVerticle(new HttpServerVerticle(), opts, result -> {});
		
	}
}
