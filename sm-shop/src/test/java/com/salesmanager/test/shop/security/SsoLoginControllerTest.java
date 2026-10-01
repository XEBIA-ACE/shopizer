package com.salesmanager.test.shop.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.salesmanager.shop.store.controller.sso.SsoLoginController;

class SsoLoginControllerTest {

	private static StaticMessageSource messages(String label) {
		StaticMessageSource source = new StaticMessageSource();
		source.addMessage("sso.login.entrypoint.title", Locale.ENGLISH, "Sign in");
		source.addMessage("sso.login.entrypoint.label", Locale.ENGLISH, label);
		source.addMessage("sso.login.entrypoint.description", Locale.ENGLISH, "Description");
		source.addMessage("sso.login.entrypoint.cta", Locale.ENGLISH, "Continue with SSO");
		return source;
	}

	@Test
	void rendersEscapedLabelAndConfiguredUrl() throws Exception {
		SsoLoginController controller = new SsoLoginController(messages("<b>Approved</b> {{ssoUrl}}"),
				"https://idp.example.com/auth");

		ResponseEntity<String> response = controller.ssoLogin(Locale.ENGLISH);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		String html = response.getBody();
		assertTrue(html.contains("&lt;b&gt;Approved&lt;/b&gt; {{ssoUrl}}"));
		assertTrue(html.contains("href=\"https://idp.example.com/auth\""));
		assertFalse(html.contains("<b>"));
	}

	@Test
	void unsafeOrMissingUrlFallsBackToPlaceholder() throws Exception {
		for (String url : new String[] { "javascript:alert(1)", "//evil.example.com", "", null }) {
			String html = new SsoLoginController(messages("Label"), url).ssoLogin(Locale.ENGLISH).getBody();
			assertTrue(html.contains("href=\"#\""), "url: " + url);
		}
	}
}
