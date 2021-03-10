package com.documaster.idp.client;

public enum OpenIDConnectScope {

	OPENID("openid"),
	PROFILE("profile"),
	OFFLINE_ACCESS("offline_access"),
	EMAIL("email");

	private String name;

	private OpenIDConnectScope(String name) {

		this.name = name;
	}

	public String getName() {

		return name;
	}
}
