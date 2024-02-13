package com.ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.entity.AccessTocken;
import com.ecommerce.entity.RefreshTocken;
import com.ecommerce.entity.User;

public interface AccessTockenRepository extends JpaRepository<AccessTocken, Long> {
	
	public Optional<AccessTocken> findByToken(String token);
	
	public List<Optional<AccessTocken>> findAllByExpirationBefore(LocalDateTime date);

	public Optional<AccessTocken> findByTokenAndIsBlocked(String token, boolean b);

	public List<Optional<AccessTocken>> findByUser(User user);

	public List<AccessTocken> findAllByUserAndIsBlocked(User user, boolean b);

	public List<AccessTocken> findAllByUserAndIsBlockedAndTokenNot(User user, boolean b, String accessToken);
	
	public boolean existsByUser(User user);

	public boolean existsByUserAndIsBlocked(User user, boolean b);
}