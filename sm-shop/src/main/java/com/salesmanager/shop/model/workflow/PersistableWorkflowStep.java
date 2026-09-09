package com.salesmanager.shop.model.workflow;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class PersistableWorkflowStep implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	@NotNull
	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
