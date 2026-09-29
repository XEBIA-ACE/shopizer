package com.salesmanager.shop.filter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import com.salesmanager.core.business.utils.DisplayLocaleContext;

/**
 * Determines the locale requested by the client and exposes it through
 * {@link DisplayLocaleContext} so that prices and dates are presented in the
 * user's locale. The {@code locale} request parameter (e.g. {@code fr-CA})
 * takes precedence over the {@code Accept-Language} header. When neither is
 * supplied the store defaults are used.
 */
public class LocaleFormattingInterceptor implements HandlerInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleFormattingInterceptor.class);

	public static final String LOCALE_PARAMETER = "locale";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		Optional<Locale> locale = resolveLocale(request);
		DisplayLocaleContext.set(locale.orElse(null));
		locale.ifPresent(l -> response.setHeader(HttpHeaders.CONTENT_LANGUAGE, l.toLanguageTag()));
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		DisplayLocaleContext.clear();
	}

	static Optional<Locale> resolveLocale(HttpServletRequest request) {
		Optional<Locale> fromParameter = parseLanguageTag(request.getParameter(LOCALE_PARAMETER));
		if (fromParameter.isPresent()) {
			return fromParameter;
		}
		return parseAcceptLanguage(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
	}

	static Optional<Locale> parseLanguageTag(String tag) {
		if (StringUtils.isBlank(tag)) {
			return Optional.empty();
		}
		Locale locale = Locale.forLanguageTag(tag.trim().replace('_', '-'));
		return StringUtils.isBlank(locale.getLanguage()) ? Optional.empty() : Optional.of(locale);
	}

	static Optional<Locale> parseAcceptLanguage(String header) {
		if (StringUtils.isBlank(header)) {
			return Optional.empty();
		}
		try {
			List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(header);
			return ranges.stream()
					.filter(range -> range.getWeight() > 0 && !range.getRange().startsWith("*"))
					.map(range -> parseLanguageTag(range.getRange()))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findFirst();
		} catch (IllegalArgumentException e) {
			LOGGER.debug("Ignoring invalid Accept-Language header [{}]", header);
			return Optional.empty();
		}
	}

}
