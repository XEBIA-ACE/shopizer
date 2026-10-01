package com.salesmanager.shop.model.customer;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Self-service storefront registration payload (email + password only).
 */
public class CustomerRegistrationRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Practical RFC 5322 address format (requires a dotted domain). Kept compatible with both Java and
	 * JavaScript regular expression syntax so the same pattern is enforced client-side.
	 */
	public static final String EMAIL_PATTERN = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";

	/** Matches the CUSTOMER_EMAIL_ADDRESS column length. */
	public static final int EMAIL_MAX_LENGTH = 96;

	public static final String INVALID_EMAIL_MESSAGE = "Please enter a valid email address.";

	@NotBlank(message = INVALID_EMAIL_MESSAGE)
	@Size(max = EMAIL_MAX_LENGTH, message = INVALID_EMAIL_MESSAGE)
	@Email(regexp = EMAIL_PATTERN, message = INVALID_EMAIL_MESSAGE)
	private String emailAddress;

	private String password;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "CustomerRegistrationRequest[emailAddress=" + emailAddress + "]";
	}
}
