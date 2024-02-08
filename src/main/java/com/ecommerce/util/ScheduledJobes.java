package com.ecommerce.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ecommerce.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ScheduledJobes {
	@Autowired
	private UserRepository userRepository;
//	@Scheduled(fixedDelay = 1000L*60)
//	@Transactional
	public void delete() {
		userRepository.findByIsDeleated(true).forEach(user -> {
				userRepository.delete(user.get());
		});
		userRepository.findByIsEmailVarified(true).forEach(user -> {
			userRepository.delete(user.get());
	});
	}
}
