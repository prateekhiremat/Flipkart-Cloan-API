package com.ecommerce.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApplicationHandler extends ResponseEntityExceptionHandler {
	private ResponseEntity<Object> structure (HttpStatus status,String message,Object rootCause){
		return new ResponseEntity<Object> (Map.of(
				"status",status.value(),
				"message",message,
				"rootCause",rootCause),status);		
	}
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			org.springframework.http.HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<ObjectError> allErrors = ex.getAllErrors();

		Map<String, String> errors= new  HashMap<String,String>();		
		allErrors.forEach(error ->{
			FieldError fieldError=(FieldError) error;
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		});
		return structure(HttpStatus.BAD_REQUEST,"failed to save the data",errors);
	}
	
	@ExceptionHandler (IllegalArgumentException.class)
	public ResponseEntity<Object> handlerIllegalArgument(IllegalArgumentException ex){
		return structure(HttpStatus.BAD_REQUEST,ex.getMessage(),"Request Not Applicable");
	}
}
