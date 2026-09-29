package com.salesmanager.core.business.utils;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Holds the locale requested by the client for the current thread (request).
 * Used to format prices and dates in the user's locale when presenting data.
 */
public final class DisplayLocaleContext {

	private static final ThreadLocal<Locale> DISPLAY_LOCALE = new ThreadLocal<>();

	private DisplayLocaleContext() {
	}

	public static void set(Locale locale) {
		if (locale == null) {
			DISPLAY_LOCALE.remove();
		} else {
			DISPLAY_LOCALE.set(locale);
		}
	}

	public static Optional<Locale> get() {
		return Optional.ofNullable(DISPLAY_LOCALE.get());
	}

	public static void clear() {
		DISPLAY_LOCALE.remove();
	}

	/**
	 * Returns the display locale for the current request. When the requested
	 * locale has no region, the store country is used as region so that
	 * national currency formats remain consistent with the store.
	 */
	public static Optional<Locale> resolve(MerchantStore store) {
		return get().map(locale -> {
			if (StringUtils.isBlank(locale.getCountry()) && store != null && store.getCountry() != null
					&& StringUtils.isNotBlank(store.getCountry().getIsoCode())) {
				return new Locale(locale.getLanguage(), store.getCountry().getIsoCode());
			}
			return locale;
		});
	}

}
