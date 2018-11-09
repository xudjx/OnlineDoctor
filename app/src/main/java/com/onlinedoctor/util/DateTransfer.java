package com.onlinedoctor.util;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * 各种日期格式转换
 */
public class DateTransfer {

	public static String dateFormat1 = "MM/dd/yyyy HH:mm";
	public static String dateFormat2 = "M月d日 HH时";
	public static String dateFormat3 = "M月d日 HH:mm";

	public static String transferLongToDate(String dateFormat, long millSec) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date date = new Date(millSec);
		return sdf.format(date);
	}
}
