package com.onlinedoctor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.onlinedoctor.activity.R;

/**
 * loading图旋转的布局（菊花神图）
 */
public class LoadingRotateLinearLayout extends LinearLayout {
	private Context mContext;

	public LoadingRotateLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public LoadingRotateLinearLayout(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.loading_rotate);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);

		ImageView image = new ImageView(mContext);
		image.setImageResource(R.drawable.loading_image);
		image.clearAnimation();
		image.startAnimation(operatingAnim);
		this.addView(image);
	}
}
