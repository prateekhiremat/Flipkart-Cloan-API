package com.ecommerce.exception;

public class UserAlreadyLoggedInException extends RuntimeException {
	private String message;

	public UserAlreadyLoggedInException(String message) {
		super();
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
}
