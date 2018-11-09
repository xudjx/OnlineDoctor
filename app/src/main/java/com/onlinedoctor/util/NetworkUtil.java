package com.onlinedoctor.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	public static boolean checkNetworkStatus(Context context) {
		boolean resp = false;
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo activeNetInfo = connMgr.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			resp = true;
		}
		return resp;
	}

	public static boolean isWirelessEnabled(Context context) {
		try {
			return Boolean.parseBoolean(invokeMethod("getMobileDataEnabled", null, context).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}

	public static Object invokeMethod(String methodName, Object[] arg, Context context) throws Exception {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("rawtypes")
		Class ownerClass = cm.getClass();
		Class[] argsClass = null;
		if (arg != null) {
			argsClass = new Class[1];
			argsClass[0] = arg.getClass();
		}
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(cm, arg);
	}
}
