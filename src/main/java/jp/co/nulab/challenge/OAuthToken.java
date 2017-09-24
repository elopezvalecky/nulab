package jp.co.nulab.challenge;

public class OAuthToken {

	private String accessToken;
	private String tokenSecret;

	public OAuthToken(String accessToken, String tokenSecret) {
		this.accessToken = accessToken;
		this.tokenSecret = tokenSecret;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

}
