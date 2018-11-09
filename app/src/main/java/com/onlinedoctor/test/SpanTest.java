package com.onlinedoctor.test;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;

import com.onlinedoctor.activity.R;

public class SpanTest {

	private EditText tv = null;
	private List<String> list = new LinkedList<String>();

	/**
	 * 超链接
	 */
	private void addUrlSpan() {
		SpannableString spanString = new SpannableString("超链接");
		URLSpan span = new URLSpan("tel:0123456789");
		spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 文字背景颜色
	 */
	private void addBackColorSpan() {
		SpannableString spanString = new SpannableString("颜色2");
		BackgroundColorSpan span = new BackgroundColorSpan(Color.YELLOW);
		spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 文字颜色
	 */
	private void addForeColorSpan() {
		SpannableString spanString = new SpannableString("颜色1");
		ForegroundColorSpan span = new ForegroundColorSpan(Color.BLUE);
		spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 字体大小
	 */
	private void addFontSpan() {
		SpannableString spanString = new SpannableString("36号字体");
		AbsoluteSizeSpan span = new AbsoluteSizeSpan(36);
		spanString.setSpan(span, 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 粗体，斜体
	 */
	private void addStyleSpan() {
		SpannableString spanString = new SpannableString("BIBI");
		StyleSpan span = new StyleSpan(Typeface.BOLD_ITALIC);
		spanString.setSpan(span, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 删除线
	 */
	private void addStrikeSpan() {
		SpannableString spanString = new SpannableString("删除线");
		StrikethroughSpan span = new StrikethroughSpan();
		spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 下划线
	 */
	private void addUnderLineSpan() {
		SpannableString spanString = new SpannableString("下划线");
		UnderlineSpan span = new UnderlineSpan();
		spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}

	/**
	 * 图片
	 */
	private void addImageSpan(Context context) {
		SpannableString spanString = new SpannableString(" ");
		Drawable d = context.getResources().getDrawable(R.drawable.ic_launcher);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
		spanString.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.append(spanString);
	}
}
