package com.onlinedoctor.pojo.mine;

public class SelfDefinedFee {
	private int id;
	private String title;
	private String description;
	private String fee;
	private long global_id;
	private long update_time;
	
	
	
	public SelfDefinedFee(int id, String title, String description, String fee,
			long global_id, long update_time) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.fee = fee;
		this.global_id = global_id;
		this.update_time = update_time;
	}

	public SelfDefinedFee(String title, String description, String fee,
			long global_id, long update_time) {
		super();
		this.title = title;
		this.description = description;
		this.fee = fee;
		this.global_id = global_id;
		this.update_time = update_time;
	}

	public SelfDefinedFee(int id, String title, String description, String fee) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.fee = fee;
	}
	
	public SelfDefinedFee(String title, String description, String fee) {
		super();
		this.title = title;
		this.description = description;
		this.fee = fee;
	}

	public SelfDefinedFee(){}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public long getGlobal_id() {
		return global_id;
	}

	public void setGlobal_id(long global_id) {
		this.global_id = global_id;
	}

	
	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}
	
}
