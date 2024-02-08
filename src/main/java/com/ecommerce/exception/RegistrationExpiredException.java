package com.ecommerce.exception;

public class RegistrationExpiredException extends RuntimeException {
	private String message;

	public RegistrationExpiredException(String message) {
		super();
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
