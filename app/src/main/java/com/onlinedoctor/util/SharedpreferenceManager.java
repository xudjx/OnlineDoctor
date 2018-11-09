package com.onlinedoctor.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.RunDataContainer;

/**
 * 本类实现sharedpreference的存取操作 使用时，在common里面定义好调用名和存储名的映射关系。 代码中只能通过调用名来访问。
 * 通过存储名实际存储。
 * 
 */

public class SharedpreferenceManager {
	private static SharedpreferenceManager instance;
	private static final HashMap<String, String> preferenceMap = Common.preferenceMap;
	
	private SharedpreferenceManager(){
		
	}
	
	public static SharedpreferenceManager getInstance(){
		if(instance == null){
			synchronized(RunDataContainer.class){
				if(instance == null){
					instance = new SharedpreferenceManager();
				}
			}
		}
		return instance;
	}

	public int setOne(String k, String v) {
		SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences(
				preferenceMap.get("perfName"), MyApplication.context.MODE_PRIVATE).edit();
		if (preferenceMap.containsKey(k) == false) {
			return -1; // 没找到返回-1
		}
		editor.putString(preferenceMap.get(k), v);
		editor.commit();
		return 0;
	}

	public int set(HashMap<String, String> map) {
		Set<String> set = map.keySet();
		SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences(
				preferenceMap.get("perfName"), MyApplication.context.MODE_PRIVATE).edit();
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			String k = (String) iter.next();
			if (preferenceMap.containsKey(k) == false) {
				return -1; // 没找到返回-1. 先检查一遍，简化回滚
			}
		}
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			String k = "", v = "";
			k = (String) iter.next();
			v = (String) map.get(k);
			k = preferenceMap.get(k);
			editor.putString(k, v);
		}
		editor.commit();
		return 0;
	}

	public String getOne(String k) {
		SharedPreferences pref = MyApplication.context.getSharedPreferences(
				preferenceMap.get("perfName"), MyApplication.context.MODE_PRIVATE);
		String v = "";
		v = pref.getString(preferenceMap.get(k), "");
		return v;
	}

	public int get(HashMap<String, String> map) {
		Set<String> set = map.keySet();
		SharedPreferences pref = MyApplication.context.getSharedPreferences(
				preferenceMap.get("perfName"), MyApplication.context.MODE_PRIVATE);
		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			String k = "", v = "";
			k = (String) iter.next();
			v = (String) pref.getString(preferenceMap.get(k), "");
			map.put(k, v);
		}
		return 0;
	}

	public void remove(String key){
		SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences(
				preferenceMap.get("perfName"), MyApplication.context.MODE_PRIVATE).edit();
		editor.remove(preferenceMap.get(key));
		editor.commit();
	}
}
