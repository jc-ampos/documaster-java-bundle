package com.documaster.oauth2.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class RefreshToken extends ClientCredentialsBase {

	private static final String REFRESH_TOKEN_PARAM_NAME = "refresh_token";

	@JsonProperty(REFRESH_TOKEN_PARAM_NAME)
	private final String refreshToken;

	public RefreshToken(String refreshToken, String clientId, String clientSecret) {

		super(GrantType.REFRESH_TOKEN, clientId, clientSecret);
		this.refreshToken = refreshToken;
	}

	public RefreshToken(String refreshToken, String clientId, String clientSecret, String scope) {

		super(GrantType.REFRESH_TOKEN, clientId, clientSecret, scope);
		this.refreshToken = refreshToken;
	}

	public RefreshToken(String refreshToken, String clientId, String clientSecret, List<String> scopes) {

		super(GrantType.REFRESH_TOKEN, clientId, clientSecret, scopes);
		this.refreshToken = refreshToken;
	}

	@Override
	public List<NameValuePair> GetAsParams() {

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(GRANT_TYPE_PARAM_NAME, getGrantType().getName()));
		params.add(new BasicNameValuePair(REFRESH_TOKEN_PARAM_NAME, refreshToken));

		if (clientId != null && !clientId.trim().isEmpty()) {
			params.add(new BasicNameValuePair(CLIENT_ID_PARAM_NAME, clientId));
		}

		if (clientSecret != null && !clientSecret.trim().isEmpty()) {
			params.add(new BasicNameValuePair(CLIENT_SECRET_PARAM_NAME, clientSecret));
		}

		if (scope != null && !scope.trim().isEmpty()) {
			params.add(new BasicNameValuePair(SCOPE_PARAM_NAME, scope));
		}

		return params;
	}

	public String getRefreshToken() {

		return refreshToken;
	}
}
