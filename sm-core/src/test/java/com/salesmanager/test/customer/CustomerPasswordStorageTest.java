package com.salesmanager.test.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.configuration.PasswordEncoderConfiguration;
import com.salesmanager.core.business.exception.PasswordHashingException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.customer.CustomerRepository;
import com.salesmanager.core.business.services.customer.CustomerServiceImpl;
import com.salesmanager.core.model.customer.Customer;

public class CustomerPasswordStorageTest {

	private static final String RAW_PASSWORD = "clear-Secret-123";

	private CustomerRepository customerRepository;
	private PasswordEncoder passwordEncoder;
	private CustomerServiceImpl customerService;

	@Before
	public void setUp() {
		customerRepository = mock(CustomerRepository.class);
		passwordEncoder = PasswordEncoderConfiguration.createPasswordEncoder("bcrypt", 4);
		customerService = new CustomerServiceImpl(customerRepository);
		ReflectionTestUtils.setField(customerService, "passwordEncoder", passwordEncoder);
	}

	@Test
	public void registerCustomerPersistsOnlyBcryptHash() throws Exception {
		Customer customer = new Customer();
		customer.setNick("customer@test.com");

		customerService.registerCustomer(customer, RAW_PASSWORD);

		ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
		verify(customerRepository).saveAndFlush(saved.capture());
		String stored = saved.getValue().getPassword();
		assertNotEquals(RAW_PASSWORD, stored);
		assertFalse(stored.contains(RAW_PASSWORD));
		assertTrue(stored.startsWith("$2a$04$"));
		assertTrue(passwordEncoder.matches(RAW_PASSWORD, stored));
	}

	@Test
	public void registerCustomerReplacesAnyPreviouslySetPassword() throws Exception {
		Customer customer = new Customer();
		customer.setPassword(RAW_PASSWORD);

		customerService.registerCustomer(customer, RAW_PASSWORD);

		assertNotEquals(RAW_PASSWORD, customer.getPassword());
		assertTrue(passwordEncoder.matches(RAW_PASSWORD, customer.getPassword()));
	}

	@Test
	public void hashingFailureRejectsRegistrationWithoutPersisting() throws Exception {
		PasswordEncoder failing = mock(PasswordEncoder.class);
		when(failing.encode(any())).thenThrow(new IllegalStateException("boom"));
		ReflectionTestUtils.setField(customerService, "passwordEncoder", failing);
		Customer customer = new Customer();

		try {
			customerService.registerCustomer(customer, RAW_PASSWORD);
			fail("Expected PasswordHashingException");
		} catch (PasswordHashingException expected) {
			// expected
		}

		verify(customerRepository, never()).saveAndFlush(any());
		verify(customerRepository, never()).save(any());
		assertEquals(null, customer.getPassword());
	}

	@Test
	public void encoderReturningPlainTextIsRejected() throws Exception {
		PasswordEncoder identity = mock(PasswordEncoder.class);
		when(identity.encode(any())).thenReturn(RAW_PASSWORD);
		ReflectionTestUtils.setField(customerService, "passwordEncoder", identity);

		try {
			customerService.registerCustomer(new Customer(), RAW_PASSWORD);
			fail("Expected PasswordHashingException");
		} catch (PasswordHashingException expected) {
			// expected
		}
		verify(customerRepository, never()).saveAndFlush(any());
	}

	@Test
	public void blankPasswordIsRejected() {
		try {
			customerService.registerCustomer(new Customer(), " ");
			fail("Expected ServiceException");
		} catch (ServiceException expected) {
			assertFalse(expected instanceof PasswordHashingException);
		}
		verify(customerRepository, never()).saveAndFlush(any());
	}

	@Test
	public void onlyBcryptAlgorithmIsAllowed() {
		assertTrue(PasswordEncoderConfiguration.createPasswordEncoder("BCrypt", 4).encode("x").startsWith("$2a$04$"));
		for (String algorithm : new String[] { "md5", "sha256", "noop", "", null }) {
			try {
				PasswordEncoderConfiguration.createPasswordEncoder(algorithm, 10);
				fail("Algorithm " + algorithm + " should be rejected");
			} catch (IllegalStateException expected) {
				// expected
			}
		}
	}

	@Test
	public void customerSerializationAndToStringNeverExposePassword() throws Exception {
		Customer customer = new Customer();
		customer.setId(42L);
		customer.setNick("customer@test.com");
		customer.setPassword(passwordEncoder.encode(RAW_PASSWORD));

		String json = new ObjectMapper().writeValueAsString(customer);

		assertFalse(customer.toString().contains(customer.getPassword()));
		assertFalse(json.contains(customer.getPassword()));
		assertFalse(json.contains("\"password\""));
	}

}
