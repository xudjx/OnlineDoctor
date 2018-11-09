package com.onlinedoctor.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.Toast;

import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends Activity {
	protected static final String TAG = "WelcomeActivity";
	private Context mContext;
	private ImageView mImageView;
	private SharedpreferenceManager spManager = null;

	private int count = 0;

	private static final int ERROR = 1;

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == ERROR){
				Toast.makeText(mContext, "请重新登陆", Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	protected void onResume(){
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected  void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
		spManager = SharedpreferenceManager.getInstance();
		findView();
		init();
	}

	private void findView() {
		mImageView = (ImageView) findViewById(R.id.iv_welcome);
	}

	private void init() {
		mImageView.postDelayed(new Runnable() {
			@Override
			public void run() {
				final String sid = spManager.getOne("keySid");
				final String token = spManager.getOne("keyToken");
				if (sid.equals("") || token.equals("")) {
					Intent intent = new Intent(mContext, LoginActivity.class);
					mContext.startActivity(intent);
				}
				else {
					// 没有创建WebSocket连接
					if (MyApplication.chatClient == null) {
						// 建立WebSocket连接
						Logger.d("Welcome", "Welcome create chatclient");
						MyApplication.createWebSocket(sid, token);
						Intent intent = new Intent(mContext,MainActivity.class);
						startActivity(intent);
						overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					} else {
						checkDoctorInfo();
					}
				}
				WelcomeActivity.this.finish();
			}
		}, 3000);
	}

	private void checkDoctorInfo(){
		MessageManager manager = MessageManager.getMessageManager();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("sid", spManager.getOne("keySid")));
		params.add(new BasicNameValuePair("token", spManager.getOne("keyToken")));
		params.add(new BasicNameValuePair("doctorId", spManager.getOne("keySid")));
		TextMessage tm = new TextMessage(Common.GetDoctorInfoUrl, params, new SimpleMessage.HttpCallBack() {
			@Override
			public boolean handle(String result) {
				// 处理返回结果
				try {
					JSONObject jsonResult = new JSONObject(result);
					int code = jsonResult .getInt("code");
					Intent intent = new Intent();
					if (code == 200) {
						intent.setClass(WelcomeActivity.this,MainActivity.class);
						startActivity(intent);
						finish();
					}else if(jsonResult.getJSONObject("data").getInt("errorCode") == -10013 ) {
						intent.setClass(WelcomeActivity.this, FillBasicInfoActivity.class);
						startActivity(intent);
						finish();
					} else {
						intent.setClass(WelcomeActivity.this,LoginActivity.class);
						startActivity(intent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return true;
			}
		}, new SimpleMessage.HttpFailedCallBack() {

			@Override
			public boolean handle(int errorCode, String errorMessage) {

				return false;
			}

		});
		manager.sendMessage(tm);
	}
}
