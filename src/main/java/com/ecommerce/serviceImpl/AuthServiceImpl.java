package com.ecommerce.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;

import com.ecommerce.cache.CacheStore;
import com.ecommerce.entity.AccessTocken;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.RefreshTocken;
import com.ecommerce.entity.Seller;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InvalidOtpException;
import com.ecommerce.exception.OTPExpiredException;
import com.ecommerce.exception.RegistrationExpiredException;
import com.ecommerce.exception.UserAlreadyLoggedInException;
import com.ecommerce.exception.UserNotLoggedInException;
import com.ecommerce.repository.AccessTockenRepository;
import com.ecommerce.repository.RefreshTockenRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.requestDTO.AuthRequest;
import com.ecommerce.requestDTO.MessageStructure;
import com.ecommerce.requestDTO.OtpModel;
import com.ecommerce.requestDTO.UserRequest;
import com.ecommerce.responseDTO.AuthResponse;
import com.ecommerce.responseDTO.UserResponse;
import com.ecommerce.security.JwtService;
import com.ecommerce.service.AuthService;
import com.ecommerce.util.CookieManager;
import com.ecommerce.util.ResponseStructure;
import com.ecommerce.util.SimpleResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
//@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

	private UserRepository userRepository;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;
	private AuthenticationManager authenticationManager;
	private CookieManager cookieManager;
	private JwtService jwtService;
	private AccessTockenRepository accessTockenRepository;
	private RefreshTockenRepository refreshTockenRepository;
	private ResponseStructure<AuthResponse> authStructure;
	private PasswordEncoder passwordEncoder;

	@Value("${myapp.access.expiry}")
	private int accessExpieryInSeconds;

	@Value("${myapp.refresh.expiry}")
	private int refreshExpieryInSeconds;


	public AuthServiceImpl(UserRepository userRepository, 
			CacheStore<String> cacheStore,
			CacheStore<User> userCacheStore, 
			JavaMailSender javaMailSender, 
			AuthenticationManager authenticationManager,
			CookieManager cookieManager, 
			JwtService jwtService, 
			AccessTockenRepository accessTockenRepository, 
			RefreshTockenRepository refreshTockenRepository, 
			ResponseStructure<AuthResponse> authStructure, 
			PasswordEncoder passwordEncoder) {
		super();
		this.userRepository = userRepository;
		this.otpCacheStore = cacheStore;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTockenRepository = accessTockenRepository;
		this.refreshTockenRepository = refreshTockenRepository;
		this.authStructure = authStructure;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@Valid UserRequest userRequest) {
		if(userRepository.existsByEmail(userRequest.getEmail())) throw new IllegalArgumentException("User already exists by given email Id");

		String otp = getOtp();
		User user = mapToUser(userRequest);
		otpCacheStore.add(userRequest.getEmail(), otp);
		userCacheStore.add(userRequest.getEmail(), user);

		//		try {
		//			sendOtpToMail(user, otp);
		//		} catch (MessagingException e) {
		//			log.error("The email address does not exist");
		//		}

		ResponseStructure<UserResponse> structure = new ResponseStructure<>();
		structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("Please verify through OTP sent on email id "+otp);
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
		String otp = otpCacheStore.get(otpModel.getEmail());

		if(otp==null) throw new OTPExpiredException("otp expired");
		if(user==null) throw new RegistrationExpiredException("registration expired");
		if(otp.equals(otpModel.getOtp())) {

			user.setEmailVarified(true);
			User save = userRepository.save(user);

			//		try {
			//			registrationComplete(user);
			//		} catch (MessagingException e) {
			//			// TODO Auto-generated catch block
			//			e.printStackTrace();
			//		}

			ResponseStructure<UserResponse> structure = new ResponseStructure<>();
			structure.setStatus(HttpStatus.OK.value());
			structure.setMessage("Email Varified");
			structure.setData(mapToUserResponce(save));
			return new ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.OK);
		}else
			throw new InvalidOtpException("invalid otp");
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse response) {
		String userName = authRequest.getEmail().split("@")[0];
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName, authRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(token);
		if(!authentication.isAuthenticated()) throw new UsernameNotFoundException("Faild to Authenticate the user");
		else
			return userRepository.findByUserName(userName).map(user -> {

				grantAccess(response, user);
				return ResponseEntity.ok(authStructure.setStatus(HttpStatus.OK.value())
						.setData(AuthResponse.builder()
								.userId(user.getUserId())
								.userName(userName)
								.role(user.getUserRole().name())
								.isAuthenticated(true)
								.accessExpiration(LocalDateTime.now().plusSeconds(accessExpieryInSeconds))
								.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpieryInSeconds))
								.build())
						.setMessage("Login success"));
			}).get();
	}

	@Override
	public ResponseEntity<SimpleResponseStructure> logout(String accessToken, String refreshToken, HttpServletResponse response) {

		if(accessToken==null && refreshToken==null) throw new UserNotLoggedInException("Please login");

		accessTockenRepository.findByToken(accessToken).ifPresent(token -> {
			token.setBlocked(true);
			accessTockenRepository.save(token);
		});
		refreshTockenRepository.findByToken(refreshToken).ifPresent(token -> {
			token.setBlocked(true);
			refreshTockenRepository.save(token);
		});

		response.addCookie(cookieManager.invalidate(new Cookie("at","")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt","")));

		return new ResponseEntity<SimpleResponseStructure>(SimpleResponseStructure.builder()
				.status(HttpStatus.OK.value()).message("Logged Out Successfully").build(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SimpleResponseStructure> logoutAllDevice(HttpServletResponse response) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if(name != null) {
			userRepository.findByUserName(name).ifPresent(user -> {
				List<AccessTocken> accessTockens = accessTockenRepository.findAllByUserAndIsBlocked(user, false);
				blockAccessToken(accessTockens);
				List<RefreshTocken> refreshTockens = refreshTockenRepository.findAllByUserAndIsBlocked(user, false);
				blockRefreshToken(refreshTockens);
			});;
		}

		response.addCookie(cookieManager.invalidate(new Cookie("at","")));
		response.addCookie(cookieManager.invalidate(new Cookie("rt","")));

		return new ResponseEntity<SimpleResponseStructure>(SimpleResponseStructure.builder()
				.status(HttpStatus.OK.value()).message("Logged Out from all device Successfully").build(), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<SimpleResponseStructure> logoutFromOtherDevice(String accessToken, String refreshToken) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if(name != null) {
			userRepository.findByUserName(name).ifPresent(user -> {
				List<AccessTocken> accessTockens = accessTockenRepository.findAllByUserAndIsBlockedAndTokenNot(user, false, accessToken);
				blockAccessToken(accessTockens);
				List<RefreshTocken> refreshTockens = refreshTockenRepository.findAllByUserAndIsBlockedAndTokenNot(user, false, refreshToken);
				blockRefreshToken(refreshTockens);
			});
		}
		return new ResponseEntity<SimpleResponseStructure>(SimpleResponseStructure.builder()
				.status(HttpStatus.OK.value()).message("Logged Out from other device Successfully").build(), HttpStatus.OK);
	}

	public ResponseEntity<SimpleResponseStructure> refreshLogin(String accessToken, String refreshToken, HttpServletResponse response){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if(name=="anonymousUser")throw new UserNotLoggedInException("Please login first");
			userRepository.findByUserName(name).ifPresent(user -> {
				if(user!=null) {
					List<AccessTocken> accessTockens = accessTockenRepository.findAllByUserAndIsBlocked(user, false);
					if(accessTockens!=null) {
						blockAccessToken(accessTockens);
					} 
					List<RefreshTocken> refreshTockens = refreshTockenRepository.findAllByUserAndIsBlocked(user, false);
					if(refreshTockens==null) throw new UserNotLoggedInException("Please relogin!!!");
					else {
						blockRefreshToken(refreshTockens);
						grantAccess(response, user);
					}
				}
			});

		return new ResponseEntity<SimpleResponseStructure>(SimpleResponseStructure.builder()
				.status(HttpStatus.OK.value()).message("re-logged in").build(), HttpStatus.OK);	
	}

	private void blockAccessToken(List<AccessTocken> accessTocken) {
		accessTocken.forEach(at -> {
			at.setBlocked(true);
			accessTockenRepository.save(at);
		});
	}

	private void blockRefreshToken(List<RefreshTocken> refreshTocken) {
		refreshTocken.forEach(rt -> {
			rt.setBlocked(true);
			refreshTockenRepository.save(rt);
		});
	}

	private void grantAccess(HttpServletResponse response, User user) {
		//generating access and refresh tokens 
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());

		//adding access and refresh tokens cookies to the response
		response.addCookie(cookieManager.configure(new Cookie("at", accessToken), accessExpieryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken), refreshExpieryInSeconds));
		//saving access and refresh cookies in the database
		if(!accessTockenRepository.existsByUserAndIsBlocked(user, false) && 
				!refreshTockenRepository.existsByUserAndIsBlocked(user, false)) {
			accessTockenRepository.save(AccessTocken.builder()
					.token(accessToken)
					.isBlocked(false)
					.user(user)
					.expiration(LocalDateTime.now().plusSeconds(accessExpieryInSeconds))
					.build());

			refreshTockenRepository.save(RefreshTocken.builder()
					.token(refreshToken)
					.isBlocked(false)
					.user(user)
					.expiration(LocalDateTime.now().plusSeconds(refreshExpieryInSeconds))
					.build());
		}else throw new UserAlreadyLoggedInException("You are already loggid in !!!");
	}

	private void sendOtpToMail(User user, String otp) throws MessagingException {
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
		user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
		user.setUserRole(userRequest.getUserRole());
		return (T) user;
	}

	private UserResponse mapToUserResponce(User user) {
		return UserResponse.builder().userId(user.getUserId()).username(user.getUserName()).email(user.getEmail())
				.userRole(user.getUserRole()).build();

	}

}
