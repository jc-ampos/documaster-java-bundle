package com.documaster.oauth2.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class PasswordCredentials extends ClientCredentialsBase {

	private static final String USERNAME_PARAM_NAME = "username";
	private static final String PASSWORD_PARAM_NAME = "password";

	@JsonProperty(USERNAME_PARAM_NAME)
	private final String username;

	@JsonProperty(PASSWORD_PARAM_NAME)
	private final String password;

	public PasswordCredentials(String clientId, String clientSecret, String username, String password) {

		super(GrantType.PASSWORD, clientId, clientSecret);
		this.username = username;
		this.password = password;
	}

	public PasswordCredentials(String clientId, String clientSecret, String username, String password, String scope) {

		super(GrantType.PASSWORD, clientId, clientSecret, scope);
		this.username = username;
		this.password = password;
	}

	public PasswordCredentials(
			String clientId, String clientSecret, String username, String password, List<String> scopes) {

		super(GrantType.PASSWORD, clientId, clientSecret, scopes);
		this.username = username;
		this.password = password;
	}

	public String getUsername() {

		return username;
	}

	public String getPassword() {

		return password;
	}

	@Override
	public List<NameValuePair> GetAsParams() {

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(GRANT_TYPE_PARAM_NAME, getGrantType().getName()));
		params.add(new BasicNameValuePair(USERNAME_PARAM_NAME, username));
		params.add(new BasicNameValuePair(PASSWORD_PARAM_NAME, password));

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
}
