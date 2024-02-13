package com.ecommerce.util;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.repository.AccessTockenRepository;
import com.ecommerce.repository.RefreshTockenRepository;
import com.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledJobes {
	
	private UserRepository userRepository;
	
	private AccessTockenRepository accessTockenRepository;
	
	private RefreshTockenRepository refreshTockenRepository;
	
	@Scheduled(fixedDelay = 1000L*60*60)
	@Transactional
	public void delete() {
		userRepository.findByIsDeleated(true).forEach(user -> {
			userRepository.delete(user.get());
		});
		accessTockenRepository.findAllByExpirationBefore(LocalDateTime.now()).forEach(tocken -> {
			if(tocken.get().getExpiration().isEqual(LocalDateTime.now()) ||
					tocken.get().getExpiration().isBefore(LocalDateTime.now()))
				accessTockenRepository.delete(tocken.get());
		});
		refreshTockenRepository.findAllByExpirationBefore(LocalDateTime.now()).forEach(tocken -> {
			if(tocken.get().getExpiration().isEqual(LocalDateTime.now()) ||
					tocken.get().getExpiration().isBefore(LocalDateTime.now()))
				refreshTockenRepository.delete(tocken.get());
		});
	}
}