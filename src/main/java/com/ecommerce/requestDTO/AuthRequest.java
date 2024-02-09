package com.ecommerce.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
public class AuthRequest {
	@Email(regexp = "[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}", message = "invalid email")
	private String email;
	@Pattern(regexp =  "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$", message = "Password must"
			+ " contain at least one small letter, one capital letter, one number, one special character")
	private String password;
}
