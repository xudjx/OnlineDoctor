package com.onlinedoctor.adapter.mine;

import java.util.ArrayList;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

/**
 * 用来作为ViewPager展示的Adapter
 */
public class MyPagerAdapter extends PagerAdapter {

	private ArrayList<View> views;
	private Activity activity;

	public MyPagerAdapter(ArrayList<View> views, Activity activity) {
		Log.i("PageAdapter", "create PagerAdapter Success");
		this.views = views;
		this.activity = activity;
	}

	@Override
	public int getCount() {
		return this.views.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(views.get(position));
	}

	public Object instantiateItem(View container, int position) {
		Log.i("PageAdapter", "instantiateItem");
		((ViewPager) container).addView(views.get(position));
		return views.get(position);
	}

}
