package com.salesmanager.core.business.services.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.salesmanager.core.model.common.CredentialsReset;

@Service("passwordResetTokenService")
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final int TOKEN_BYTES = 32;
	private static final String HTTPS = "https";
	private static final List<String> LOOPBACK_HOSTS = Arrays.asList("localhost", "127.0.0.1", "[::1]", "::1");

	private final SecureRandom secureRandom = new SecureRandom();
	private final SecretKeySpec secretKey;
	private final long expirationSeconds;
	private final boolean httpsRequired;
	private final Clock clock;

	@Autowired
	public PasswordResetTokenServiceImpl(
			@Value("${password.reset.token.secret}") String secret,
			@Value("${password.reset.token.expiration:3600}") long expirationSeconds,
			@Value("${password.reset.https.required:true}") boolean httpsRequired) {
		this(secret, expirationSeconds, httpsRequired, Clock.systemUTC());
	}

	PasswordResetTokenServiceImpl(String secret, long expirationSeconds, boolean httpsRequired, Clock clock) {
		Validate.notBlank(secret, "password.reset.token.secret must be configured");
		Validate.isTrue(expirationSeconds > 0, "password.reset.token.expiration must be positive");
		Validate.notNull(clock, "Clock cannot be null");
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
		this.expirationSeconds = expirationSeconds;
		this.httpsRequired = httpsRequired;
		this.clock = clock;
	}

	@Override
	public String generateToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	@Override
	public String hashToken(String token) {
		Validate.notBlank(token, "Token cannot be empty");
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(secretKey);
			byte[] digest = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new IllegalStateException("Cannot hash password reset token", e);
		}
	}

	@Override
	public CredentialsReset createCredentialsReset(String token) {
		CredentialsReset credentialsReset = new CredentialsReset();
		credentialsReset.setCredentialsRequest(hashToken(token));
		credentialsReset.setCredentialsRequestExpiry(
				new Date(clock.millis() + expirationSeconds * 1000L));
		return credentialsReset;
	}

	@Override
	public boolean isValid(CredentialsReset credentialsReset) {
		if (credentialsReset == null || StringUtils.isBlank(credentialsReset.getCredentialsRequest())
				|| credentialsReset.getCredentialsRequestExpiry() == null) {
			return false;
		}
		return clock.millis() <= credentialsReset.getCredentialsRequestExpiry().getTime();
	}

	@Override
	public boolean isSecureLink(String link) {
		if (StringUtils.isBlank(link)) {
			return false;
		}
		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			return false;
		}
		if (uri.getScheme() == null || uri.getHost() == null) {
			return false;
		}
		if (!httpsRequired || HTTPS.equalsIgnoreCase(uri.getScheme())) {
			return true;
		}
		return LOOPBACK_HOSTS.contains(uri.getHost().toLowerCase());
	}

}
