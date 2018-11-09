package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.chats.BriefMessagePojo;

public interface CurrentChatUserService {

	// CRUD
	public boolean addChatUserList(List<BriefMessagePojo> messagePojos);

	public boolean addChatUserOne(BriefMessagePojo messagePojo);

	public boolean updateChatUser(BriefMessagePojo messagePojo);

	public boolean deleteChatUser(String userId);
	
	public List<BriefMessagePojo> getAllChatUsersInTimeOrder();
	
	public BriefMessagePojo getChatUserByUserId(String userId);
	
	public List<BriefMessagePojo> listTopKBriefMessage(long k);
}
