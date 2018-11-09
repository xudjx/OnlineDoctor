package com.onlinedoctor.pojo.chats;

import java.io.Serializable;

import com.onlinedoctor.util.JsonUtil;

/**
 * 信息界面：保存当前正在聊天的对象 BriefMessage(简略消息)
 */
public class BriefMessagePojo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int count; // 未读消息数
	private String userId;
	private String userName;
	private String message; // 最新消息,缩略消息
	private String faceImageUrl;
	private long time; // 最近时间
	private boolean ifNoDisturbing; // 是否屏蔽
	private String contentType;
	private String labels;
	private int sendStatus;// 0：正在发送 1：发送成功 -1：发送失败
	private String pGuid;
	private int isDraft;// 0 false 1 true

	public BriefMessagePojo() {
		this.count = 0;
		this.ifNoDisturbing = false;
		this.isDraft = 0;
	}

	public BriefMessagePojo(BriefMessagePojo bm) {
		this.count = bm.getCount();
		this.userId = bm.getUserId();
		this.userName = bm.getUserName();
		this.message = bm.getMessage();
		this.faceImageUrl = bm.getFaceImageUrl();
		this.time = bm.getTime();
		this.ifNoDisturbing = bm.isIfNoDisturbing();
		this.contentType = bm.getContentType();
		this.labels = bm.getLabels();
		this.sendStatus = bm.getSendStatus();
		this.pGuid = bm.getpGuid();
		this.isDraft = bm.getIsDraft();
	}

	public BriefMessagePojo(int count, String userId, String userName, String message, String faceImageUrl, long time,
			boolean ifNoDisturbing, String contentType, String labels, int sendStatus, String pGuid, int isDraft) {
		super();
		this.count = count;
		this.userId = userId;
		this.userName = userName;
		this.message = message;
		this.faceImageUrl = faceImageUrl;
		this.time = time;
		this.ifNoDisturbing = ifNoDisturbing;
		this.contentType = contentType;
		this.labels = labels;
		this.sendStatus = sendStatus;
		this.pGuid = pGuid;
		this.isDraft = isDraft;
	}

	public void updateMeNotId(BriefMessagePojo bm){
		if(bm == null){
			return;
		}
		this.count = bm.count;
		this.userName = bm.userName;
		this.message = bm.message;
		this.faceImageUrl = bm.faceImageUrl;
		this.time = bm.time;
		this.ifNoDisturbing = bm.ifNoDisturbing;
		this.contentType = bm.contentType;
		this.labels = bm.labels;
		this.sendStatus = bm.sendStatus;
		this.pGuid = bm.pGuid;
		this.isDraft = bm.isDraft;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFaceImageUrl() {
		return faceImageUrl;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public void setFaceImageUrl(String faceImageUrl) {
		this.faceImageUrl = faceImageUrl;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isIfNoDisturbing() {
		return ifNoDisturbing;
	}

	public void setIfNoDisturbing(boolean ifNoDisturbing) {
		this.ifNoDisturbing = ifNoDisturbing;
	}

	public int getSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(int sendStatus) {
		this.sendStatus = sendStatus;
	}

	public String getpGuid() {
		return pGuid;
	}

	public void setpGuid(String pGuid) {
		this.pGuid = pGuid;
	}

	public int getIsDraft() {
		return isDraft;
	}

	public void setIsDraft(int isDraft) {
		this.isDraft = isDraft;
	}

	@Override
	public String toString() {
		return JsonUtil.objectToJson(this);
	}

}
