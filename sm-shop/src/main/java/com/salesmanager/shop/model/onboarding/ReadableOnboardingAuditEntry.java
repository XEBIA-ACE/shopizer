package com.salesmanager.shop.model.onboarding;

import java.io.Serializable;

public class ReadableOnboardingAuditEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String timestamp;
	private String channel;
	private String action;
	private String detail;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
}
