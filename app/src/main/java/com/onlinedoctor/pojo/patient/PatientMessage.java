package com.onlinedoctor.pojo.patient;

import java.io.Serializable;

import com.onlinedoctor.util.JsonUtil;

public class PatientMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String guid;
	private String content;
	private String contentType;
	private String fromID;
	private String toID;
	private int isRead;
	private int sendStatus;
	private int deleted;
	private long timestamp;

	public PatientMessage() {
		super();
		this.isRead = 0;
		this.deleted = 0;
		this.sendStatus = 0;
	}

	public PatientMessage(PatientMessage pm) {
		this.id = pm.getId();
		this.guid = pm.getGuid();
		this.content = pm.getContent();
		this.contentType = pm.getContentType();
		this.fromID = pm.getFromID();
		this.toID = pm.getToID();
		this.isRead = pm.getIsRead();
		this.sendStatus = pm.getSendStatus();
		this.deleted = pm.getDeleted();
		this.timestamp = pm.getTimestamp();
	}

	// test demo
	public PatientMessage(String guid, String content, String contentType, String fromID, String toID, int isRead,
			int sendStatus, int deleted, long timestamp) {
		super();
		this.guid = guid;
		this.content = content;
		this.contentType = contentType;
		this.fromID = fromID;
		this.toID = toID;
		this.isRead = isRead;
		this.sendStatus = sendStatus;
		this.deleted = deleted;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFromID() {
		return fromID;
	}

	public void setFromID(String fromID) {
		this.fromID = fromID;
	}

	public String getToID() {
		return toID;
	}

	public void setToID(String toID) {
		this.toID = toID;
	}

	public int getIsRead() {
		return isRead;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}

	public int getSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(int sendStatus) {
		this.sendStatus = sendStatus;
	}

	public int getDeleted() {
		return deleted;
	}

	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return JsonUtil.objectToJson(this);
	}

}
