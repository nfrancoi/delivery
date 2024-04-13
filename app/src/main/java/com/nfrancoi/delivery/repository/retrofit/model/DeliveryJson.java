package com.nfrancoi.delivery.repository.retrofit.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DeliveryJson {
	private Long deliveryId;
	
	private Date startDate;
	private Date sentDate;

	private String receiverName;
	private String commentDelivery;
	private String commentReceiver;


	private String noteId;

	private Long employeeId;
	private String employee;

	private Long pointOfDeliveryId;
	private String pointOfDelivery;

	// vat
	private boolean isVatApplicable;

	@SerializedName("deliveryProducts")
	private List<DeliveryProductJson> deliveryProductJsons
			= new ArrayList<DeliveryProductJson>();

	@Override
	public String toString() {

		StringBuilder sbDeliveryProducts = new StringBuilder();
		
		for (DeliveryProductJson dp : deliveryProductJsons) {
			sbDeliveryProducts.append(dp).append("\n");
		}

		String output = "Delivery [deliveryId=" + deliveryId + ", startDate=" + startDate + ", sentDate=" + sentDate
				+ ", receiverName=" + receiverName + ", commentDelivery=" + commentDelivery + ", commentReceiver="
				+ commentReceiver + ", noteId=" + noteId + ", employeeId=" + employeeId + ", employee=" + employee
				+ ", pointOfDeliveryId=" + pointOfDeliveryId + ", pointOfDelivery=" + pointOfDelivery
				+ ", isVatApplicable=" + isVatApplicable + ", deliveryProducts=" + sbDeliveryProducts.toString() + "]";

		return output;
	}

	public Long getDeliveryId() {
		return deliveryId;
	}

	public void setDeliveryId(Long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getCommentDelivery() {
		return commentDelivery;
	}

	public void setCommentDelivery(String commentDelivery) {
		this.commentDelivery = commentDelivery;
	}

	public String getCommentReceiver() {
		return commentReceiver;
	}

	public void setCommentReceiver(String commentReceiver) {
		this.commentReceiver = commentReceiver;
	}

	public String getNoteId() {
		return noteId;
	}

	public void setNoteId(String noteId) {
		this.noteId = noteId;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployee() {
		return employee;
	}

	public void setEmployee(String employee) {
		this.employee = employee;
	}

	public Long getPointOfDeliveryId() {
		return pointOfDeliveryId;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public void setPointOfDeliveryId(Long pointOfDeliveryId) {
		this.pointOfDeliveryId = pointOfDeliveryId;
	}

	public String getPointOfDelivery() {
		return pointOfDelivery;
	}

	public void setPointOfDelivery(String pointOfDelivery) {
		this.pointOfDelivery = pointOfDelivery;
	}

	public boolean isVatApplicable() {
		return isVatApplicable;
	}

	public void setVatApplicable(boolean isVatApplicable) {
		this.isVatApplicable = isVatApplicable;
	}

	public List<DeliveryProductJson> getDeliveryProductJsons() {
		return deliveryProductJsons;
	}

	public void setDeliveryProductJsons(List<DeliveryProductJson> deliveryProductJsons) {
		this.deliveryProductJsons = deliveryProductJsons;
	}

}
