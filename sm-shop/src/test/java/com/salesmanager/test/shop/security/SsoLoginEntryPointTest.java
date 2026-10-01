package com.salesmanager.test.shop.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import com.salesmanager.shop.store.security.SsoLoginEntryPoint;

class SsoLoginEntryPointTest {

	private final SsoLoginEntryPoint entryPoint = new SsoLoginEntryPoint("/sso-login");

	@Test
	void redirectsToSsoLoginPage() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/cart/123");
		MockHttpServletResponse response = new MockHttpServletResponse();

		entryPoint.commence(request, response, new InsufficientAuthenticationException("unauthenticated"));

		assertEquals(HttpStatus.FOUND.value(), response.getStatus());
		assertEquals("/sso-login", response.getRedirectedUrl());
	}

	@Test
	void redirectHonoursContextPath() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/shopizer/checkout");
		request.setContextPath("/shopizer");
		MockHttpServletResponse response = new MockHttpServletResponse();

		entryPoint.commence(request, response, new InsufficientAuthenticationException("unauthenticated"));

		assertEquals("/shopizer/sso-login", response.getRedirectedUrl());
	}

	@Test
	void rejectsInvalidLoginPath() {
		assertThrows(IllegalArgumentException.class, () -> new SsoLoginEntryPoint(""));
		assertThrows(IllegalArgumentException.class, () -> new SsoLoginEntryPoint("sso-login"));
	}
}
