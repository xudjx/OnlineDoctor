package com.onlinedoctor.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.pojo.Common;

public class HttpTest {

	@SuppressWarnings("unused")
	public void httpPostTest() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user_id", "adfadf"));
		MessageManager manager = MessageManager.getMessageManager();
		TextMessage tm = new TextMessage(Common.RootChatUrl, params, new HttpCallBack() {

			@Override
			public boolean handle(String result) {
				Log.i("OnSuccess", result);
				return false;
			}
		}, new HttpFailedCallBack() {

			@Override
			public boolean handle(int errorCode, String errorMessage) {
				Log.i("OnSuccess", "ErrorCode=" + errorCode + ", ErrorMessage=" + errorMessage + "]");
				return false;
			}
		});
		manager.sendMessage(tm);
	}
}
