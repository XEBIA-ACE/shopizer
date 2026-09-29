package com.salesmanager.shop.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.constants.Constants;

/**
 * Resolves the product description to display for a requested language.
 * Resolution order: requested language, store default language, English, first available.
 */
public final class ProductDescriptionUtils {

	private ProductDescriptionUtils() {
	}

	public static Optional<ProductDescription> resolve(Collection<ProductDescription> descriptions,
			Language language, MerchantStore store) {
		if (descriptions == null || descriptions.isEmpty()) {
			return Optional.empty();
		}
		Language storeDefault = store == null ? null : store.getDefaultLanguage();
		Optional<ProductDescription> description = find(descriptions, language);
		if (!description.isPresent()) {
			description = find(descriptions, storeDefault);
		}
		if (!description.isPresent()) {
			description = find(descriptions, new Language(Constants.DEFAULT_LANGUAGE));
		}
		if (!description.isPresent()) {
			description = descriptions.stream().filter(Objects::nonNull).findFirst();
		}
		return description;
	}

	static Optional<ProductDescription> find(Collection<ProductDescription> descriptions, Language language) {
		if (language == null) {
			return Optional.empty();
		}
		return descriptions.stream()
				.filter(Objects::nonNull)
				.filter(d -> sameLanguage(d.getLanguage(), language))
				.findFirst();
	}

	private static boolean sameLanguage(Language candidate, Language language) {
		if (candidate == null) {
			return false;
		}
		if (candidate.getId() != null && language.getId() != null) {
			return candidate.getId().intValue() == language.getId().intValue();
		}
		return candidate.getCode() != null && candidate.getCode().equalsIgnoreCase(language.getCode());
	}
}
