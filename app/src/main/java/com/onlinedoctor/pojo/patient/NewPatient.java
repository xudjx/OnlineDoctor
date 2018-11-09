package com.onlinedoctor.pojo.patient;

public class NewPatient {
	
	private String patientId;
	private String name;
	private String gender;
	private long followTime;
	private String thumbnail;
	private int status;
	private int isChecked;


	public int getIsChecked() {
		return isChecked;
	}

	public void setIsChecked(int isChecked) {
		this.isChecked = isChecked;
	}
	
	public String getPatientId() {
		return patientId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public long getFollowTime() {
		return followTime;
	}

	public void setFollowTime(long followTime) {
		this.followTime = followTime;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public NewPatient() {
		// TODO Auto-generated constructor stub
	}
}
