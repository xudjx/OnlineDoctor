package com.onlinedoctor.pojo.chats;

import java.io.Serializable;

/**
 * 发送消息的实体类
 */
public class PatientMessageDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String guid;
	private String from;
	private String to;
	private long time;
	private String contentType;
	private String content;

	public PatientMessageDTO() {

	}

	public PatientMessageDTO(String guid, String from, String to, long time, String contentType, String content) {
		super();
		this.guid = guid;
		this.from = from;
		this.to = to;
		this.time = time;
		this.contentType = contentType;
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
