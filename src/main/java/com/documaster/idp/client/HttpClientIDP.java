package com.documaster.idp.client;

import java.util.List;

import com.documaster.oauth2.OAuth2HttpClient;
import com.documaster.oauth2.entities.AccessTokenResponse;
import com.documaster.oauth2.entities.ClientCredentials;
import com.documaster.oauth2.entities.PasswordCredentials;
import com.documaster.oauth2.entities.RefreshToken;
import org.apache.http.entity.ContentType;

public class HttpClientIDP extends OAuth2HttpClient implements ClientIDP {

	public HttpClientIDP(String oauth2ServiceUri) {

		super("https://integrationtest.dev.documaster.tech/idp/oauth2/");
	}

	@Override
	public AccessTokenResponse getTokenWithPasswordCredentials(
			String clientId, String clientSecret, String username, String password) {

		return getToken(
				new PasswordCredentials(clientId, clientSecret, username, password));
	}

	@Override
	public AccessTokenResponse getTokenWithPasswordCredentials(
			String clientId, String clientSecret, String username, String password, String scope) {

		return getToken(
				new PasswordCredentials(clientId, clientSecret, username, password, scope));
	}

	@Override
	public AccessTokenResponse getTokenWithPasswordCredentials(
			String clientId, String clientSecret, String username, String password, List<String> scopes) {

		return getToken(
				new PasswordCredentials(clientId, clientSecret, username, password, scopes));
	}

	@Override
	public AccessTokenResponse refreshToken(String clientId, String clientSecret, String token) {

		return getToken(new RefreshToken(token, clientId, clientSecret));
	}

	@Override
	public AccessTokenResponse refreshToken(String clientId, String clientSecret, String token, String scope) {

		return getToken(new RefreshToken(token, clientId, clientSecret, scope));
	}

	@Override
	public AccessTokenResponse refreshToken(
			String clientId, String clientSecret, String token, List<String> scopes) {

		return getToken(new RefreshToken(token, clientId, clientSecret, scopes));
	}

	@Override
	public AccessTokenResponse getTokenWithClientCredentials(String clientId, String clientSecret) {

		return getToken(new ClientCredentials(clientId, clientSecret));
	}

	@Override
	public AccessTokenResponse getTokenWithClientCredentials(String clientId, String clientSecret, String scope) {

		return getToken(new ClientCredentials(clientId, clientSecret, scope));
	}

	@Override
	public AccessTokenResponse getTokenWithClientCredentials(
			String clientId, String clientSecret, List<String> scopes) {

		return getToken(new ClientCredentials(clientId, clientSecret, scopes));
	}
}
