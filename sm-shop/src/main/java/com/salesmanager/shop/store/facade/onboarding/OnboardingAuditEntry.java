package com.salesmanager.shop.store.facade.onboarding;

import java.time.Instant;

public class OnboardingAuditEntry {

	private final Instant timestamp;
	private final OnboardingChannel channel;
	private final String action;
	private final String detail;

	public OnboardingAuditEntry(Instant timestamp, OnboardingChannel channel, String action, String detail) {
		this.timestamp = timestamp;
		this.channel = channel;
		this.action = action;
		this.detail = detail;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public OnboardingChannel getChannel() {
		return channel;
	}

	public String getAction() {
		return action;
	}

	public String getDetail() {
		return detail;
	}
}
