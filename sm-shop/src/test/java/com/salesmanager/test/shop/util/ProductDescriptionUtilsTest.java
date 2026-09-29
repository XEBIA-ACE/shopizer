package com.salesmanager.test.shop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.utils.ProductDescriptionUtils;

public class ProductDescriptionUtilsTest {

	private static Language language(int id, String code) {
		Language language = new Language(code);
		language.setId(id);
		return language;
	}

	private static ProductDescription description(Language language, String name) {
		ProductDescription description = new ProductDescription();
		description.setLanguage(language);
		description.setName(name);
		return description;
	}

	private static MerchantStore store(Language defaultLanguage) {
		MerchantStore store = new MerchantStore();
		store.setDefaultLanguage(defaultLanguage);
		return store;
	}

	private final Language en = language(1, "en");
	private final Language fr = language(2, "fr");
	private final Language es = language(3, "es");
	private final Language de = language(4, "de");

	@Test
	public void returnsRequestedLanguageWhenTranslationExists() {
		Set<ProductDescription> descriptions = new LinkedHashSet<>(
				Arrays.asList(description(en, "Chair"), description(fr, "Chaise")));

		assertEquals("Chaise",
				ProductDescriptionUtils.resolve(descriptions, fr, store(en)).get().getName());
	}

	@Test
	public void fallsBackToStoreDefaultLanguage() {
		Set<ProductDescription> descriptions = new LinkedHashSet<>(
				Arrays.asList(description(en, "Chair"), description(es, "Silla")));

		assertEquals("Silla",
				ProductDescriptionUtils.resolve(descriptions, fr, store(es)).get().getName());
	}

	@Test
	public void fallsBackToEnglishWhenStoreDefaultMissing() {
		Set<ProductDescription> descriptions = new LinkedHashSet<>(
				Arrays.asList(description(de, "Stuhl"), description(en, "Chair")));

		assertEquals("Chair",
				ProductDescriptionUtils.resolve(descriptions, fr, store(es)).get().getName());
	}

	@Test
	public void fallsBackToFirstAvailableDescription() {
		Set<ProductDescription> descriptions = new LinkedHashSet<>(
				Collections.singletonList(description(de, "Stuhl")));

		assertEquals("Stuhl",
				ProductDescriptionUtils.resolve(descriptions, fr, null).get().getName());
	}

	@Test
	public void matchesByCodeWhenIdMissing() {
		Set<ProductDescription> descriptions = new LinkedHashSet<>(
				Arrays.asList(description(en, "Chair"), description(fr, "Chaise")));

		assertEquals("Chaise",
				ProductDescriptionUtils.resolve(descriptions, new Language("fr"), store(en)).get().getName());
	}

	@Test
	public void emptyWhenNoDescriptions() {
		assertFalse(ProductDescriptionUtils.resolve(Collections.emptySet(), fr, store(en)).isPresent());
		assertFalse(ProductDescriptionUtils.resolve(null, fr, store(en)).isPresent());
	}
}
