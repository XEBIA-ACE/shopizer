package com.salesmanager.shop.model.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PersistableWorkflow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	@NotNull
	private String name;

	@Valid
	private List<PersistableWorkflowStep> steps = new ArrayList<>();

	/**
	 * Expected current version when updating; used for optimistic concurrency.
	 */
	private Integer version;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PersistableWorkflowStep> getSteps() {
		return steps;
	}

	public void setSteps(List<PersistableWorkflowStep> steps) {
		this.steps = steps;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
