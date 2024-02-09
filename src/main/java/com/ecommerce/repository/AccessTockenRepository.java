package com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.entity.AccessTocken;

public interface AccessTockenRepository extends JpaRepository<AccessTocken, Long> {

}
