package jp.co.nulab.challenge;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface CacooAPI {

	void getDiagrams(Integer offset, Integer limit, String type, String sortOn, String sortType, Integer folderId, Handler<AsyncResult<JsonObject>> handler);
	
	void getDiagram(String id, Handler<AsyncResult<JsonObject>> handler);
	
	void getFolders(Handler<AsyncResult<JsonObject>> handler);
	
	void createDiagram(Integer folderId, String title, String description, String security, Handler<AsyncResult<JsonObject>> handler);
	
	void moveDiagram(Integer folderId, Handler<AsyncResult<JsonObject>> handler);

	void copyDiagram(String originalDigramId, Integer folderId,String title, String description, String security, Handler<AsyncResult<JsonObject>> handler);
	
	void deleteDiagram(String id, Handler<AsyncResult<JsonObject>> handler);

	void editDiagram(String id, Handler<AsyncResult<JsonObject>> handler);
	
}
