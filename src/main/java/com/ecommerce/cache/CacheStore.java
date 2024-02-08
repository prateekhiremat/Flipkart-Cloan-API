package com.ecommerce.cache;

import java.time.Duration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CacheStore<T> {
	
	private Cache<String, T> cache;

	public CacheStore(Duration expiery) {
		this.cache = CacheBuilder.newBuilder().expireAfterWrite(expiery)
				.concurrencyLevel(Runtime.getRuntime().availableProcessors())
				.build();
	}
	
	public void add(String key, T value) {
		cache.put(key, value);
	}
	
	public T get(String key) {
		return cache.getIfPresent(key);
	}
	
	public void remove(T key) {
		cache.invalidate(key);
	}
}
