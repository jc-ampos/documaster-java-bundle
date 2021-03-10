package com.documaster.oauth2.exceptions;

public class Oauth2ServiceException extends RuntimeException {

	public Oauth2ServiceException(String message) {

		super(message);
	}

	public Oauth2ServiceException(String message, Throwable t) {

		super(message, t);
	}
}
