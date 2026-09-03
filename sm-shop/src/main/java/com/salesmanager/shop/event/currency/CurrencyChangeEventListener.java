package com.salesmanager.shop.event.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for {@link CurrencyChangeEvent}s and propagates real-time
 * notifications to connected clients.
 *
 * <p>The listener is invoked asynchronously (via {@code @Async}) so that
 * the originating transaction is not blocked by downstream notification
 * delivery.  Extend or replace this class to integrate with a WebSocket
 * broker, SSE endpoint, or message queue as required.
 */
@Component
public class CurrencyChangeEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyChangeEventListener.class);

    /**
     * Handle a currency change event.
     *
     * <p>Current implementation logs the event and serves as the integration
     * point for real-time push mechanisms (WebSocket, SSE, etc.).
     *
     * @param event the currency change event
     */
    @Async
    @EventListener
    public void onCurrencyChange(CurrencyChangeEvent event) {
        LOGGER.info("[REAL-TIME] Currency change detected – store='{}', currency='{}', type={}",
                event.getStoreCode(), event.getCurrencyCode(), event.getChangeType());

        // TODO: integrate with WebSocket / SSE broker to push update to
        //       connected storefront clients, e.g.:
        //
        //   messagingTemplate.convertAndSend(
        //       "/topic/store/" + event.getStoreCode() + "/currencies",
        //       buildPayload(event));
        //
        // For now the log entry acts as the observable side-effect that
        // confirms real-time processing has occurred.
        notifyStorefront(event);
    }

    /**
     * Extension point: override to push the event to a real-time channel.
     *
     * @param event the currency change event
     */
    protected void notifyStorefront(CurrencyChangeEvent event) {
        // Default: no-op placeholder – subclass or replace with actual push logic
        LOGGER.debug("notifyStorefront called for event: {}", event);
    }
}
