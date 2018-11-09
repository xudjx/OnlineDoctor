package com.onlinedoctor.test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.message.ImageMessage;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.pojo.Common;

public class PicTest {

	Context mContext;

	public PicTest(Context context) {
		this.mContext = context;
	}

	public void loadPicToView(ImageView v, String url) {
		ImageLoader.getInstance().displayImage(url, v);
	}

	public void postPicToServer() {
		MessageManager manager = MessageManager.getMessageManager();
		Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.defaulthead);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		ImageMessage tm = new ImageMessage(Common.ImageHttpUrl, byteArray, new HttpCallBack() {
			@Override
			public boolean handle(String result) {
				Gson gson = new Gson();
				Type type = new TypeToken<Map<String, Object>>() {
				}.getType();
				Map<String, Object> map = gson.fromJson(result, type);
				Log.d("imageBack", result);
				return true;
			}
		}, new HttpFailedCallBack() {

			@Override
			public boolean handle(int errorCode, String errorMessage) {
				Log.i("OnFailed", "ErrorCode=" + errorCode + ", ErrorMessage=" + errorMessage + "]");
				return false;
			}
		});
		manager.sendMessage(tm);
	}

}
