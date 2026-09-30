package com.salesmanager.test.shop.integration.customer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerGender;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.address.Address;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CustomerRegistrationPasswordStorageIntegrationTest extends ServicesTestSupport {

	private static final String EMAIL = "password.storage@test.com";
	private static final String RAW_PASSWORD = "Clear-Pass-987";

	@Inject
	private CustomerService customerService;

	@Inject
	private PasswordEncoder passwordEncoder;

	@Test
	public void registrationStoresOnlyHashedPasswordAndNeverEchoesIt() {
		final PersistableCustomer testCustomer = new PersistableCustomer();
		testCustomer.setEmailAddress(EMAIL);
		testCustomer.setPassword(RAW_PASSWORD);
		testCustomer.setGender(CustomerGender.F.name());
		testCustomer.setLanguage("en");
		final Address billing = new Address();
		billing.setFirstName("password");
		billing.setLastName("storage");
		billing.setCountry("BE");
		testCustomer.setBilling(billing);
		testCustomer.setStoreCode(Constants.DEFAULT_STORE);

		final ResponseEntity<String> response = testRestTemplate.postForEntity("/api/v1/customer/register",
				new HttpEntity<>(testCustomer, getHeader()), String.class);
		assertThat(response.getStatusCode(), is(OK));
		assertNotNull(response.getBody());
		assertFalse(response.getBody().contains(RAW_PASSWORD));
		assertFalse(response.getBody().toLowerCase().contains("password"));

		final Customer stored = customerService.getByNick(EMAIL, Constants.DEFAULT_STORE);
		assertNotNull(stored);
		assertNotEquals(RAW_PASSWORD, stored.getPassword());
		assertTrue(stored.getPassword().startsWith("$2a$"));
		assertTrue(passwordEncoder.matches(RAW_PASSWORD, stored.getPassword()));
		assertFalse(response.getBody().contains(stored.getPassword()));
	}

}
