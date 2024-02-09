package com.ecommerce.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	@Value("${myapp.secret}")//get value from key in prop file
	private String secret;
	
	@Value("${myapp.access.expiry}")
	private Long accessExpirationInSeconds;
	
	@Value("${myapp.refresh.expiry}")
	private Long refreshExpirationInSeconds;
	
	public String generateAccessToken(String userName) {
		return generateJET(new HashMap<String, Object>(), userName, accessExpirationInSeconds*1000l);
	}
	
	public String generateRefreshToken(String userName) {
		return generateJET(new HashMap<String, Object>(), userName, refreshExpirationInSeconds*1000l);
	}
	
	private String generateJET(Map<String, Object> claims, String username, Long expiry) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis()+ expiry))
				.signWith(getSignature(), SignatureAlgorithm.HS512) //Signing the JWT with key
				.compact();
	}
	
	private Key getSignature() {
		byte[] secretBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(secretBytes);
	}
	
}
