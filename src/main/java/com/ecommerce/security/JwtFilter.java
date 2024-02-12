package com.ecommerce.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.entity.AccessTocken;
import com.ecommerce.exception.UserNotLoggedInException;
import com.ecommerce.repository.AccessTockenRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private AccessTockenRepository accessTockenRepository;

	private JwtService jwtService;

	private CustomUserDetailService userDetailService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String rt = null;
		String at = null;
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				if(cookie.getName().equals("at")) at = cookie.getValue();
				if(cookie.getName().equals("rt")) rt = cookie.getValue();
			}
			String userName = null;
			if(at!=null && rt!=null) {
				Optional<AccessTocken> accessToken = accessTockenRepository.findByTokenAndIsBlocked(at, false);

				if(accessToken == null) throw new UserNotLoggedInException("pleace login");
				else{
					log.info("Authenticating the TOKEN.....");
					userName = jwtService.extractUserName(at);
					if(userName == null) throw new UsernameNotFoundException("Faild to authenticate");
					UserDetails userDetails = userDetailService.loadUserByUsername(userName);
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken
							(userName, null, userDetails.getAuthorities());
					token.setDetails(new WebAuthenticationDetails(request));
					SecurityContextHolder.getContext().setAuthentication(token);
					log.info("Authenticated Successfully");
				}
			}
		}
		filterChain.doFilter(request, response);//delegates for further filters
	}
}