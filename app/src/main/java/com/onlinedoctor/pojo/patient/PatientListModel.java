package com.onlinedoctor.pojo.patient;

import java.io.Serializable;


public class PatientListModel implements Serializable{

	private static final long serialVersionUID = -2511377779854840513L;
	private String name;
	private String sortLetters;
	private String imgUrl;
	private String userId;

	public String getImgUrl(){
		return imgUrl;
	}
	public void setImgUrl(String imgUrl){
		this.imgUrl = imgUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	

}
