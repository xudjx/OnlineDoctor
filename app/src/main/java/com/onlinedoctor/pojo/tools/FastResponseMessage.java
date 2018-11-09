package com.onlinedoctor.pojo.tools;

public class FastResponseMessage {
	private int id;
	private String msg;
	
	public FastResponseMessage(){}
	public FastResponseMessage(int id, String msg) {
		super();
		this.id = id;
		this.msg = msg;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}


	
}
