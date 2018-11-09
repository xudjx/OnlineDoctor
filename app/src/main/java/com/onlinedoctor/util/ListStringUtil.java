package com.onlinedoctor.util;

import java.util.List;


public class ListStringUtil {
	public static String List2String(List<String> list, String sp){
		String ret = "";
		for (String str : list) {
			str += sp;
			ret += str;
		}
		return ret;
	}
	public static String[] String2List(String str, String sp){
		return str.split(sp);
	}
}
