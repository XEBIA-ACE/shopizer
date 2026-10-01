package com.salesmanager.shop.store.controller.customer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.customer.CustomerRegistrationRequest;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.address.Address;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.security.validation.PasswordComplexityValidator;

import springfox.documentation.annotations.ApiIgnore;

/**
 * Storefront self-service registration page (email + password).
 */
@Controller
public class CustomerRegistrationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerRegistrationController.class);

	static final String TEMPLATE = "templates/register.html";
	static final String RULES_PLACEHOLDER = "__REGISTRATION_RULES__";

	public static final String SUCCESS_MESSAGE = "Your account has been created successfully.";
	public static final String GENERIC_ERROR_MESSAGE = "We were unable to complete your registration. Please try again or contact support.";

	private static final String FIELD_EMAIL = "emailAddress";
	private static final String FIELD_PASSWORD = "password";
	private static final int MAX_NAME_LENGTH = 64;
	private static final Pattern EMAIL_PATTERN = Pattern.compile(CustomerRegistrationRequest.EMAIL_PATTERN);

	private final CustomerFacade customerFacade;
	private final PasswordComplexityValidator passwordComplexityValidator;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public CustomerRegistrationController(CustomerFacade customerFacade,
			PasswordComplexityValidator passwordComplexityValidator) {
		this.customerFacade = customerFacade;
		this.passwordComplexityValidator = passwordComplexityValidator;
	}

	@GetMapping(value = "/register", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public ResponseEntity<String> registrationForm() throws IOException {
		String template;
		try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream()) {
			template = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
		}
		return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(template.replace(RULES_PLACEHOLDER, rulesJson()));
	}

	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> register(@RequestBody CustomerRegistrationRequest request,
			@ApiIgnore MerchantStore merchantStore, @ApiIgnore Language language) {

		Map<String, List<String>> fieldErrors = validate(request);
		if (!fieldErrors.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
					.body(Collections.singletonMap("fieldErrors", fieldErrors));
		}

		String email = request.getEmailAddress().trim();
		try {
			if (customerFacade.checkIfUserExists(email, merchantStore)) {
				return genericError();
			}
			customerFacade.registerCustomer(toPersistableCustomer(email, request.getPassword(), merchantStore, language),
					merchantStore, language);
		} catch (Exception e) {
			LOGGER.warn("Customer registration failed for store {}: {}", merchantStore.getCode(), e.getClass().getName());
			return genericError();
		}

		return ResponseEntity.ok(Collections.singletonMap("message", SUCCESS_MESSAGE));
	}

	private Map<String, List<String>> validate(CustomerRegistrationRequest request) {
		Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

		String email = StringUtils.trimToEmpty(request.getEmailAddress());
		if (email.length() > CustomerRegistrationRequest.EMAIL_MAX_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
			fieldErrors.put(FIELD_EMAIL, Collections.singletonList(CustomerRegistrationRequest.INVALID_EMAIL_MESSAGE));
		}

		List<String> passwordErrors = passwordComplexityValidator.validate(request.getPassword());
		if (!passwordErrors.isEmpty()) {
			fieldErrors.put(FIELD_PASSWORD, passwordErrors);
		}
		return fieldErrors;
	}

	private PersistableCustomer toPersistableCustomer(String email, String password, MerchantStore store,
			Language language) {
		String name = StringUtils.left(StringUtils.substringBefore(email, "@"), MAX_NAME_LENGTH);

		Address billing = new Address();
		billing.setFirstName(name);
		billing.setLastName(name);
		billing.setCountry(store.getCountry().getIsoCode());

		PersistableCustomer customer = new PersistableCustomer();
		customer.setEmailAddress(email);
		customer.setUserName(email);
		customer.setPassword(password);
		customer.setStoreCode(store.getCode());
		customer.setLanguage(language.getCode());
		customer.setBilling(billing);
		return customer;
	}

	private ResponseEntity<Map<String, Object>> genericError() {
		return ResponseEntity.badRequest().body(Collections.singletonMap("message", GENERIC_ERROR_MESSAGE));
	}

	private String rulesJson() throws JsonProcessingException {
		Map<String, Object> rules = new LinkedHashMap<>();
		rules.put("emailPattern", CustomerRegistrationRequest.EMAIL_PATTERN);
		rules.put("emailMaxLength", CustomerRegistrationRequest.EMAIL_MAX_LENGTH);
		rules.put("emailMessage", CustomerRegistrationRequest.INVALID_EMAIL_MESSAGE);
		rules.put("password", passwordComplexityValidator.clientRules());
		rules.put("genericErrorMessage", GENERIC_ERROR_MESSAGE);
		return objectMapper.writeValueAsString(rules)
				.replace("<", "\\u003c")
				.replace(">", "\\u003e")
				.replace("&", "\\u0026");
	}
}
