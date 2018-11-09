package com.onlinedoctor.view;

import com.onlinedoctor.activity.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommonActionbarCancelRelativeLayout extends RelativeLayout {

	public CommonActionbarCancelRelativeLayout(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.actionbar_common_cancel, this);
		TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
		backTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((Activity)getContext()).finish();
			}
		});
	}

	public void setTitle(String title){
		TextView titleTextView = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
		titleTextView.setText(title);
	}

	public void setRight(String right){
		TextView rightView = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
		rightView.setText(right);
	}

	public TextView getRightView(){
		TextView rightView = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
		return rightView;
	}
}
