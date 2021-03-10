package com.documaster.oauth2.entities;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ClientCredentialsBase extends AuthorizationGrant {

	protected static final String CLIENT_ID_PARAM_NAME = "client_id";
	protected static final String CLIENT_SECRET_PARAM_NAME = "client_secret";
	protected static final String SCOPE_PARAM_NAME = "scope";

	@JsonProperty(CLIENT_ID_PARAM_NAME)
	protected final String clientId;

	@JsonProperty(CLIENT_SECRET_PARAM_NAME)
	protected final String clientSecret;

	@JsonProperty(SCOPE_PARAM_NAME)
	protected String scope;

	protected ClientCredentialsBase(GrantType grantType, String clientId, String clientSecret) {

		super(grantType);
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	protected ClientCredentialsBase(GrantType grantType, String clientId, String clientSecret, String scope) {

		this(grantType, clientId, clientSecret);
		this.scope = scope;
	}

	protected ClientCredentialsBase(GrantType grantType, String clientId, String clientSecret, List<String> scopes) {

		this(grantType, clientId, clientSecret);

		if (scopes != null) {

			this.scope = scopes.stream()
					.filter(scope -> scope != null && !scope.trim().isEmpty())
					.collect(Collectors.joining(" "));
		}
	}

	public String getClientId() {

		return clientId;
	}

	public String getClientSecret() {

		return clientSecret;
	}

	public String getScope() {

		return scope;
	}
}
