package com.onlinedoctor.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Pinyin {
	
	public static final String getSortLetter(char hanzi){
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		String pinyin = "";
		try {
			pinyin = PinyinHelper.toHanyuPinyinStringArray(hanzi, format)[0];
		} catch(BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		if(pinyin == ""){
			pinyin = "#";
		}
		return pinyin.substring(0, 1);
	}

	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}
	
	public static final String convertToPinyin(String han){
		StringBuilder sb = new StringBuilder();
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		String pinyin = "";
		int len = han.length();
		for(int i = 0; i < len; i++){
			char one = han.charAt(i);
			if(isChinese(one)) {
				try {
					pinyin = PinyinHelper.toHanyuPinyinStringArray(one, format)[0];
					sb.append(pinyin);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}
			else{
				String tmp = String.valueOf(one);
				sb.append(tmp.toLowerCase());
			}
		}
		return sb.toString();
	}

	public Pinyin() {
	}

}
