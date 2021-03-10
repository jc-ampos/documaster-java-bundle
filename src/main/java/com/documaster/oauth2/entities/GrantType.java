package com.documaster.oauth2.entities;

public enum GrantType {

	PASSWORD("password"),
	CLIENT_CREDENTIALS("client_credentials"),
	REFRESH_TOKEN("refresh_token");

	private String name;

	private GrantType(String name) {

		this.name = name;
	}

	public String getName() {

		return name;
	}
}
