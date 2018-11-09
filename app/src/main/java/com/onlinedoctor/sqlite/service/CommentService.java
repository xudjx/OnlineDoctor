package com.onlinedoctor.sqlite.service;

import java.util.List;
import java.util.Map;

public interface CommentService {

	// CRUD
	public boolean addCommentInfo(Object[] params);

	public boolean updateCommentInfo(Object[] params);

	public boolean deleteCommentInfo(Object[] params);

	// 根据一定的条件找出所有的信息
	public List<Map<String, String>> listCommentInfoByDoctorId(String[] selectionArgs);
	public List<Map<String, String>> listCommentInfoByTime(String[] selectionArgs);

	// 查看某条信息
	public Map<String, String> viewCommentInfo(String[] selectionArgs);

	// 彻底删除消息
	public boolean deleteAllComment();

}
