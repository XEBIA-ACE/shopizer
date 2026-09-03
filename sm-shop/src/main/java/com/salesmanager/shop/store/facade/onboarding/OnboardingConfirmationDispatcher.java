package com.salesmanager.shop.store.facade.onboarding;

import java.util.Locale;

import com.salesmanager.core.model.merchant.MerchantStore;

public interface OnboardingConfirmationDispatcher {

	void dispatch(String workflowId, MerchantStore store, Locale locale, String contextPath);
}
