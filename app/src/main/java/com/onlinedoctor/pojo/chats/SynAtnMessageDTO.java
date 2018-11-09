package com.onlinedoctor.pojo.chats;

import java.io.Serializable;

/**
 * 发送SynMessage数据请求
 */
public class SynAtnMessageDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String timestamp;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public SynAtnMessageDTO(String s) {
		timestamp = s;
	}
}
