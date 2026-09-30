package com.salesmanager.core.business.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Single source of the application {@link PasswordEncoder} (REQ-F01-03).
 * Algorithm and work factor are read from authentication.properties.
 */
@Configuration
@PropertySource("classpath:authentication.properties")
public class PasswordEncoderConfiguration {

	public static final String BCRYPT = "bcrypt";

	@Value("${security.password.encoder.algorithm}")
	private String algorithm;

	@Value("${security.password.encoder.strength}")
	private int strength;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return createPasswordEncoder(algorithm, strength);
	}

	public static PasswordEncoder createPasswordEncoder(String algorithm, int strength) {
		if (!BCRYPT.equalsIgnoreCase(algorithm == null ? null : algorithm.trim())) {
			throw new IllegalStateException(
					"Unsupported password encoder algorithm [" + algorithm + "], only [" + BCRYPT + "] is allowed");
		}
		return new BCryptPasswordEncoder(strength);
	}

}
