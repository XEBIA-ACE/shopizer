package com.salesmanager.core.business.services.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.salesmanager.core.model.common.CredentialsReset;

public class PasswordResetTokenServiceImplTest {

	private static final String SECRET = "test-secret";
	private static final long EXPIRATION_SECONDS = 3600;
	private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

	private MutableClock clock;
	private PasswordResetTokenServiceImpl service;

	@Before
	public void setUp() {
		clock = new MutableClock(NOW);
		service = new PasswordResetTokenServiceImpl(SECRET, EXPIRATION_SECONDS, true, clock);
	}

	@Test
	public void generateToken_isUrlSafeAndUnique() {
		Set<String> tokens = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			String token = service.generateToken();
			assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
			tokens.add(token);
		}
		assertEquals(100, tokens.size());
	}

	@Test
	public void hashToken_isDeterministicHmacAndDiffersFromToken() {
		String token = service.generateToken();
		String hash = service.hashToken(token);

		assertEquals(hash, service.hashToken(token));
		assertTrue(hash.matches("[0-9a-f]{64}"));
		assertNotEquals(token, hash);
		assertNotEquals(hash, service.hashToken(service.generateToken()));
	}

	@Test
	public void hashToken_dependsOnSecret() {
		PasswordResetTokenServiceImpl other = new PasswordResetTokenServiceImpl("other-secret", EXPIRATION_SECONDS,
				true, clock);
		String token = service.generateToken();
		assertNotEquals(service.hashToken(token), other.hashToken(token));
	}

	@Test
	public void hashToken_matchesKnownHmacSha256Vector() {
		PasswordResetTokenServiceImpl rfc = new PasswordResetTokenServiceImpl("key", EXPIRATION_SECONDS, true, clock);
		assertEquals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8",
				rfc.hashToken("The quick brown fox jumps over the lazy dog"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void hashToken_rejectsBlankToken() {
		service.hashToken(" ");
	}

	@Test
	public void createCredentialsReset_storesHashAndConfiguredExpiry() {
		String token = service.generateToken();
		CredentialsReset reset = service.createCredentialsReset(token);

		assertEquals(service.hashToken(token), reset.getCredentialsRequest());
		assertEquals(Date.from(NOW.plusSeconds(EXPIRATION_SECONDS)), reset.getCredentialsRequestExpiry());
	}

	@Test
	public void isValid_untilExpiryThenExpired() {
		CredentialsReset reset = service.createCredentialsReset(service.generateToken());
		assertTrue(service.isValid(reset));

		clock.setInstant(NOW.plusSeconds(EXPIRATION_SECONDS));
		assertTrue(service.isValid(reset));

		clock.setInstant(NOW.plusSeconds(EXPIRATION_SECONDS + 1));
		assertFalse(service.isValid(reset));
	}

	@Test
	public void isValid_falseWhenMissingOrInvalidated() {
		assertFalse(service.isValid(null));

		CredentialsReset noToken = service.createCredentialsReset(service.generateToken());
		noToken.setCredentialsRequest(null);
		assertFalse(service.isValid(noToken));

		CredentialsReset noExpiry = service.createCredentialsReset(service.generateToken());
		noExpiry.setCredentialsRequestExpiry(null);
		assertFalse(service.isValid(noExpiry));
	}

	@Test
	public void isSecureLink_requiresHttpsExceptLoopback() {
		assertTrue(service.isSecureLink("https://shop.example.com/customer/DEFAULT/reset/abc"));
		assertTrue(service.isSecureLink("HTTPS://shop.example.com/reset"));
		assertTrue(service.isSecureLink("http://localhost:8080/customer/DEFAULT/reset/abc"));
		assertTrue(service.isSecureLink("http://127.0.0.1/reset"));

		assertFalse(service.isSecureLink("http://shop.example.com/customer/DEFAULT/reset/abc"));
		assertFalse(service.isSecureLink("ftp://shop.example.com/reset"));
		assertFalse(service.isSecureLink("shop.example.com/reset"));
		assertFalse(service.isSecureLink("not a url"));
		assertFalse(service.isSecureLink(null));
		assertFalse(service.isSecureLink(""));
	}

	@Test
	public void isSecureLink_allowsHttpWhenNotRequired() {
		PasswordResetTokenServiceImpl lenient = new PasswordResetTokenServiceImpl(SECRET, EXPIRATION_SECONDS, false,
				clock);
		assertTrue(lenient.isSecureLink("http://shop.example.com/reset"));
		assertFalse(lenient.isSecureLink("shop.example.com/reset"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_rejectsBlankSecret() {
		new PasswordResetTokenServiceImpl("", EXPIRATION_SECONDS, true, clock);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_rejectsNonPositiveExpiration() {
		new PasswordResetTokenServiceImpl(SECRET, 0, true, clock);
	}

	private static class MutableClock extends Clock {

		private Instant instant;

		MutableClock(Instant instant) {
			this.instant = instant;
		}

		void setInstant(Instant instant) {
			this.instant = instant;
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}

	}

}
