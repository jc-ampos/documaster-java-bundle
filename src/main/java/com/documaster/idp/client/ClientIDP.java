package com.documaster.idp.client;

import java.util.List;

import com.documaster.oauth2.entities.AccessTokenResponse;

public interface ClientIDP {

	//Resource Owner Password Credentials Grant
	public AccessTokenResponse getTokenWithPasswordCredentials(
			String clientId, String clientSecret, String username, String password);

	public AccessTokenResponse getTokenWithPasswordCredentials(
			String clientId, String clientSecret, String username, String password, String scope);

	public AccessTokenResponse getTokenWithPasswordCredentials(
			String clientId, String clientSecret, String username, String password, List<String> scopes);

	//Refresh Token Grant
	public AccessTokenResponse refreshToken(
			String clientId, String clientSecret, String token);

	public AccessTokenResponse refreshToken(
			String clientId, String clientSecret, String token, String scope);

	public AccessTokenResponse refreshToken(
			String clientId, String clientSecret, String token, List<String> scopes);

	//Client Credentials Grant
	public AccessTokenResponse getTokenWithClientCredentials(String clientId, String clientSecret);

	public AccessTokenResponse getTokenWithClientCredentials(String clientId, String clientSecret, String scope);

	public AccessTokenResponse getTokenWithClientCredentials(String clientId, String clientSecret, List<String> scopes);

}
