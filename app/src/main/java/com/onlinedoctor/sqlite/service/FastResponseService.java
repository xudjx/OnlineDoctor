package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.tools.FastResponseMessage;

public interface FastResponseService {
	public long addFastResponseItem(String msg);
	public boolean deleteFastResponseItem(FastResponseMessage frm);
	public boolean updateFastResponseItem(FastResponseMessage frm);
	public List<FastResponseMessage> getAllFastResponses();
}
