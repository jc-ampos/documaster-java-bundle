package com.documaster.oauth2.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class ClientCredentials extends ClientCredentialsBase {

	public ClientCredentials(String clientId, String clientSecret) {

		super(GrantType.CLIENT_CREDENTIALS, clientId, clientSecret);
	}

	public ClientCredentials(String clientId, String clientSecret, String scope) {

		super(GrantType.CLIENT_CREDENTIALS, clientId, clientSecret, scope);
	}

	public ClientCredentials(String clientId, String clientSecret, List<String> scopes) {

		super(GrantType.CLIENT_CREDENTIALS, clientId, clientSecret, scopes);
	}

	@Override
	public List<NameValuePair> GetAsParams() {

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(GRANT_TYPE_PARAM_NAME, getGrantType().getName()));
		params.add(new BasicNameValuePair(CLIENT_ID_PARAM_NAME, clientId));
		params.add(new BasicNameValuePair(CLIENT_SECRET_PARAM_NAME, clientSecret));

		if (scope != null && !scope.trim().isEmpty()) {
			params.add(new BasicNameValuePair(SCOPE_PARAM_NAME, scope));
		}

		return params;
	}
}
