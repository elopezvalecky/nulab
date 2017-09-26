package jp.co.nulab.challenge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

public class CacooUser extends AbstractUser implements CacooAPI {

	final JsonObject principal;
	final OAuthToken token;

	CacooAuthProvider provider;

	public CacooUser(CacooAuthProvider provider, JsonObject principal, OAuthToken token) {
		this.provider = provider;
		this.principal = principal;
		this.token = token;
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
	protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
		// TODO to be implemented
	}

	@Override
	public void getDiagrams(Integer offset, Integer limit, String type, String sortOn, String sortType, Integer folderId, Handler<AsyncResult<JsonObject>> handler) {
		final Map<String,Object> params = new HashMap<String,Object>(6);
		
		params.put("offset", offset);
		params.put("limit", limit);
		params.put("type", type);
		params.put("sortOn", sortOn);
		params.put("sortType", sortType);
		params.put("folderId", folderId);
		
		final List<String> queryString = params.entrySet().stream().filter(e -> { return e.getValue() != null; }).map( e -> { return e.getKey()+"="+e.getValue(); }).collect(Collectors.toList());
		
		final String endpoint = "/api/v1/diagrams.json?"+StringUtils.join(queryString, '&');
		
		provider.api(HttpMethod.GET, endpoint, null, token, handler);
	}

	@Override
	public void getDiagram(String id, Handler<AsyncResult<JsonObject>> handler) {
		provider.api(HttpMethod.GET, "/api/v1/diagrams/"+id+".json", null, token, handler);
	}

	@Override
	public void getFolders(Handler<AsyncResult<JsonObject>> handler) {
		provider.api(HttpMethod.GET, "/api/v1/folders.json", null, token, handler);
	}

	@Override
	public void createDiagram(Integer folderId, String title, String description, String security, Handler<AsyncResult<JsonObject>> handler) {
		final JsonObject params = new JsonObject();
		
		params.put("title", title);
		params.put("description", description);
		params.put("security", security);
		params.put("folderId", folderId);		

		provider.api(HttpMethod.POST, "/api/v1/diagrams/create.json", params, token, handler);
	}

	@Override
	public void moveDiagram(Integer folderId, Handler<AsyncResult<JsonObject>> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyDiagram(String originalDigramId, Integer folderId, String title, String description, String security, Handler<AsyncResult<JsonObject>> handler) {
		final JsonObject params = new JsonObject();
		
		params.put("title", title);
		params.put("description", description);
		params.put("security", security);
		params.put("folderId", folderId);
		
		provider.api(HttpMethod.POST, "/api/v1/diagrams/"+originalDigramId+"/copy.json", params, token, handler);
	}

	@Override
	public void deleteDiagram(String id, Handler<AsyncResult<JsonObject>> handler) {
		provider.api(HttpMethod.GET, "/api/v1/diagrams/"+id+"/delete.json", null, token, handler);
	}

	@Override
	public void editorToken(String diagramId, Handler<AsyncResult<String>> handler) {
		final Future<JsonObject> future = Future.future();
		provider.api(HttpMethod.GET, "/api/v1/diagrams/"+diagramId+"/editor/token.json", null, token, future.completer());
		future
			.map(json -> { return json.getString("token"); })
			.setHandler(handler);
	}

}
