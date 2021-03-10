package com.documaster.oauth2;

import java.io.IOException;
import java.util.List;

import com.documaster.oauth2.entities.AccessTokenResponse;
import com.documaster.oauth2.entities.AuthorizationGrant;
import com.documaster.oauth2.entities.ErrorResponse;
import com.documaster.oauth2.exceptions.BadRequestException;
import com.documaster.oauth2.exceptions.Oauth2ServiceException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;

public class OAuth2HttpClient {

	private final String oauth2ServiceUri;
	private ObjectMapper objectMapper;

	public OAuth2HttpClient(String oauth2ServiceUri) {

		if (!oauth2ServiceUri.endsWith("/")) {

			oauth2ServiceUri = oauth2ServiceUri + "/";
		}

		this.oauth2ServiceUri = oauth2ServiceUri;
		this.objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public AccessTokenResponse getToken(AuthorizationGrant authorizationGrant) {

		if (authorizationGrant == null) {

			throw new IllegalArgumentException("authorizationGrant cannot be null");
		}

		String url = oauth2ServiceUri + "token";

		try {

			Request request = buildHttpRequest(url, "POST");
			request.body(new UrlEncodedFormEntity(authorizationGrant.GetAsParams()));

			HttpResponse response = request.execute().returnResponse();
			readResponseStatus(response);
			return deserializeResponse(response);

		} catch (IOException e) {

			throw new Oauth2ServiceException("Could not execute request", e);
		}
	}

	public AccessTokenResponse getTokenWithAuthorizationHeaders(AuthorizationGrant authorizationGrant) {

		if (authorizationGrant == null) {

			throw new IllegalArgumentException("authorizationGrant cannot be null");
		}

		String url = oauth2ServiceUri + "token";

		try {

			Request request = buildHttpRequest(url, "POST");
			List<NameValuePair> authorizationParameters = authorizationGrant.GetAsParams();
			for (NameValuePair parameter : authorizationParameters) {
				request.addHeader(parameter.getName(), parameter.getValue());
			}

			HttpResponse response = request.execute().returnResponse();
			readResponseStatus(response);
			return deserializeResponse(response);

		} catch (IOException e) {

			throw new Oauth2ServiceException("Could not execute request", e);
		}
	}

	private Request buildHttpRequest(String url, String httpMethod) {

		switch (httpMethod) {

			case "POST":
				return Request.Post(url);
			case "GET":
				return Request.Get(url);
			case "PUT":
				return Request.Put(url);
			case "DELETE":
				return Request.Delete(url);
			default:
				return Request.Get(url);
		}
	}

	private void readResponseStatus(HttpResponse response) {

		StatusLine statusLine = response.getStatusLine();

		if (statusLine == null) {

			throw new Oauth2ServiceException("Could not get the response status");
		}

		int httpStatus = statusLine.getStatusCode();

		if (httpStatus >= HttpStatus.SC_BAD_REQUEST && httpStatus < HttpStatus.SC_INTERNAL_SERVER_ERROR) {

			ErrorResponse errorResponse;

			try {

				errorResponse = objectMapper.readValue(response.getEntity().getContent(), ErrorResponse.class);

			} catch (Exception exception) {

				throw new BadRequestException(String.format("Request failed with http status code %s", httpStatus));
			}

			throw new BadRequestException(
					String.format("Request failed with http status code %s. Response was: %s", httpStatus,
							errorResponse));

		} else if (httpStatus != HttpStatus.SC_OK) {

			throw new Oauth2ServiceException(String.format("Request failed with http status code %s", httpStatus));
		}
	}

	private AccessTokenResponse deserializeResponse(HttpResponse response) {

		try {
			return objectMapper.readValue(response.getEntity().getContent(), AccessTokenResponse.class);
		} catch (Exception e) {

			throw new Oauth2ServiceException("Failed to deserialize response!", e);
		}
	}

	public ObjectMapper getObjectMapper() {

		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {

		this.objectMapper = objectMapper;
	}
}
