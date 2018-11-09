package com.onlinedoctor.pojo.patient;

import com.onlinedoctor.pojo.chats.BriefMessagePojo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Patient implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	private String patientId = "";
	private String name = "";
	private String gender = "";
	private long birthday = 0;
	private long registerTime = 0;
	private long lastTime = 0;
	private String thumbnail = "";
	private String avatar = "";
	private String phone = "";
	private String pinyin = "";
	private String comment = "";
	//not in db
	private String sortLetters = "";

	public Patient() {
	}

	public Patient(BriefMessagePojo pojo){
		this.patientId = pojo.getUserId();
		this.name = pojo.getUserName();
		this.thumbnail = pojo.getFaceImageUrl();
	}

	public Patient(String patientId, String name, String gender, long birthday, long registerTime, long lastTime,
				   String thumbnail, String avatar, String phone, String pinyin, String comment) {
		this.patientId = patientId;
		this.name = name;
		this.gender = gender;
		this.birthday = birthday;
		this.registerTime = registerTime;
		this.lastTime = lastTime;
		this.thumbnail = thumbnail;
		this.avatar = avatar;
		this.phone = phone;
		this.pinyin = pinyin;
		this.comment = comment;
	}

	public String getPatientId() {
		return patientId;
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

	public long getBirthday() {
		return birthday;
	}

	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}

	public long getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(long registerTime) {
		this.registerTime = registerTime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public static int birthdayToAge(long birthday){
		int age = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String birthYear = sdf.format(new Date(birthday));
		String curYear = sdf.format(new Date(System.currentTimeMillis()));
		age = Integer.valueOf(curYear) - Integer.valueOf(birthYear);
		return age;
	}

	public String getDetailString(){
		String detailString = this.gender + " " + Integer.toString(birthdayToAge(this.birthday)) + "岁";
		return detailString;
	}

	public String getAllCommentString(){
		String commentString = "备注： " + this.getDetailString() + " " + this.getComment();
		return commentString;
	}
}
