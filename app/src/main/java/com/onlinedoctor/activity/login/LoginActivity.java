package com.onlinedoctor.activity.login;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.MyChatClient;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.net.message.TextHttpsMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.service.KeepAliveService;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.view.TextURLView;
import com.umeng.analytics.MobclickAgent;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.ForgetPasswordPage;
import cn.smssdk.gui.RegisterPage;

public class LoginActivity extends Activity {

	private Context mContext;
	private RelativeLayout rl_user;
	private Button mLogin;
	private Button register;
	private TextURLView mTextViewURL;
	private EditText account, password;
	private ImageView loginHead;
	private HashMap<String, String> userThumbMap;
	private JSONArray userArray;

	SharedpreferenceManager spManager;
	private final static String TAG = "LoginActivity";
	private final static int _msg_reg_succes=1;
	private final static int _msg_reg_fail=-2;
	private final static int _msg_reg_user_duplicate  = -3;
	private final static int _msg_reg_user_not_invited = -4;
	private final static int _msg_login_timeout = 1001;

	private boolean IsLogining = false;

	private Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case -1:
					Toast.makeText(mContext, "用户名或密码错误", Toast.LENGTH_SHORT).show();
					break;
				case 0:
					Toast.makeText(mContext, "登录成功", Toast.LENGTH_SHORT).show();
					MobclickAgent.onEvent(mContext, "doctor_logon_success");
					break;
				case _msg_reg_succes:
					Toast.makeText(LoginActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
					MobclickAgent.onEvent(mContext,"doctor_regist_success");
					break;
				case _msg_reg_fail:
					Toast.makeText(LoginActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
					break;
				case _msg_reg_user_not_invited:
					Toast.makeText(LoginActivity.this,"该应用目前仅针对内测用户开放",Toast.LENGTH_LONG).show();
					break;
				case _msg_reg_user_duplicate:
					Toast.makeText(LoginActivity.this,"注册失败,手机号已被注册",Toast.LENGTH_SHORT).show();
					break;
				case _msg_login_timeout:
					Toast.makeText(LoginActivity.this,"登陆超时，请稍候再试",Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = this;
		findView();
		HandlerManager.getManager().setMessageHandler(loginHandler);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IsLogining = false;
		initTvUrl();
		SMSSDK.unregisterAllEventHandler();
		MobclickAgent.onResume(this);
	}
	@Override
	protected  void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
	private void findView() {
		rl_user = (RelativeLayout) findViewById(R.id.rl_user);
		mLogin = (Button) findViewById(R.id.login);
		register = (Button) findViewById(R.id.register);
		mTextViewURL = (TextURLView) findViewById(R.id.tv_forget_password);
		loginHead = (ImageView) findViewById(R.id.login_picture);
		account = (EditText) findViewById(R.id.account);
		/*View apline = findViewById(R.id.apline);
		apline.setVisibility(View.GONE);
		account.setVisibility(View.GONE);*/
		password = (EditText) findViewById(R.id.password);
	}

	private void init() {
		Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.login_anim);
		anim.setFillAfter(true);
		rl_user.startAnimation(anim);

		final DisplayImageOptions option = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_stub)
				.showImageOnFail(R.drawable.ic_stub).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusBig)).build();

		final DisplayImageOptions bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusBig)).build();

		mLogin.setOnClickListener(loginOnClickListener);
		register.setOnClickListener(registerOnClickListener);
		mTextViewURL.setOnClickListener(forgetOnClickListener);

		userThumbMap = new HashMap<String, String>();
		spManager = SharedpreferenceManager.getInstance();
		String users = spManager.getOne("keyUsers");
		if(!users.isEmpty()) {
			try {
				userArray = new JSONArray(users);
				for (int i = 0; i < userArray.length(); i++) {
					JSONObject oneUser = userArray.getJSONObject(i);
					userThumbMap.put(oneUser.getString("phone"), oneUser.getString("thumbnail"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		account.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String phone = s.toString();
				if(userThumbMap.containsKey(phone)){
					String thumb = userThumbMap.get(phone);
					if(thumb.isEmpty()){
						loginHead.setImageResource(R.drawable.login_default_avatar);
					}
					else{
						ImageLoader.getInstance().displayImage(thumb, loginHead, option);
					}
				}
				else{
					loginHead.setImageResource(R.drawable.login_default_avatar);
				}
				Logger.d("login", "after edit");
			}
		});
		String lastThumbnail = spManager.getOne("keyLastThumb");
		if(lastThumbnail != null && !lastThumbnail.isEmpty()){
			ImageLoader.getInstance().displayImage(lastThumbnail, loginHead, option);
		}
		else {
			loginHead.setImageResource(R.drawable.login_default_avatar);
		}
		String lastPhone = spManager.getOne("keyLastPhone");
		if(lastPhone != null && !lastPhone.isEmpty()){
			account.setText(lastPhone);
		}

		findViewById(R.id.debug).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, MainActivity.class);
				mContext.startActivity(intent);
				finish();
			}
		});
	}

	private void initTvUrl() {
		mTextViewURL.setText(R.string.forget_password);
	}

	private OnClickListener loginOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Logger.d("test", "点了");
			if(IsLogining)
			{
				return;
			}
			IsLogining = true;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("cellphone", account.getText().toString()));
			params.add(new BasicNameValuePair("password", password.getText().toString()));
			MessageManager manager = MessageManager.getMessageManager();
			TextHttpsMessage tm = new TextHttpsMessage(Common.UrlHttps + "logon", params, new HttpCallBack() {
				@Override
				public boolean handle(String result) {
					// 处理返回结果
					Logger.d("test", "到handle了");
					IsLogining = false;
					String code = JsonUtil.getJsonStringByKey(result, "code").toString();
					if (code.equals("200.0")) {
						String data = JsonUtil.getJsonStringByKey(result, "data").toString();
						spManager = SharedpreferenceManager.getInstance();
						HashMap<String, String> setMap = new HashMap<String, String>();
						String sid = (String) JsonUtil.getJsonStringByKey(data, "sid"), token = (String) JsonUtil
								.getJsonStringByKey(data, "token");
						setMap.put("keySid", sid);
						setMap.put("keyToken", token);
						String phone = account.getText().toString();
						setMap.put("keyPhone", phone);
						spManager.set(setMap);

						String synTimes = spManager.getOne("keySynTimes");
						if(synTimes == null || synTimes.isEmpty()){
							spManager.setOne("keyLatestTime", "0");
							spManager.setOne("keyAtnLatestTime", "0");
						}
						else{
							try {
								JSONObject times = new JSONObject(synTimes);
								JSONObject oneTime = (JSONObject) times.get(phone);
								String msgTime = (String) oneTime.get("msgTime");
								String atnTime = (String) oneTime.get("atnTime");
								spManager.setOne("keyLatestTime", msgTime);
								spManager.setOne("keyAtnLatestTime", atnTime);
							}
							catch(JSONException e){
								e.printStackTrace();
							}

						}
						// 建立WebSocket连接
						Logger.i("Login", sid);
						Logger.i("Login", token);
						Message msg = new Message();
						msg.what = 0;
						loginHandler.sendMessage(msg);

						initWebSocket(sid, token);
						checkDoctorInfo(sid, token);
					} else {
						Message msg = new Message();
						msg.what = -1;
						loginHandler.sendMessage(msg);
					}
					return true;
				}
			}, new HttpFailedCallBack() {

				@Override
				public boolean handle(int errorCode, String errorMessage) {
					// 登陆失败，点击重新登陆
					IsLogining = false;
					Message msg = new Message();
					msg.what = _msg_login_timeout;
					loginHandler.sendMessage(msg);
					return false;
				}

			});
			manager.sendMessage(tm);
		}
	};

	private OnClickListener registerOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			RegisterPage registerPage = new RegisterPage();
			registerPage.setRegisterCallback(new EventHandler() {
				public void afterEvent(int event, int result, Object data) {
					if (result == SMSSDK.RESULT_COMPLETE) {
						@SuppressWarnings("unchecked")
						HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
						String country = (String) phoneMap.get("country");
						String phone = (String) phoneMap.get("phone");
						String password = (String) phoneMap.get("password");
						Logger.i("password", password);

						registerUser(country, phone, password);
					}
				}
			});
			registerPage.show(LoginActivity.this);
		}
	};

	private void initWebSocket(final String sid, final String token) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Logger.d("LoginActivity", "LoginActivity create chatclient");
				URI uri = null;
				try{
					uri = new URI(Common.RootChatUrl + sid + "/" + token);
				}catch (URISyntaxException e){
					e.printStackTrace();
				}
				new MyChatClient(uri, new MyChatClient.ChatClientCallBack() {

					@Override
					public void handle(int statudCode, String result) {
						if (MyChatClient.CODE_CONNECT_OK == statudCode) {
							Logger.d(TAG,"<after login>Create websocket success");
							// 启动KeepAliveService
							Intent intent = new Intent(mContext, KeepAliveService.class);
							mContext.startService(intent);
						} else if ((MyChatClient.CODE_TIMEOUT == statudCode) || (MyChatClient.CODE_UNREACHABLE == statudCode)) {
							Logger.d(TAG,"<after login>Create websocket failed statudCode = " + statudCode);
							Toast toast = Toast.makeText(mContext, "无法建立连接，请检查网络", Toast.LENGTH_SHORT);
							toast.show();
							Intent intent = new Intent(mContext, MainActivity.class);
							mContext.startActivity(intent);
							finish();
						}
					}
				});
			}
		});
		thread.start();

	}

	private OnClickListener forgetOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ForgetPasswordPage forgetPasswordPage = new ForgetPasswordPage();
				forgetPasswordPage.show(LoginActivity.this);
			}

	};

	private void registerUser(String country, String phone, String password) {
		Logger.i("phone", phone);
		Logger.i("password", password);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cellphone", phone));
		params.add(new BasicNameValuePair("password", password));
		MessageManager manager = MessageManager.getMessageManager();
		TextMessage tm = new TextMessage(Common.RegUrl, params, new HttpCallBack() {
			@Override
			public boolean handle(String result) {
				// 处理返回结果
				try {
					JSONObject jsonResult = new JSONObject(result);
					int code = jsonResult .getInt("code");

					if (code == 200) {
						loginHandler.sendEmptyMessage(_msg_reg_succes);
					} else if (jsonResult.getJSONObject("data").getInt("errorCode")== -10003){
						loginHandler.sendEmptyMessage(_msg_reg_user_duplicate);
					}  else if (jsonResult.getJSONObject("data").getInt("errorCode")== -10039){
						loginHandler.sendEmptyMessage(_msg_reg_user_not_invited);
					} else {
						loginHandler.sendEmptyMessage(_msg_reg_fail);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return true;
			}
		}, new HttpFailedCallBack() {

			@Override
			public boolean handle(int errorCode, String errorMessage) {

				return false;
			}

		});
		manager.sendMessage(tm);
	}


	private void updatePasswd(String country, String phone, String password) {
		Logger.i("phone", phone);
		Logger.i("password", password);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cellphone", phone));
		params.add(new BasicNameValuePair("password", password));
		MessageManager manager = MessageManager.getMessageManager();
		TextMessage tm = new TextMessage(Common.RegUrl, params, new HttpCallBack() {
			@Override
			public boolean handle(String result) {
				// 处理返回结果
				try {
					JSONObject jsonResult = new JSONObject(result);
					int code = jsonResult .getInt("code");

					if (code == 200) {
						loginHandler.sendEmptyMessage(_msg_reg_succes);
					} else if (jsonResult.getJSONObject("data").getInt("errorCode")== -10003){
						loginHandler.sendEmptyMessage(_msg_reg_user_duplicate);
					}  else if (jsonResult.getJSONObject("data").getInt("errorCode")== -10039){
						loginHandler.sendEmptyMessage(_msg_reg_user_not_invited);
					} else {
						loginHandler.sendEmptyMessage(_msg_reg_fail);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return true;
			}
		}, new HttpFailedCallBack() {

			@Override
			public boolean handle(int errorCode, String errorMessage) {

				return false;
			}

		});
		manager.sendMessage(tm);
	}

	private void checkDoctorInfo(String sid, String token){
		MessageManager manager = MessageManager.getMessageManager();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("sid", sid));
		params.add(new BasicNameValuePair("token", token));
		params.add(new BasicNameValuePair("doctorId", sid));
		TextMessage tm = new TextMessage(Common.GetDoctorInfoUrl, params, new SimpleMessage.HttpCallBack() {
			@Override
			public boolean handle(String result) {
				// 处理返回结果
				try {
					JSONObject jsonResult = new JSONObject(result);
					int code = jsonResult .getInt("code");
					Intent intent = new Intent();
					if (code == 200) {
						intent.setClass(LoginActivity.this,MainActivity.class);
						startActivity(intent);
						finish();
					}else if(jsonResult.getJSONObject("data").getInt("errorCode") == -10013 ) {
						intent.setClass(LoginActivity.this, FillBasicInfoActivity.class);
						startActivity(intent);
						finish();
					} else {
						loginHandler.sendEmptyMessage(-1);
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