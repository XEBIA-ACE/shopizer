package com.salesmanager.shop.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Spring Cache configuration backed by Caffeine.
 *
 * <p>Registers the {@code storeCurrencies} cache whose TTL is driven by the
 * {@code store.currency.cache.ttl.seconds} application property (default 300 s).
 * Changing that property and restarting the application is sufficient to alter
 * the TTL — no code modification is required (NFR-04).
 *
 * <p>If additional caches are needed in the future, add their names to the
 * {@link CaffeineCacheManager} constructor call or use
 * {@link CaffeineCacheManager#setAllowNullValues} / dynamic cache creation.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * TTL (seconds) for the {@code storeCurrencies} cache.
     * Sourced from {@code store.currency.cache.ttl.seconds}; falls back to 300 s.
     */
    @Value("${store.currency.cache.ttl.seconds:300}")
    private long currencyCacheTtlSeconds;

    /**
     * Primary {@link CacheManager} bean.
     *
     * <p>Uses a {@link CaffeineCacheManager} pre-configured with the
     * {@code storeCurrencies} cache. The Caffeine spec applies:
     * <ul>
     *   <li>expireAfterWrite — driven by {@code store.currency.cache.ttl.seconds}</li>
     *   <li>maximumSize — 500 entries (one entry per store code)</li>
     * </ul>
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("storeCurrencies");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(currencyCacheTtlSeconds, TimeUnit.SECONDS)
                        .maximumSize(500));
        return manager;
    }
}
