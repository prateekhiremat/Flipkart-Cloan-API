package com.ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.entity.AccessTocken;
import com.ecommerce.entity.RefreshTocken;
import com.ecommerce.entity.User;

public interface RefreshTockenRepository extends JpaRepository<RefreshTocken, Long> {

	public Optional<RefreshTocken> findByToken(String token);
	
	public List<Optional<RefreshTocken>> findAllByExpirationBefore(LocalDateTime date);

	public Optional<RefreshTocken> findByTokenAndIsBlocked(String token, boolean b);

	public List<Optional<RefreshTocken>> findByUser(User user);

	public List<RefreshTocken> findAllByUserAndIsBlocked(User user, boolean b);

	public List<RefreshTocken> findAllByUserAndIsBlockedAndTokenNot(User user, boolean b, String refreshToken);
	
	public boolean existsByUser(User user);

	public boolean existsByUserAndIsBlocked(User user, boolean b);
	
}
