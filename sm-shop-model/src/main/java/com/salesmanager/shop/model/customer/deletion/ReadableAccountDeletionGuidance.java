package com.salesmanager.shop.model.customer.deletion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * US-004 guided account deletion content consumed by the storefront Profile page
 * Account Settings / Account Actions area.
 *
 * <ul>
 * <li>video* fields back the instructional video entry (FR-US-004-001, FR-US-004-002).</li>
 * <li>dialog* fields back the confirmation modal (FR-US-004-003, FR-US-004-004, FR-US-004-005).</li>
 * </ul>
 */
public class ReadableAccountDeletionGuidance implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sectionTitle;
	private String videoLabel;
	private String videoDescription;
	private String videoUrl;
	private boolean videoAvailable;
	private String deleteAccountLabel;
	private String dialogTitle;
	private String dialogPermanentNotice;
	private List<String> dialogConsequences = new ArrayList<>();
	private String dialogCancelLabel;
	private String dialogConfirmLabel;

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public String getVideoLabel() {
		return videoLabel;
	}

	public void setVideoLabel(String videoLabel) {
		this.videoLabel = videoLabel;
	}

	public String getVideoDescription() {
		return videoDescription;
	}

	public void setVideoDescription(String videoDescription) {
		this.videoDescription = videoDescription;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public boolean isVideoAvailable() {
		return videoAvailable;
	}

	public void setVideoAvailable(boolean videoAvailable) {
		this.videoAvailable = videoAvailable;
	}

	public String getDeleteAccountLabel() {
		return deleteAccountLabel;
	}

	public void setDeleteAccountLabel(String deleteAccountLabel) {
		this.deleteAccountLabel = deleteAccountLabel;
	}

	public String getDialogTitle() {
		return dialogTitle;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	public String getDialogPermanentNotice() {
		return dialogPermanentNotice;
	}

	public void setDialogPermanentNotice(String dialogPermanentNotice) {
		this.dialogPermanentNotice = dialogPermanentNotice;
	}

	public List<String> getDialogConsequences() {
		return dialogConsequences;
	}

	public void setDialogConsequences(List<String> dialogConsequences) {
		this.dialogConsequences = dialogConsequences;
	}

	public String getDialogCancelLabel() {
		return dialogCancelLabel;
	}

	public void setDialogCancelLabel(String dialogCancelLabel) {
		this.dialogCancelLabel = dialogCancelLabel;
	}

	public String getDialogConfirmLabel() {
		return dialogConfirmLabel;
	}

	public void setDialogConfirmLabel(String dialogConfirmLabel) {
		this.dialogConfirmLabel = dialogConfirmLabel;
	}

}
