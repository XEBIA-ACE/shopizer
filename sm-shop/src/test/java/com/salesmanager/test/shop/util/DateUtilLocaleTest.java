package com.salesmanager.test.shop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.After;
import org.junit.Test;

import com.salesmanager.core.business.utils.DisplayLocaleContext;
import com.salesmanager.shop.utils.DateUtil;

public class DateUtilLocaleTest {

	private static final Date DATE = new Date(1700000000000L);

	@After
	public void tearDown() {
		DisplayLocaleContext.clear();
	}

	@Test
	public void formatDateUsesLocaleConventions() {
		assertEquals(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY).format(DATE),
				DateUtil.formatDate(DATE, Locale.GERMANY));
		assertEquals(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(DATE),
				DateUtil.formatDate(DATE, Locale.US));
	}

	@Test
	public void formatLongDateKeepsLegacyFormatWithoutDisplayLocale() {
		assertEquals(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(DATE), DateUtil.formatLongDate(DATE));
	}

	@Test
	public void formatLongDateUsesDisplayLocale() {
		DisplayLocaleContext.set(Locale.FRANCE);

		assertEquals(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.FRANCE).format(DATE),
				DateUtil.formatLongDate(DATE));
	}

	@Test
	public void nullDatesReturnNull() {
		assertNull(DateUtil.formatDate(null, Locale.US));
		assertNull(DateUtil.formatLongDate(null, Locale.US));
		assertNull(DateUtil.formatLongDate(null));
	}

}
