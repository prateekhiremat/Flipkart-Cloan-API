package com.ecommerce.service;

import org.springframework.http.ResponseEntity;

import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.util.ResponseStructure;

import jakarta.validation.Valid;

public interface AuthService {

	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@Valid UserRequest userRequest);

	public ResponseEntity<ResponseStructure<UserResponse>> deleteById(int userId);

	public ResponseEntity<ResponseStructure<UserResponse>> updateById(int userId, @Valid UserRequest userRequest);

	public ResponseEntity<ResponseStructure<UserResponse>> fetchById(int userId);

}
