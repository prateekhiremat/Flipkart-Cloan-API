package com.ecommerce.exception;

public class OTPExpiredException extends RuntimeException {
	private String message;

	public OTPExpiredException(String message) {
		super();
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
