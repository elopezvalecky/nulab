package jp.co.nulab.challenge;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface CacooAPI {

	void getDiagrams(JsonObject params, Handler<AsyncResult<JsonObject>> handler);
	
}
