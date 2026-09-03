package com.salesmanager.shop.event.currency;

import org.springframework.context.ApplicationEvent;

/**
 * Spring application event fired whenever a currency is added to or removed
 * from a merchant store's supported-currency list.
 *
 * <p>Listeners (e.g. WebSocket broadcasters, cache invalidators) can subscribe
 * to this event to propagate real-time updates to connected clients.
 */
public class CurrencyChangeEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /** The type of change that occurred. */
    public enum ChangeType {
        ADDED,
        REMOVED
    }

    private final String storeCode;
    private final String currencyCode;
    private final ChangeType changeType;

    /**
     * @param source       the object that published the event (never {@code null})
     * @param storeCode    the merchant store affected
     * @param currencyCode the ISO 4217 currency code that changed
     * @param changeType   whether the currency was added or removed
     */
    public CurrencyChangeEvent(Object source, String storeCode,
                               String currencyCode, ChangeType changeType) {
        super(source);
        this.storeCode = storeCode;
        this.currencyCode = currencyCode;
        this.changeType = changeType;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String toString() {
        return "CurrencyChangeEvent{"
                + "storeCode='" + storeCode + '\''
                + ", currencyCode='" + currencyCode + '\''
                + ", changeType=" + changeType
                + '}';
    }
}
