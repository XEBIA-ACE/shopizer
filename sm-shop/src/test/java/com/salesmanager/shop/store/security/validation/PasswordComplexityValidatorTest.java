package com.salesmanager.shop.store.security.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordComplexityValidatorTest {

	private final PasswordComplexityValidator validator = new PasswordComplexityValidator();

	@Test
	void compliantPasswordHasNoViolations() {
		assertTrue(validator.validate("Str0ng!Passw0rd").isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = { "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "-", "=", "[", "]", "{", "}",
			"|", ";", "'", ":", "\"", ",", ".", "<", ">", "?", "/" })
	void everyListedSpecialCharacterIsAccepted(String special) {
		assertEquals(Collections.emptyList(), validator.validate("Abcdefghij1" + special));
	}

	@Test
	void tooShortPassword() {
		assertEquals(Collections.singletonList(PasswordComplexityValidator.MIN_LENGTH_MESSAGE),
				validator.validate("Abcdefgh1!x"));
	}

	@Test
	void exactlyMinimumLengthIsAccepted() {
		assertTrue(validator.validate("Abcdefghi1!x").isEmpty());
	}

	@Test
	void missingUppercase() {
		assertEquals(Collections.singletonList(PasswordComplexityValidator.UPPERCASE_MESSAGE),
				validator.validate("abcdefghij1!"));
	}

	@Test
	void missingLowercase() {
		assertEquals(Collections.singletonList(PasswordComplexityValidator.LOWERCASE_MESSAGE),
				validator.validate("ABCDEFGHIJ1!"));
	}

	@Test
	void missingDigit() {
		assertEquals(Collections.singletonList(PasswordComplexityValidator.DIGIT_MESSAGE),
				validator.validate("Abcdefghijk!"));
	}

	@Test
	void missingSpecialCharacter() {
		assertEquals(Collections.singletonList(PasswordComplexityValidator.SPECIAL_CHARACTER_MESSAGE),
				validator.validate("Abcdefghijk1"));
	}

	@Test
	void nonAsciiLettersDoNotSatisfyCaseRules() {
		List<String> violations = validator.validate("ÉÀÜéàü12345!");
		assertEquals(Arrays.asList(PasswordComplexityValidator.UPPERCASE_MESSAGE,
				PasswordComplexityValidator.LOWERCASE_MESSAGE), violations);
	}

	@Test
	void nullOrEmptyPasswordReportsEveryRuleInOrder() {
		List<String> all = Arrays.asList(PasswordComplexityValidator.MIN_LENGTH_MESSAGE,
				PasswordComplexityValidator.UPPERCASE_MESSAGE, PasswordComplexityValidator.LOWERCASE_MESSAGE,
				PasswordComplexityValidator.DIGIT_MESSAGE, PasswordComplexityValidator.SPECIAL_CHARACTER_MESSAGE);
		assertEquals(all, validator.validate(null));
		assertEquals(all, validator.validate(""));
	}

	@Test
	void clientRulesExposeServerRuleDefinitions() {
		Map<String, Object> rules = validator.clientRules();
		assertEquals(PasswordComplexityValidator.MIN_LENGTH, rules.get("minLength"));
		assertEquals(PasswordComplexityValidator.SPECIAL_CHARACTERS, rules.get("specialCharacters"));
		@SuppressWarnings("unchecked")
		Map<String, String> messages = (Map<String, String>) rules.get("messages");
		assertEquals(PasswordComplexityValidator.MIN_LENGTH_MESSAGE, messages.get("minLength"));
		assertEquals(PasswordComplexityValidator.SPECIAL_CHARACTER_MESSAGE, messages.get("special"));
	}
}
