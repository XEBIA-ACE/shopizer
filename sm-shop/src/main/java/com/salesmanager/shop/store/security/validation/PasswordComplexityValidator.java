package com.salesmanager.shop.store.security.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Password complexity rules shared by the server-side registration endpoint and the client-side
 * registration form (see {@link #clientRules()}).
 */
@Component
public class PasswordComplexityValidator {

	public static final int MIN_LENGTH = 12;

	public static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;':\",.<>?/";

	public static final String MIN_LENGTH_MESSAGE = "Password must be at least " + MIN_LENGTH + " characters.";
	public static final String UPPERCASE_MESSAGE = "Password must contain at least one uppercase letter.";
	public static final String LOWERCASE_MESSAGE = "Password must contain at least one lowercase letter.";
	public static final String DIGIT_MESSAGE = "Password must contain at least one number.";
	public static final String SPECIAL_CHARACTER_MESSAGE = "Password must contain at least one special character.";

	/**
	 * @return the messages of every violated rule, in a stable order; empty when the password is valid
	 */
	public List<String> validate(String password) {
		String value = password == null ? "" : password;
		List<String> violations = new ArrayList<>();
		if (value.length() < MIN_LENGTH) {
			violations.add(MIN_LENGTH_MESSAGE);
		}
		if (value.chars().noneMatch(c -> c >= 'A' && c <= 'Z')) {
			violations.add(UPPERCASE_MESSAGE);
		}
		if (value.chars().noneMatch(c -> c >= 'a' && c <= 'z')) {
			violations.add(LOWERCASE_MESSAGE);
		}
		if (value.chars().noneMatch(c -> c >= '0' && c <= '9')) {
			violations.add(DIGIT_MESSAGE);
		}
		if (value.chars().noneMatch(c -> SPECIAL_CHARACTERS.indexOf(c) >= 0)) {
			violations.add(SPECIAL_CHARACTER_MESSAGE);
		}
		return violations;
	}

	/**
	 * Rule definitions rendered into the registration page so client-side validation cannot drift from
	 * {@link #validate(String)}.
	 */
	public Map<String, Object> clientRules() {
		Map<String, String> messages = new LinkedHashMap<>();
		messages.put("minLength", MIN_LENGTH_MESSAGE);
		messages.put("uppercase", UPPERCASE_MESSAGE);
		messages.put("lowercase", LOWERCASE_MESSAGE);
		messages.put("digit", DIGIT_MESSAGE);
		messages.put("special", SPECIAL_CHARACTER_MESSAGE);

		Map<String, Object> rules = new LinkedHashMap<>();
		rules.put("minLength", MIN_LENGTH);
		rules.put("specialCharacters", SPECIAL_CHARACTERS);
		rules.put("messages", Collections.unmodifiableMap(messages));
		return Collections.unmodifiableMap(rules);
	}
}
