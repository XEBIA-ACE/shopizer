package com.salesmanager.core.business.configuration;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.springframework.cache.Cache;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class CacheConfiguration {

	private static final String EHCACHE_CONFIG = "spring/ehcache.xml";
	private static final String OBJECT_CACHE = "com.shopizer.OBJECT_CACHE";

	@Bean(destroyMethod = "close")
	public CacheManager jCacheManager() throws IOException, URISyntaxException {
		return Caching.getCachingProvider().getCacheManager(new ClassPathResource(EHCACHE_CONFIG).getURI(),
				getClass().getClassLoader());
	}

	@Bean
	public JCacheCacheManager serviceCacheManager(CacheManager jCacheManager) {
		return new JCacheCacheManager(jCacheManager);
	}

	@Bean
	public Cache serviceCache(JCacheCacheManager serviceCacheManager) {
		return serviceCacheManager.getCache(OBJECT_CACHE);
	}

}
