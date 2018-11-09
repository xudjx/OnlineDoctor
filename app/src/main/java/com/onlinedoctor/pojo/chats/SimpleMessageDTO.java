package com.onlinedoctor.pojo.chats;

import java.io.Serializable;

public class SimpleMessageDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String type = "";

	public SimpleMessageDTO(String s) {
		type = s;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
