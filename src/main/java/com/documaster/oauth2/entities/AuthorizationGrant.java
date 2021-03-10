package com.documaster.oauth2.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.NameValuePair;

public abstract class AuthorizationGrant {

	protected static final String GRANT_TYPE_PARAM_NAME = "grant_type";

	@JsonProperty(GRANT_TYPE_PARAM_NAME)
	private final GrantType grantType;

	protected AuthorizationGrant(GrantType grantType) {

		this.grantType = grantType;
	}

	public GrantType getGrantType() {

		return grantType;
	}

	public abstract List<NameValuePair> GetAsParams();
}
