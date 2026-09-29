package com.salesmanager.test.business.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesmanager.core.business.utils.DisplayLocaleContext;
import com.salesmanager.core.business.utils.ProductPriceUtils;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;

public class ProductPriceUtilsLocaleTest {

	private static final BigDecimal AMOUNT = new BigDecimal("1234.56");

	private final ProductPriceUtils priceUtils = new ProductPriceUtils();
	private MerchantStore store;

	@Before
	public void setUp() {
		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("CAD"));

		Country country = new Country();
		country.setIsoCode("CA");

		Language language = new Language();
		language.setCode("en");

		store = new MerchantStore();
		store.setCode("DEFAULT");
		store.setCurrency(currency);
		store.setCountry(country);
		store.setDefaultLanguage(language);
		store.setCurrencyFormatNational(true);
	}

	@After
	public void tearDown() {
		DisplayLocaleContext.clear();
	}

	@Test
	public void usesStoreLocaleWhenNoDisplayLocale() throws Exception {
		assertEquals(expected(new Locale("en", "CA")), priceUtils.getStoreFormatedAmountWithCurrency(store, AMOUNT));
	}

	@Test
	public void usesDisplayLocaleWhenRequested() throws Exception {
		DisplayLocaleContext.set(Locale.CANADA_FRENCH);

		String formatted = priceUtils.getStoreFormatedAmountWithCurrency(store, AMOUNT);

		assertEquals(expected(Locale.CANADA_FRENCH), formatted);
		assertTrue(formatted.contains(",56"));
	}

	@Test
	public void completesLanguageOnlyLocaleWithStoreCountry() throws Exception {
		DisplayLocaleContext.set(Locale.FRENCH);

		assertEquals(expected(Locale.CANADA_FRENCH), priceUtils.getStoreFormatedAmountWithCurrency(store, AMOUNT));
		assertEquals(Locale.CANADA_FRENCH, DisplayLocaleContext.resolve(store).get());
	}

	@Test
	public void clearRestoresStoreLocale() throws Exception {
		DisplayLocaleContext.set(Locale.GERMANY);
		DisplayLocaleContext.clear();

		assertFalse(DisplayLocaleContext.get().isPresent());
		assertEquals(expected(new Locale("en", "CA")), priceUtils.getStoreFormatedAmountWithCurrency(store, AMOUNT));
	}

	private String expected(Locale locale) {
		NumberFormat format = NumberFormat.getCurrencyInstance(locale);
		format.setCurrency(java.util.Currency.getInstance("CAD"));
		return format.format(AMOUNT.doubleValue());
	}

}
