package com.ecommerce.serviceImpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Seller;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.ResponseStructure;

import jakarta.validation.Valid;

@Service
public class AuthServiceImpl implements AuthService {
	@Autowired
	private UserRepository userRepository;
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@Valid UserRequest userRequest) {
		
		User user = isExist(userRequest);
		
		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("Sucefully saved User");
		structure.setData(mapToUserResponce(user));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
	}
	
	private User isExist(UserRequest userRequest) {
		 return (User) userRepository.findByEmail(userRequest.getEmail()).map(user -> {
			 if(user.isEmailVarified())
				 throw new IllegalArgumentException("User Already Existed");
			 else {
				 //send verification code
				 return null;
			 }
		 }).orElse(userRepository.save(mapToUser(userRequest)));
	}
	
	private <T extends User>T mapToUser(@Valid UserRequest userRequest) {
		User user = null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER -> {
			user = new Customer();
		}
		case SELLER -> {
			user = new Seller();
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + userRequest.getUserRole());
		}

		user.setUserName(userRequest.getEmail().split("@")[0]);
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
		user.setUserRole(userRequest.getUserRole());
		return (T) user;
	}

	private UserResponse mapToUserResponce(User user) {
		return UserResponse.builder().userId(user.getUserId()).username(user.getUserName()).email(user.getEmail())
				.userRole(user.getUserRole()).build();

	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> deleteById(int userId) {
		return userRepository.findById(userId).map(user -> {
			if(user.isDeleated()==true)
				throw new IllegalArgumentException("User ID Not Found");
			
			user.setDeleated(true);
			
			ResponseStructure<UserResponse> structure = new ResponseStructure<>();
			structure.setStatus(HttpStatus.OK.value());
			structure.setMessage("Sucefully saved User");
			structure.setData(mapToUserResponce(userRepository.save(user)));
			return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.OK);
		}).orElseThrow(()->new IllegalArgumentException("User ID Not Found"));
	}
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> fetchById(int userId) {
		User user =userRepository.findById(userId).get();
		if(user!=null) {
		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("Found User");
		structure.setData(mapToUserResponce(user));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
		}else throw new IllegalArgumentException("User Not FoundBy Id!!!");
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> updateById(int userId, @Valid UserRequest userRequest) {
		// TODO Auto-generated method stub
		return null;
	}
}
