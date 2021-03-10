package com.documaster.oauth2.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

	private String error;
	private String description;
	private String uri;

	@JsonCreator
	public ErrorResponse(
			@JsonProperty("error") String error,
			@JsonProperty("error_description") String description,
			@JsonProperty("error_uri") String uri) {

		this.error = error;
		this.description = description;
		this.uri = uri;
	}

	public String getError() {

		return error;
	}

	public String getDescription() {

		return description;
	}

	public String getUri() {

		return uri;
	}

	@Override
	public String toString() {

		return String.format("ErrorResponse { Error = '%s', Description = '%s', Uri = '%s' }", error, description, uri);
	}
}
