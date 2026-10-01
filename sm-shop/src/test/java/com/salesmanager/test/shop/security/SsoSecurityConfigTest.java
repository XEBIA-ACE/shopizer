package com.salesmanager.test.shop.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = SsoTestConfiguration.class)
class SsoSecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	static Stream<Arguments> protectedRoutes() {
		String[] paths = { "/cart", "/cart/42", "/checkout", "/checkout/payment", "/wishlist", "/wishlist/items",
				"/address", "/address/1", "/orders", "/orders/2024/details" };
		HttpMethod[] methods = { HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE };
		return Stream.of(paths).flatMap(path -> Stream.of(methods).map(method -> Arguments.of(method, path)));
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("protectedRoutes")
	void unauthenticatedRequestIsRedirectedToSsoLogin(HttpMethod method, String path) throws Exception {
		mockMvc.perform(request(method, path))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/sso-login"));
	}

	@Test
	@WithMockUser
	void authenticatedRequestIsNotRedirected() throws Exception {
		mockMvc.perform(request(HttpMethod.GET, "/cart"))
				.andExpect(status().isNotFound());
	}

	@Test
	void unprotectedRouteIsNotIntercepted() throws Exception {
		mockMvc.perform(request(HttpMethod.GET, "/api/v1/products"))
				.andExpect(status().isNotFound());
	}

	@Test
	void ssoLoginPageIsPublic() throws Exception {
		mockMvc.perform(request(HttpMethod.GET, "/sso-login"))
				.andExpect(status().isOk());
	}
}
