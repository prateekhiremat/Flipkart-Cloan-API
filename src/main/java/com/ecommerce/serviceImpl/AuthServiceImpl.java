package com.ecommerce.serviceImpl;

import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ecommerce.cache.CacheStore;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Seller;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InvalidOtpException;
import com.ecommerce.exception.OTPExpiredException;
import com.ecommerce.exception.RegistrationExpiredException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.requestDTO.MessageStructure;
import com.ecommerce.requestDTO.OtpModel;
import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CacheStore<String> cacheStore;
	@Autowired
	private CacheStore<User> userCacheStore;
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@Valid UserRequest userRequest) {
		if(userRepository.existsByEmail(userRequest.getEmail())) throw new IllegalArgumentException("User already exists by given email Id");

		String otp = getOtp();
		User user = mapToUser(userRequest);
		cacheStore.add(userRequest.getEmail(), otp);
		userCacheStore.add(userRequest.getEmail(), user);
		
		try {
			sendOtpToMail(user, otp);
		} catch (MessagingException e) {
			log.error("The email address does not exist");
		}
		
		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("Please verify through OTP sent on email id ");
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
		
		try {
			registrationComplete(user);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.OK.value());
		structure.setMessage("Email Varified");
		structure.setData(mapToUserResponce(save));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.OK);
		}else
			throw new InvalidOtpException("invalid otp");
	}
	
	public void sendOtpToMail(User user, String otp) throws MessagingException {
		sendMail(MessageStructure.builder()
		.to(user.getEmail())
		.subject("Complete your registration to flipkart")
		.sentDate(new Date())
		.text("hey, "+user.getUserName()+" Good to see you intrested in flipkart,"
				+"Complete your registration using the OTP <br>"
				+"<h1>"+otp+"</h1><br>"
				+"Note: the OTP expires in 1 minute"
				+"<br><br>"
				+"with best regards"
				+"<h1>Flipkart</h1>")
		.build());
		
	}
	
	private void registrationComplete(User user) throws MessagingException {
		sendMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Your registration to flipkart is completed")
				.sentDate(new Date())
				.text("hey, "+user.getUserName()+" yor register is completed"
						+"you can enjoy the services of <h1>Flipkart</h1>")
				.build());
	}
	
	@Async
	private void sendMail(MessageStructure message) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(), true);
		javaMailSender.send(mimeMessage);
		
	}
	
	private String getOtp() {
		return String.valueOf(new Random().nextInt(100000,999999));
	}
	
	private <T extends User>T mapToUser(UserRequest userRequest) {
		User user = null;
		switch (userRequest.getUserRole()) {
		
		case CUSTOMER -> {user = new Customer();}
		
		case SELLER -> {user = new Seller();}
		
		default -> throw new IllegalArgumentException("Unexpected value: " + userRequest.getUserRole());}

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
