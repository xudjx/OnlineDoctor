package com.onlinedoctor.pojo.chats;

import java.io.Serializable;

/**
 * 发送SynMessage数据请求
 */
public class SynMessageDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String timestamp; // 发送timestamp以synMessage

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public SynMessageDTO(String timestamp) {
		this.timestamp = timestamp;
	}
}
