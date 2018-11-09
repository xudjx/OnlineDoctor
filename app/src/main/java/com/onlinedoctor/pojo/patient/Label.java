package com.onlinedoctor.pojo.patient;

public class Label {

	private long id = 0;
	private String labelName = "";
	private int labelColor = 0;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getLabelName() {
		return labelName;
	}
	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public int getLabelColor() {
		return labelColor;
	}

	public int setLabelColor(int labelColor) {
		int oldColor = this.labelColor;
		this.labelColor = labelColor;
		return oldColor;
	}
}
