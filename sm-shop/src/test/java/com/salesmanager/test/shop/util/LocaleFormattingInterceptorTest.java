package com.salesmanager.test.shop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.salesmanager.core.business.utils.DisplayLocaleContext;
import com.salesmanager.shop.filter.LocaleFormattingInterceptor;

public class LocaleFormattingInterceptorTest {

	private final LocaleFormattingInterceptor interceptor = new LocaleFormattingInterceptor();

	@After
	public void tearDown() {
		DisplayLocaleContext.clear();
	}

	@Test
	public void noLocaleRequestedKeepsStoreDefaults() {
		MockHttpServletResponse response = handle(new MockHttpServletRequest());

		assertFalse(DisplayLocaleContext.get().isPresent());
		assertNull(response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
	}

	@Test
	public void acceptLanguageHeaderSetsDisplayLocale() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr-CA,fr;q=0.9,en;q=0.8");

		MockHttpServletResponse response = handle(request);

		assertEquals(Locale.CANADA_FRENCH, DisplayLocaleContext.get().get());
		assertEquals("fr-CA", response.getHeader(HttpHeaders.CONTENT_LANGUAGE));
	}

	@Test
	public void wildcardAcceptLanguageIsSkipped() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "*;q=1, de;q=0.5");

		handle(request);

		assertEquals(Locale.GERMAN, DisplayLocaleContext.get().get());
	}

	@Test
	public void localeParameterTakesPrecedenceOverHeader() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US");
		request.setParameter(LocaleFormattingInterceptor.LOCALE_PARAMETER, "de_DE");

		handle(request);

		assertEquals(Locale.GERMANY, DisplayLocaleContext.get().get());
	}

	@Test
	public void invalidHeaderIsIgnored() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "not a valid ;;; header");

		handle(request);

		assertFalse(DisplayLocaleContext.get().isPresent());
	}

	@Test
	public void afterCompletionClearsDisplayLocale() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "fr");
		MockHttpServletResponse response = handle(request);

		interceptor.afterCompletion(request, response, new Object(), null);

		assertFalse(DisplayLocaleContext.get().isPresent());
	}

	private MockHttpServletResponse handle(MockHttpServletRequest request) {
		MockHttpServletResponse response = new MockHttpServletResponse();
		interceptor.preHandle(request, response, new Object());
		return response;
	}

}
