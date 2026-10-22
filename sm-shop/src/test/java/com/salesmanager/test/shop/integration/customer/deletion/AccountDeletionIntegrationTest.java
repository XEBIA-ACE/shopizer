package com.salesmanager.test.shop.integration.customer.deletion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.model.customer.CustomerGender;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.model.customer.address.Address;
import com.salesmanager.shop.model.customer.deletion.ReadableAccountDeletionGuidance;
import com.salesmanager.shop.populator.customer.ReadableCustomerList;
import com.salesmanager.shop.store.controller.customer.deletion.AccountDeletionAuditService;
import com.salesmanager.shop.store.security.AuthenticationRequest;
import com.salesmanager.shop.store.security.AuthenticationResponse;
import com.salesmanager.shop.store.api.v1.customer.AccountDeletionApi;
import com.salesmanager.test.shop.common.ServicesTestSupport;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * US-004 guided account deletion (T007-T018). The Profile page UI is rendered by the separate
 * storefront; these tests characterize the backend contract it relies on.
 */
@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class AccountDeletionIntegrationTest extends ServicesTestSupport {

	static final String VIDEO_URL = "https://videos.example.com/account-deletion";
	private static final String PASSWORD = "clear123";

	@Autowired
	private AccountDeletionApi accountDeletionApi;

	private final Logger auditLogger = (Logger) LoggerFactory.getLogger(AccountDeletionAuditService.AUDIT_LOGGER_NAME);
	private final ListAppender<ILoggingEvent> auditEvents = new ListAppender<>();

	/**
	 * Shares the application context with the other integration tests (a second context would
	 * clash on the ehcache manager), so the configured video URL is injected on the bean and
	 * audit events are captured from the dedicated audit logger.
	 */
	@Before
	public void setUp() {
		ReflectionTestUtils.setField((Object) AopTestUtils.getTargetObject(accountDeletionApi), "videoUrl", VIDEO_URL);
		auditEvents.start();
		auditLogger.addAppender(auditEvents);
	}

	@After
	public void tearDown() {
		auditLogger.detachAppender(auditEvents);
		ReflectionTestUtils.setField((Object) AopTestUtils.getTargetObject(accountDeletionApi), "videoUrl", "");
	}

	private List<String> auditedEvents() {
		return auditEvents.list.stream().map(ILoggingEvent::getFormattedMessage)
				.map(m -> m.replaceAll(".*event=(\\w+).*", "$1")).collect(Collectors.toList());
	}

	@Test
	public void us004ac1_ac2_guidanceExposesClearlyLabeledInstructionalVideo() {
		HttpHeaders headers = registerAndLogin("deletion.video@test.com");

		ResponseEntity<ReadableAccountDeletionGuidance> response = testRestTemplate.exchange(
				"/api/v1/auth/customer/deletion?lang=en", HttpMethod.GET, new HttpEntity<>(headers),
				ReadableAccountDeletionGuidance.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ReadableAccountDeletionGuidance guidance = response.getBody();
		assertNotNull(guidance);
		// US-004-ac-1: entry is part of the Account Actions area and labeled as account deletion related
		assertEquals("Account Actions", guidance.getSectionTitle());
		assertTrue(guidance.getVideoLabel().toLowerCase().contains("account deletion"));
		assertTrue(guidance.getVideoDescription().toLowerCase().contains("consequences"));
		// US-004-ac-2: entry is directly available and links to the stakeholder-provided video
		assertTrue(guidance.isVideoAvailable());
		assertEquals(VIDEO_URL, guidance.getVideoUrl());
		// viewing guidance alone is not a deletion initiation
		assertTrue(auditedEvents().isEmpty());
	}

	@Test
	public void us004ac1_guidanceRequiresAuthenticatedCustomer() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		ResponseEntity<String> response = testRestTemplate.exchange("/api/v1/auth/customer/deletion",
				HttpMethod.GET, new HttpEntity<>(headers), String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void us004ac3_ac4_initiateShowsPermanentNoticeAndCancelKeepsAccountActive() {
		String email = "deletion.cancel@test.com";
		HttpHeaders headers = registerAndLogin(email);

		// US-004-ac-3: initiating the request returns the confirmation dialog content
		ResponseEntity<ReadableAccountDeletionGuidance> initiate = testRestTemplate.exchange(
				"/api/v1/auth/customer/deletion/initiate?lang=en", HttpMethod.POST, new HttpEntity<>(headers),
				ReadableAccountDeletionGuidance.class);
		assertEquals(HttpStatus.OK, initiate.getStatusCode());
		ReadableAccountDeletionGuidance dialog = initiate.getBody();
		assertNotNull(dialog);
		assertTrue(dialog.getDialogPermanentNotice().toLowerCase().contains("permanent"));
		assertFalse(dialog.getDialogConsequences().isEmpty());
		assertEquals("Cancel", dialog.getDialogCancelLabel());
		assertNotNull(dialog.getDialogConfirmLabel());
		assertEquals("Delete Account", dialog.getDeleteAccountLabel());

		// US-004-ac-4: cancel applies no change, account remains active
		ResponseEntity<String> cancel = testRestTemplate.exchange("/api/v1/auth/customer/deletion/cancel",
				HttpMethod.POST, new HttpEntity<>(headers), String.class);
		assertEquals(HttpStatus.OK, cancel.getStatusCode());

		ResponseEntity<ReadableCustomer> profile = testRestTemplate.exchange("/api/v1/auth/customer/profile",
				HttpMethod.GET, new HttpEntity<>(headers), ReadableCustomer.class);
		assertEquals(HttpStatus.OK, profile.getStatusCode());
		assertEquals(email, profile.getBody().getEmailAddress());
		assertEquals(HttpStatus.OK, login(email).getStatusCode());

		assertEquals(Arrays.asList("INITIATED", "CANCELLED"), auditedEvents());
	}

	@Test
	public void us004ac5_ac6_confirmDeletesAccountWithExistingBehaviorAndAuditsFlow() {
		String email = "deletion.confirm@test.com";
		HttpHeaders headers = registerAndLogin(email);

		assertEquals(HttpStatus.OK, testRestTemplate.exchange("/api/v1/auth/customer/deletion/initiate",
				HttpMethod.POST, new HttpEntity<>(headers), ReadableAccountDeletionGuidance.class).getStatusCode());

		// US-004-ac-5: confirm invokes the existing Delete Account endpoint
		ResponseEntity<String> delete = testRestTemplate.exchange("/api/v1/auth/customer/", HttpMethod.DELETE,
				new HttpEntity<>(headers), String.class);
		assertEquals(HttpStatus.OK, delete.getStatusCode());

		assertNotEquals(HttpStatus.OK, login(email).getStatusCode());
		ReadableCustomerList customers = fetchCustomers();
		assertFalse(customers.getCustomers().stream().anyMatch(c -> email.equals(c.getEmailAddress())));

		// US-004-ac-6: initiation, confirmation and completion are audited in order
		assertEquals(Arrays.asList("INITIATED", "CONFIRMED", "COMPLETED"), auditedEvents());
	}

	private ResponseEntity<AuthenticationResponse> login(String email) {
		return testRestTemplate.postForEntity("/api/v1/customer/login",
				new HttpEntity<>(new AuthenticationRequest(email, PASSWORD)), AuthenticationResponse.class);
	}

	private HttpHeaders registerAndLogin(String email) {
		PersistableCustomer customer = new PersistableCustomer();
		customer.setEmailAddress(email);
		customer.setPassword(PASSWORD);
		customer.setGender(CustomerGender.M.name());
		customer.setLanguage("en");
		Address billing = new Address();
		billing.setFirstName("deletion");
		billing.setLastName("customer");
		billing.setCountry("BE");
		customer.setBilling(billing);
		customer.setStoreCode(Constants.DEFAULT_STORE);
		ResponseEntity<PersistableCustomer> registration = testRestTemplate.postForEntity("/api/v1/customer/register",
				new HttpEntity<>(customer, getHeader()), PersistableCustomer.class);
		assertEquals(HttpStatus.OK, registration.getStatusCode());

		ResponseEntity<AuthenticationResponse> login = login(email);
		assertEquals(HttpStatus.OK, login.getStatusCode());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
		headers.add("Authorization", "Bearer " + login.getBody().getToken());
		return headers;
	}

}
