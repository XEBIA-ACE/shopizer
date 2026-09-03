package com.salesmanager.shop.store.facade.onboarding;

import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

public enum OnboardingChannel {
	ONLINE,
	MOBILE,
	BRANCH;

	public static OnboardingChannel from(String value) {
		if (value == null || value.trim().isEmpty()) {
			return ONLINE;
		}
		try {
			return OnboardingChannel.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ServiceRuntimeException("Unknown onboarding channel [" + value + "]");
		}
	}
}
