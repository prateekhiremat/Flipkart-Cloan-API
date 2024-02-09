package com.ecommerce.responseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	private int userId;
	private String userName;
	private String role;
	private boolean isAuthenticated;
	private LocalDateTime accessExpiration;
	private LocalDateTime refreshExpiration;
}
