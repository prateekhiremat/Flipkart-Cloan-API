package com.ecommerce.responseDTO;

import com.ecommerce.Enum.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class UserResponse {
	private int userId;
	private String username;
	private String email;
	private UserRole userRole;
}
