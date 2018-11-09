package com.onlinedoctor.pojo.patient;

public class Record {
	
	public static final int TYPE_IMAGE = 1; // 图片，单张
	public static final int TYPE_SURVEY = 2;
	public static final int TYPE_PRESCRIPTION = 3; // 处方图片
	
	private long id;
	private String patientId;
	private String doctorId;
	private String thumbnail;
	private String recordPic;
	private int recordType;
	private long created;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getRecordPic() {
		return recordPic;
	}

	public void setRecordPic(String recordPic) {
		this.recordPic = recordPic;
	}

	public int getRecordType() {
		return recordType;
	}

	public void setRecordType(int recordType) {
		this.recordType = recordType;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	
}
