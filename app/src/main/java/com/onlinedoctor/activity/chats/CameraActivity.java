package com.onlinedoctor.activity.chats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.umeng.analytics.MobclickAgent;

import java.io.FileInputStream;
import java.io.InputStream;

import uk.co.senab.photoview.PhotoViewAttacher;

public class CameraActivity extends AppCompatActivity {
	private Object imgUrl;
	private Bitmap bitmap;

	public static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
	public static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";

	private PhotoViewAttacher mAttacher;

	private Toast mCurrentToast;
	private Matrix mCurrentDisplayMatrix = null;

	private ImageLoader loader;
	private DisplayImageOptions bImageOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		init();
	}

	private void init() {
		TextView add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
		add_tv.setText("");
		TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("预览");
		findViewById(R.id.actionbar_common_backable_left_tv).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		ImageView mImageView = (ImageView) findViewById(R.id.iv_photo);

		bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error)
				.cacheOnDisk(true).considerExifParams(true).build();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		imgUrl = bundle.get("camera");
		Logger.d("CameraActivity",imgUrl.toString());
		try{
			loader = ImageLoader.getInstance();

			InputStream is = new FileInputStream(imgUrl.toString());
			BitmapFactory.Options opts=new BitmapFactory.Options();
			opts.inTempStorage = new byte[100 * 1024];
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inSampleSize = 2;
			bitmap = BitmapFactory.decodeStream(is, null, opts);
			mImageView.setImageBitmap(bitmap);

			//loader.displayImage("file:///" + imgUrl.toString(), mImageView, bImageOptions);
		}catch (Exception e){
			e.printStackTrace();
		}

		// The MAGIC happens here!
		mAttacher = new PhotoViewAttacher(mImageView);

		// Lets attach some listeners, not required though!
		mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
		mAttacher.setOnPhotoTapListener(new PhotoTapListener());
	}

	public void btnClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_camera:
			CameraActivity.this.finish();
			break;
		case R.id.confirm_camera:
			Intent intent = new Intent();
			intent.putExtra("imgUrl", imgUrl.toString());
			CameraActivity.this.setResult(ChatActivity.SHOW_CAMERA_RESULT, intent);
			CameraActivity.this.finish();
			break;
		}
	}

	private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {

		@Override
		public void onPhotoTap(View view, float x, float y) {
			float xPercentage = x * 100f;
			float yPercentage = y * 100f;
			showToast(String.format(PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));
		}
	}

	private void showToast(CharSequence text) {
		if (null != mCurrentToast) {
			mCurrentToast.cancel();
		}
		mCurrentToast = Toast.makeText(CameraActivity.this, text, Toast.LENGTH_SHORT);
		mCurrentToast.show();
	}

	private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener {

		@Override
		public void onMatrixChanged(RectF rect) {
			Logger.d("onMatrixChanged", rect.toString());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(bitmap != null){
			bitmap.recycle();
		}
		// Need to call clean-up
		mAttacher.cleanup();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return false;
	}
	@Override
	protected void onResume(){
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
