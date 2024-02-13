package com.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.requestDTO.AuthRequest;
import com.ecommerce.requestDTO.OtpModel;
import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.AuthResponse;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.ResponseStructure;
import com.ecommerce.util.SimpleResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

//kpiy himm hbgp rteo

@RestController
@RequestMapping
@AllArgsConstructor
public class AuthController {
	
	@Autowired
	private AuthService authservice;
	
	@PostMapping("/registration/user")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody @Valid UserRequest userRequest){
		return authservice.registerUser(userRequest);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@DeleteMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> deleteById(@PathVariable int userId){
		return authservice.deleteById(userId);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> fetchById(@PathVariable int userId){
		return authservice.fetchById(userId);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@PutMapping("/user/otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(@RequestBody  OtpModel otp){
		return authservice.verifyOTP(otp);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest, HttpServletResponse response){
		return authservice.login(authRequest, response);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@PostMapping("/user/logout")
	public ResponseEntity<SimpleResponseStructure> logout(@CookieValue(name = "at", required = false)String accessToken, 
			@CookieValue(name = "rt", required = false)String refreshToken, HttpServletResponse response) {
		return authservice.logout(accessToken, refreshToken,response);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@PostMapping("user/logout/allDevice")
	public ResponseEntity<SimpleResponseStructure> logoutFromAllDevice(HttpServletResponse response){
		return authservice.logoutAllDevice(response);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@PostMapping("user/logout/otherDevice")
	public ResponseEntity<SimpleResponseStructure> logoutFromOtherDevice(@CookieValue(name = "at", required = false)String accessToken, 
			@CookieValue(name = "rt", required = false)String refreshToken){
		return authservice.logoutFromOtherDevice(accessToken, refreshToken);
	}
	
	@PreAuthorize(value = "hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
	@PostMapping("user/refresh-login")
	public ResponseEntity<SimpleResponseStructure> refreshLogin(@CookieValue(name = "at", required = false)String accessToken, 
			@CookieValue(name = "rt", required = false)String refreshToken, HttpServletResponse response){
		return authservice.refreshLogin(accessToken, refreshToken, response);
	}
}
