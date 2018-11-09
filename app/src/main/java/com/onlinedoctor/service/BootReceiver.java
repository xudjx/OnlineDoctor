package com.onlinedoctor.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.login.LoginActivity;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.MyChatClient;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.NetworkUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Android广播事件处理： 开机事件、网络状态变化、应用内发送的重连请求
 */
public class BootReceiver extends BroadcastReceiver {

	private SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
	private Context context = MyApplication.context;

	private Lock lock = new ReentrantLock();

	private int count = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BootReceiver", "Receiver action:" + intent.getAction());
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			reconnectWebSocket();
		} else if (action.equals(Common.ACTION_REConnect_WebSocket)) {
			Log.d("BootReceiver", "connect count: " + (++count));
			reconnectWebSocket();
		} else if (action.equals(Common.ACTION_CONNECTIVITY_CHANGE)) {
			// 有网络状况
			if (NetworkUtil.checkNetworkStatus(context)) {
				reconnectWebSocket();
			}
		}
	}

	public void reconnectWebSocket() {
		Logger.d("BootReceiver", "Begin reconnect!");
		final String sid = spManager.getOne("keySid");
		final String token = spManager.getOne("keyToken");
		Logger.d("BootReceiver","sid="+sid);
		Logger.d("BootReceiver","token="+token);
		if (sid.equals("") || token.equals("")) {
			return;
		}
		Logger.d("BootReceiver", "BootReceiver create chatclient");
		MyApplication.createWebSocket(sid,token);
	}
}
