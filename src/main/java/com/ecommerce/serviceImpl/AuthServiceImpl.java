package com.ecommerce.serviceImpl;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.cache.CacheStore;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Seller;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InvalidOtpException;
import com.ecommerce.exception.OTPExpiredException;
import com.ecommerce.exception.RegistrationExpiredException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.requestDTO.OtpModel;
import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.ResponseStructure;

import jakarta.validation.Valid;

@Service
public class AuthServiceImpl implements AuthService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CacheStore<String> cacheStore;
	@Autowired
	private CacheStore<User> userCacheStore;
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@Valid UserRequest userRequest) {
		if(userRepository.existsByEmail(userRequest.getEmail())) throw new IllegalArgumentException("User already exists by given email Id");

		String otp = getOtp();
		User user = mapToUser(userRequest);
		cacheStore.add(userRequest.getEmail(), otp);
		userCacheStore.add(userRequest.getEmail(), user);

		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("Sucefully saved User "+otp);
		structure.setData(mapToUserResponce(user));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
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
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel) {
		User user = userCacheStore.get(otpModel.getEmail());
		String otp = cacheStore.get(otpModel.getEmail());

		if(otp==null) throw new OTPExpiredException("otp expired");
		if(user==null) throw new RegistrationExpiredException("registration expired");
		if(otp.equals(otpModel.getOtp())) {
		
		user.setEmailVarified(true);
		User save = userRepository.save(user);
		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.OK.value());
		structure.setMessage("Email Varified");
		structure.setData(mapToUserResponce(save));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.OK);
		}else
			throw new InvalidOtpException("invalid otp");
	}
	private String getOtp() {
		return String.valueOf(new Random().nextInt(100000,999999));
	}
	
	private <T extends User>T mapToUser(UserRequest userRequest) {
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
}
