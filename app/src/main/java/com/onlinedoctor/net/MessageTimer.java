package com.onlinedoctor.net;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

import com.onlinedoctor.pojo.Common;

/**
 * 消息计时器，并处理消息失败
 */
public class MessageTimer {

	public static final int FAILED_TIME = 30; // 设定消息默认超时时间
	private static MessageTimer instance;
	public HashMap<String, TimerTask> msgTimerMap;
	private Timer timer;

	private MessageTimer() {
		msgTimerMap = new HashMap<String, TimerTask>();
		timer = new Timer();
	}

	public static MessageTimer getInstance() {
		if (instance == null) {
			synchronized (MessageTimer.class) {
				if (instance == null) {
					instance = new MessageTimer();
					return instance;
				}
			}
		}
		return instance;
	}

	/**
	 * 新消息开始计时: 传入消息的guid
	 * @param guid, 消息的guid
	 * @param timeOut, 超时时间
	 * @param  handler, 回调方法
	 */
	public boolean begin(String guid, int timeOut, RTOHandler handler) {
		return addMsgTimer(guid, timeOut, handler);
	}

	/**
	 * 停止消息计时,判断是否超时
	 * 
	 * @return true: 超时 false: 不超时
	 */
	public boolean stop(String guid) {
		if (msgTimerMap.containsKey(guid)) {
			MyTimerTask timerTask = (MyTimerTask) msgTimerMap.get(guid);
			int time = timerTask.stop();
			timerTask.cancel();
			msgTimerMap.remove(guid);
			if (time > FAILED_TIME) {
				return true;
			}
		}
		return false;
	}

	public boolean addMsgTimer(String guid, int timeOut, RTOHandler handler) {
		MyTimerTask myTimer = new MyTimerTask(guid,timeOut, handler);
		// 读秒计时
		timer.scheduleAtFixedRate(myTimer, 0, 1000);
		msgTimerMap.put(guid, myTimer);
		return true;
	}

	public class MyTimerTask extends TimerTask {
		private int count_time = 0;
		private volatile boolean is_pause = false;
		private String guid;
		private int timeOut;
		private RTOHandler handler;

		public MyTimerTask(String guid, int timeOut, RTOHandler handler) {
			this.guid = guid;
			this.timeOut = timeOut;
			this.handler = handler;
		}

		@Override
		public void run() {
			if (!is_pause) {
				count_time++;
				int hh = count_time / 3600;
				int mm = (count_time % 3600) / 60;
				int ss = count_time % 60;
				String date = hh + ":" + mm + ":" + ss;
				if (count_time > timeOut) {
					is_pause = true;
					// 触发回调方法
					handler.handle();

					// TimerTask停止
					msgTimerMap.remove(guid);
					this.cancel();
					System.gc();
				}
			}
		}

		public int stop() {
			is_pause = true;
			return getTime();
		}

		public int getTime() {
			return count_time;
		}
	}

	public interface  RTOHandler{
		public void handle();
	}
}
