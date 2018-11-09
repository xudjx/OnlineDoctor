package com.onlinedoctor.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.net.MyChatClient;

/**
 * 后台Service 隔一段时间就发送“ping - pong”数据给server
 */
public class KeepAliveService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("KeepAliveService", "KeepAliveService Create");
		final MyChatClient client = MyApplication.chatClient;
		if (client != null) {
			client.beChatty(); // 一直发送ping pong数据给后台
		}
	}
}
