package com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.entity.RefreshTocken;

public interface RefreshTockenRepository extends JpaRepository<RefreshTocken, Long> {

}
