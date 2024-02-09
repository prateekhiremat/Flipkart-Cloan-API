package com.ecommerce.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessTocken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long tokenId;
	private String token;
	private boolean isBlocked;
	private LocalDateTime expiration;
}
