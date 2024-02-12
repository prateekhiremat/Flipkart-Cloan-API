package com.ecommerce.service;

import org.springframework.http.ResponseEntity;

import com.ecommerce.requestDTO.AuthRequest;
import com.ecommerce.requestDTO.OtpModel;
import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.AuthResponse;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.util.ResponseStructure;
import com.ecommerce.util.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthService {

	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@Valid UserRequest userRequest);

	public ResponseEntity<ResponseStructure<UserResponse>> deleteById(int userId);

	public ResponseEntity<ResponseStructure<UserResponse>> fetchById(int userId);

	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otp);

	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse response);

	public ResponseEntity<SimpleResponseStructure> logout(String accessToken, String refreshToken, HttpServletResponse response);

	public ResponseEntity<SimpleResponseStructure> logoutAllDevice(HttpServletResponse response);

	public ResponseEntity<SimpleResponseStructure> logoutFromOtherDevice(String accessToken, String refreshToken);

}
