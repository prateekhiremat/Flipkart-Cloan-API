package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	Optional<User> findByUserName(String userName);
	
	Optional<User> findByEmail(String email);
	
	List<Optional<User>> findByIsEmailVarified(boolean b);
	
	List<Optional<User>> findByIsDeleated(boolean b);
	
}
