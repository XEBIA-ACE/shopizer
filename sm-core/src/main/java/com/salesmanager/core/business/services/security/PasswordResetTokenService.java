package com.salesmanager.core.business.services.security;

import com.salesmanager.core.model.common.CredentialsReset;

/**
 * Issues and validates password reset tokens.
 *
 * The raw token is only ever sent to the account owner (reset link). Only its
 * HMAC-SHA256 digest is persisted, so a leaked database cannot be used to
 * reset passwords.
 */
public interface PasswordResetTokenService {

	/**
	 * @return a new cryptographically random, URL safe token
	 */
	String generateToken();

	/**
	 * @return the HMAC-SHA256 hex digest of the token, as persisted
	 */
	String hashToken(String token);

	/**
	 * @return a reset request holding the token digest and its expiry date
	 */
	CredentialsReset createCredentialsReset(String token);

	/**
	 * @return true when the reset request exists, has an expiry date and is not expired
	 */
	boolean isValid(CredentialsReset credentialsReset);

	/**
	 * @return true when the link can be used to transmit a reset token
	 *         (HTTPS, or loopback host when HTTPS is required)
	 */
	boolean isSecureLink(String link);

}
