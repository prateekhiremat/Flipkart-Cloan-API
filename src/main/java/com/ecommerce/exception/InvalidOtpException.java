package com.ecommerce.exception;

public class InvalidOtpException extends RuntimeException {
	private String message;

	public InvalidOtpException(String message) {
		super();
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
