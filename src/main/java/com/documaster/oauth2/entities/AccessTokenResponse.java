package com.documaster.oauth2.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenResponse {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("token_type")
	private String tokenType;

	//RFC 6749: RECOMMENDED.  The lifetime in seconds of the access token.
	@JsonProperty("expires_in")
	private long expiresInSeconds;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("scope")
	private String scope;

	@JsonCreator
	public AccessTokenResponse(
			@JsonProperty("access_token") String accessToken,
			@JsonProperty("token_type") String tokenType,
			@JsonProperty("expires_in") long expiresInSeconds,
			@JsonProperty("refresh_token") String refreshToken,
			@JsonProperty("scope") String scope) {

		if (accessToken == null || accessToken.trim().isEmpty()) {

			throw new IllegalArgumentException("accessToken cannot be blank.");
		}

		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresInSeconds = expiresInSeconds;
		this.refreshToken = refreshToken;
		this.scope = scope;
	}

	public long getExpiresInSeconds() {

		return expiresInSeconds;
	}

	public String getAccessToken() {

		return accessToken;
	}

	public String getRefreshToken() {

		return refreshToken;
	}

	public String getScope() {

		return scope;
	}

	public String getTokenType() {

		return tokenType;
	}
}
