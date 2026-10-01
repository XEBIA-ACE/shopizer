package com.salesmanager.shop.store.controller.sso;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

/**
 * Serves the SSO login entry point page shown to unauthenticated users of
 * protected storefront routes. The page only contains a call-to-action towards
 * the SSO provider and never any credential input.
 */
@Controller
public class SsoLoginController {

	public static final String PATH = "/sso-login";

	static final String TEMPLATE_LOCATION = "templates/sso-login.html";
	static final String DEFAULT_PROVIDER_URL = "#";

	private static final Logger LOGGER = LoggerFactory.getLogger(SsoLoginController.class);
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

	private final MessageSource messageSource;
	private final String providerAuthUrl;
	private final String template;

	public SsoLoginController(MessageSource messageSource,
			@Value("${sso.provider.auth-url:" + DEFAULT_PROVIDER_URL + "}") String providerAuthUrl) throws IOException {
		this.messageSource = messageSource;
		this.providerAuthUrl = sanitizeProviderUrl(providerAuthUrl);
		try (InputStream in = new ClassPathResource(TEMPLATE_LOCATION).getInputStream()) {
			this.template = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
		}
	}

	@GetMapping(value = PATH, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> ssoLogin(Locale locale) {
		Map<String, String> values = Map.of(
				"lang", locale.getLanguage(),
				"title", message("sso.login.entrypoint.title", locale),
				"label", message("sso.login.entrypoint.label", locale),
				"description", message("sso.login.entrypoint.description", locale),
				"cta", message("sso.login.entrypoint.cta", locale),
				"ssoUrl", providerAuthUrl);

		return ResponseEntity.ok()
				.contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
				.cacheControl(CacheControl.noStore())
				.body(render(values));
	}

	private String message(String key, Locale locale) {
		return messageSource.getMessage(key, null, locale);
	}

	private String render(Map<String, String> values) {
		Matcher matcher = PLACEHOLDER.matcher(template);
		StringBuilder html = new StringBuilder(template.length() + 256);
		while (matcher.find()) {
			String value = values.getOrDefault(matcher.group(1), "");
			matcher.appendReplacement(html, Matcher.quoteReplacement(HtmlUtils.htmlEscape(value, "UTF-8")));
		}
		matcher.appendTail(html);
		return html.toString();
	}

	static String sanitizeProviderUrl(String url) {
		if (url == null || url.isBlank()) {
			return DEFAULT_PROVIDER_URL;
		}
		String trimmed = url.trim();
		String lower = trimmed.toLowerCase(Locale.ROOT);
		if (trimmed.equals(DEFAULT_PROVIDER_URL) || lower.startsWith("https://") || lower.startsWith("http://")
				|| (trimmed.startsWith("/") && !trimmed.startsWith("//"))) {
			return trimmed;
		}
		LOGGER.warn("Ignoring unsupported sso.provider.auth-url scheme, falling back to placeholder");
		return DEFAULT_PROVIDER_URL;
	}
}
