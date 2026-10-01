package com.salesmanager.test.shop.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest
@ContextConfiguration(classes = SsoTestConfiguration.class)
@TestPropertySource(properties = "sso.provider.auth-url=https://idp.example.com/authorize?client=shop&scope=openid")
class SsoLoginPageTest {

	private static final String LABEL = "Sign in with SSO to continue";
	private static final String[] PROTECTED_PATHS = { "/cart", "/checkout", "/wishlist", "/address", "/orders" };
	private static final int CONCURRENT_USERS = 100;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void pageShowsApprovedLabelAndSsoCallToAction() throws Exception {
		String html = mockMvc.perform(get("/sso-login"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
				.andReturn().getResponse().getContentAsString();

		assertTrue(html.contains(LABEL));
		assertTrue(html.contains("href=\"https://idp.example.com/authorize?client=shop&amp;scope=openid\""));
		assertNoCredentialForm(html);
	}

	@Test
	void pageIsLocalised() throws Exception {
		String html = mockMvc.perform(get("/sso-login").locale(Locale.FRENCH))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		assertTrue(html.contains("lang=\"fr\""));
		assertTrue(html.contains("Connectez-vous avec le SSO pour continuer"));
		assertNoCredentialForm(html);
	}

	@Test
	void concurrentUnauthenticatedUsersAllGetSsoEntryPoint() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
		CountDownLatch start = new CountDownLatch(1);
		try {
			List<Future<String>> results = new ArrayList<>();
			for (int i = 0; i < CONCURRENT_USERS; i++) {
				String path = PROTECTED_PATHS[i % PROTECTED_PATHS.length] + "/" + i;
				results.add(executor.submit(followToEntryPoint(path, start)));
			}
			start.countDown();

			for (Future<String> result : results) {
				String html = result.get(30, TimeUnit.SECONDS);
				assertTrue(html.contains(LABEL));
				assertNoCredentialForm(html);
			}
		} finally {
			executor.shutdownNow();
		}
	}

	private Callable<String> followToEntryPoint(String path, CountDownLatch start) {
		return () -> {
			start.await();
			MvcResult redirect = mockMvc.perform(get(path)).andExpect(status().isFound()).andReturn();
			String location = redirect.getResponse().getRedirectedUrl();
			assertEquals("/sso-login", location);
			return mockMvc.perform(get(location)).andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();
		};
	}

	private static void assertNoCredentialForm(String html) {
		String lower = html.toLowerCase(Locale.ROOT);
		assertFalse(lower.contains("<form"));
		assertFalse(lower.contains("<input"));
		assertFalse(lower.contains("password"));
	}
}
