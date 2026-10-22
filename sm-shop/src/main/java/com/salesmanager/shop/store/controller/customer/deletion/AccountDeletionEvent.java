package com.salesmanager.shop.store.controller.customer.deletion;

/**
 * Key events of the US-004 deletion confirmation flow (FR-US-004-006).
 */
public enum AccountDeletionEvent {
	INITIATED,
	CANCELLED,
	CONFIRMED,
	COMPLETED,
	FAILED
}
