package com.onlinedoctor.log;

import android.util.Log;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.manager.ILogManager;
import com.onlinedoctor.log.manager.LogManager;
import com.onlinedoctor.log.utils.LogUtils;
import com.onlinedoctor.pojo.Common;

public class Logger {

	private static ILogManager logManager = LogManager.getManager(MyApplication.context);
	/**
	 * 程序是否Debug版本
	 */
	public static final boolean IsDebug = Common.DEBUG;
	private static final String TAG = "[OnlineDoctor]";

	public static void printStackTrace(String TAG, Exception e) {
		if (IsDebug) {
			e.printStackTrace();
			logManager.log(TAG, e.getMessage(), LogUtils.LOG_TYPE_2_FILE);
		}
	}

	/**
	 * 未捕获的异常
	 * 
	 * @param TAG
	 * @param e
	 */
	public static void printStackTrace(String TAG, Throwable e) {
		if (IsDebug) {
			e.printStackTrace();
			logManager.log(TAG, e.getMessage(), LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void d(String tag, String msg) {
		if (IsDebug) {
			Log.d(tag, msg);
			logManager.log(tag,msg, LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void d(String msg) {
		Log.d(TAG, msg);
		logManager.log(TAG, msg, LogUtils.LOG_TYPE_2_FILE);
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (IsDebug) {
			Log.d(tag, msg, tr);
			logManager.log(tag, msg + tr.getStackTrace(), LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void e(Throwable tr) {
		if (IsDebug) {
			Log.e(TAG, "", tr);
			logManager.log(TAG, "" + tr.getStackTrace(), LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void i(String msg) {
		if (IsDebug) {
			Log.i(TAG, msg);
			logManager.log(TAG, msg, LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void i(String tag, String msg) {
		if (IsDebug) {
			Log.i(tag, msg);
			logManager.log(tag, msg, LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (IsDebug) {
			Log.i(tag, msg, tr);
			logManager.log(tag, msg + tr.getStackTrace(), LogUtils.LOG_TYPE_2_FILE);
		}

	}

	public static void e(String tag, String msg) {
		if (IsDebug) {
			Log.e(tag, msg);
			logManager.log(tag, msg, LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void e(String msg) {
		if (IsDebug) {
			Log.e(TAG, msg);
			logManager.log(TAG, msg, LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (IsDebug) {
			Log.e(tag, msg, tr);
			logManager.log(tag, msg + tr.getStackTrace(), LogUtils.LOG_TYPE_2_FILE);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (IsDebug) {
			Log.e(TAG, msg, tr);
			logManager.log(TAG, msg + tr.getStackTrace(), LogUtils.LOG_TYPE_2_FILE);
		}
	}
}
