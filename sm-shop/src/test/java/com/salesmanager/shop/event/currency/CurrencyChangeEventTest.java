package com.salesmanager.shop.event.currency;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CurrencyChangeEvent}.
 */
public class CurrencyChangeEventTest {

    @Test
    public void shouldStoreAllFields() {
        Object source = new Object();
        CurrencyChangeEvent event = new CurrencyChangeEvent(
                source, "DEFAULT", "EUR", CurrencyChangeEvent.ChangeType.ADDED);

        assertEquals("DEFAULT", event.getStoreCode());
        assertEquals("EUR", event.getCurrencyCode());
        assertEquals(CurrencyChangeEvent.ChangeType.ADDED, event.getChangeType());
        assertSame(source, event.getSource());
    }

    @Test
    public void shouldSupportRemovedChangeType() {
        CurrencyChangeEvent event = new CurrencyChangeEvent(
                this, "STORE1", "USD", CurrencyChangeEvent.ChangeType.REMOVED);

        assertEquals(CurrencyChangeEvent.ChangeType.REMOVED, event.getChangeType());
    }

    @Test
    public void toStringShouldContainKeyFields() {
        CurrencyChangeEvent event = new CurrencyChangeEvent(
                this, "DEFAULT", "GBP", CurrencyChangeEvent.ChangeType.ADDED);

        String str = event.toString();
        assertTrue(str.contains("DEFAULT"));
        assertTrue(str.contains("GBP"));
        assertTrue(str.contains("ADDED"));
    }
}
