package cn.smssdk.gui;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.tools.FakeActivity;
import com.onlinedoctor.net.mine.ModifyPasswordNetManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;

import static com.mob.tools.utils.R.getBitmapRes;
import static com.mob.tools.utils.R.getIdRes;
import static com.mob.tools.utils.R.getLayoutRes;
import static com.mob.tools.utils.R.getStringRes;
import static com.mob.tools.utils.R.getStyleRes;

/** 短信注册页面 */
public class 	ForgetPasswordPage extends FakeActivity implements OnClickListener {

	// 默认使用中国区号
	private static final String DEFAULT_COUNTRY_ID = "42";

	private EventHandler callback;

	// 国家
	private TextView tvCountry;
	// 手机号码
	private EditText etPhoneNum;
	// 国家编号
	private TextView tvCountryNum;

	//
	private EditText etIdentify;

	// clear号码
	private ImageView ivClear;
	// 密码
	private EditText etPassword;
	// clear密码
	private ImageView ivClearPassswd;
	// 密码
	private EditText etPassword_again;
	// clear密码
	private ImageView ivClearPassswd_again;

	// 下一步按钮
	private Button btnNext;

	// 获取验证码
	private Button btnCode;
	private String currentId;
	private String currentCode;

	// 国家号码规则
	private HashMap<String, String> countryRules;
	private Dialog pd;

	//
	private static final int RETRY_INTERVAL = 60;
	private int time = RETRY_INTERVAL;
	private TextView tvUnreceiveIdentify;
	private Button btnSounds;

	private EventHandler handler = new EventHandler() {
		@SuppressWarnings("unchecked")
		public void afterEvent(final int event, final int result,
							   final Object data) {
			runOnUIThread(new Runnable() {
				public void run() {
					if (pd != null && pd.isShowing()) {
						pd.dismiss();
					}
					if (result == SMSSDK.RESULT_COMPLETE) {
						if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
							// 请求支持国家列表
							onCountryListGot((ArrayList<HashMap<String, Object>>) data);
						} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
							// 请求验证码后，进入倒计时阶段
							// TODO
						}
					} else {
						if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE && data != null
								&& (data instanceof UserInterruptException)) {
							// 由于此处是开发者自己决定要中断发送的，因此什么都不用做
							return;
						}

						// 根据服务器返回的网络错误，给toast提示
						try {
							((Throwable) data).printStackTrace();
							Throwable throwable = (Throwable) data;

							JSONObject object = new JSONObject(
									throwable.getMessage());
							String des = object.optString("detail");
							if (!TextUtils.isEmpty(des)) {
								Toast.makeText(activity, des, Toast.LENGTH_SHORT).show();
								return;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 如果木有找到资源，默认提示
						int resId = getStringRes(activity, "smssdk_network_error");
						if (resId > 0) {
							Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
		}
	};

	private Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case ModifyPasswordNetManager.UPDATE_SUCCESSFUL:
					Log.d("result", msg.obj.toString());
					Toast.makeText(activity, "重置密码成功", Toast.LENGTH_SHORT).show();
					finish();
					break;
				case ModifyPasswordNetManager.UPDATE_FAIL:
					Toast.makeText(activity, "重置密码失败", Toast.LENGTH_SHORT).show();
					Log.d("result", msg.obj.toString());
					break;
				default:
					break;
			}

		}
	};

	private TextWatcher phoneNumTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			if (s.length() > 0) {
				btnCode.setEnabled(true);
				btnNext.setEnabled(true);
				ivClear.setVisibility(View.VISIBLE);
				int resId = getBitmapRes(activity, "smssdk_btn_enable");
				if (resId > 0) {
					btnCode.setBackgroundResource(resId);
					btnNext.setBackgroundResource(resId);
				}
			} else {
				btnCode.setEnabled(false);
				btnNext.setEnabled(false);
				ivClear.setVisibility(View.GONE);
				int resId = getBitmapRes(activity, "smssdk_btn_disenable");
				if (resId > 0) {
					btnCode.setBackgroundResource(resId);
					btnNext.setBackgroundResource(resId);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
	};

//	private TextWatcher identifyCodeTextWatcher = new TextWatcher() {
//		@Override
//		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//		}
//
//		@Override
//		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable editable) {
//
//		}
//	};

	private TextWatcher passwdTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			if (s.length() > 0) {
				btnNext.setEnabled(true);
				ivClearPassswd.setVisibility(View.VISIBLE);
				int resId = getBitmapRes(activity, "smssdk_btn_enable");
				if (resId > 0) {
					btnNext.setBackgroundResource(resId);
				}
			} else {
				btnNext.setEnabled(false);
				ivClearPassswd.setVisibility(View.GONE);
				int resId = getBitmapRes(activity, "smssdk_btn_disenable");
				if (resId > 0) {
					btnNext.setBackgroundResource(resId);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
	};

	private TextWatcher passwdAgainTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			if (s.length() > 0) {
				btnNext.setEnabled(true);
				ivClearPassswd_again.setVisibility(View.VISIBLE);
				int resId = getBitmapRes(activity, "smssdk_btn_enable");
				if (resId > 0) {
					btnNext.setBackgroundResource(resId);
				}
			} else {
				btnNext.setEnabled(false);
				ivClearPassswd_again.setVisibility(View.GONE);
				int resId = getBitmapRes(activity, "smssdk_btn_disenable");
				if (resId > 0) {
					btnNext.setBackgroundResource(resId);
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
	};

	public void setRegisterCallback(EventHandler callback) {
		this.callback = callback;
	}

	public void show(Context context) {
		super.show(context, null);
	}

	public void onCreate() {
		int resId = getLayoutRes(activity, "forget_password");
		if (resId > 0) {
			activity.setContentView(resId);
			currentId = DEFAULT_COUNTRY_ID;

			//Back
			resId = getIdRes(activity, "ll_back");
			View llBack = activity.findViewById(resId);

			//Title
			resId = getIdRes(activity, "tv_title");
			TextView tv = (TextView) activity.findViewById(resId);
			resId = getStringRes(activity, "smssdk_input_phone_number");
			if (resId > 0) {
				tv.setText(resId);
			}

			//Country: LinearLayout
			resId = getIdRes(activity, "rl_country");
			View viewCountry = activity.findViewById(resId);

			//Button: Next
			resId = getIdRes(activity, "btn_next");
			btnNext = (Button) activity.findViewById(resId);

			resId = getIdRes(activity, "tv_unreceive_identify");
			tvUnreceiveIdentify = (TextView) activity.findViewById(resId);

			// EditText: identify
			resId = getIdRes(activity, "et_put_identify");
			etIdentify = (EditText) activity.findViewById(resId);

			//Button: Sound
			resId = getIdRes(activity, "btn_sounds");
			btnSounds = (Button) findViewById(resId);

			//Identify Code
			resId = getIdRes(activity, "btn_get_identify_code");
			btnCode = (Button) activity.findViewById(resId);

			//
			resId = getIdRes(activity, "tv_country");
			tvCountry = (TextView) activity.findViewById(resId);

			//Countries list
			String[] country = getCurrentCountry();
			// String[] country = SMSSDK.getCountry(currentId);
			if (country != null) {
				currentCode = country[1];
				tvCountry.setText(country[0]);
			}
			resId = getIdRes(activity, "tv_country_num");
			tvCountryNum = (TextView) activity.findViewById(resId);
			tvCountryNum.setText("+" + currentCode);

			//Phone number
			resId = getIdRes(activity, "et_write_phone");
			etPhoneNum = (EditText) activity.findViewById(resId);
			etPhoneNum.setText("");
			etPhoneNum.addTextChangedListener(phoneNumTextWatcher);
			etPhoneNum.requestFocus();

			//Phone number clear image
			resId = getIdRes(activity, "iv_clear");
			ivClear = (ImageView) activity.findViewById(resId);

			//add password editText
			resId = getIdRes(activity, "et_write_password");
			etPassword = (EditText) activity.findViewById(resId);
			etPassword.setText("");
			etPassword.addTextChangedListener(passwdTextWatcher);

			// password clear image
			resId = getIdRes(activity, "iv_clear_password");
			ivClearPassswd = (ImageView) activity.findViewById(resId);

			// EditText write password again
			resId = getIdRes(activity, "et_write_password_again");
			etPassword_again = (EditText) activity.findViewById(resId);
			etPassword_again.setText("");
			etPassword_again.addTextChangedListener(passwdAgainTextWatcher);

			//password again clear image
			resId = getIdRes(activity, "iv_clear_password_again");
			ivClearPassswd_again = (ImageView) activity.findViewById(resId);

			llBack.setOnClickListener(this);
			btnNext.setOnClickListener(this);
			btnCode.setOnClickListener(this);
			ivClear.setOnClickListener(this);
			ivClearPassswd.setOnClickListener(this);
			ivClearPassswd_again.setOnClickListener(this);
			viewCountry.setOnClickListener(this);
		}
	}

	private String[] getCurrentCountry() {
		String mcc = getMCC();
		String[] country = null;
		if (!TextUtils.isEmpty(mcc)) {
			country = SMSSDK.getCountryByMCC(mcc);
		}

		if (country == null) {
			Log.w("SMSSDK", "no country found by MCC: " + mcc);
			country = SMSSDK.getCountry(DEFAULT_COUNTRY_ID);
		}
		return country;
	}

	private String getMCC() {
		TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		// 返回当前手机注册的网络运营商所在国家的MCC+MNC. 如果没注册到网络就为空.
		String networkOperator = tm.getNetworkOperator();

		// 返回SIM卡运营商所在国家的MCC+MNC. 5位或6位. 如果没有SIM卡返回空
		String simOperator = tm.getSimOperator();

		String mcc = null;
		if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() >= 5) {
			mcc = networkOperator.substring(0, 3);
		}

		if (TextUtils.isEmpty(mcc)) {
			if (!TextUtils.isEmpty(simOperator) && simOperator.length() >= 5) {
				mcc = simOperator.substring(0, 3);
			}
		}
		return mcc;
	}

	public void onResume() {
		SMSSDK.registerEventHandler(handler);
	}

	public void onPause() {
		SMSSDK.unregisterEventHandler(handler);
	}

	public void onClick(View v) {
		int id = v.getId();
		int id_ll_back = getIdRes(activity, "ll_back");
		int id_rl_country = getIdRes(activity, "rl_country");
		int id_btn_next = getIdRes(activity, "btn_next");
		int id_btn_code = getIdRes(activity,"btn_get_identify_code");
		int id_btn_sounds = getIdRes(activity, "btn_sounds");
		int id_iv_clear = getIdRes(activity, "iv_clear");
		int id_passwd_clear = getIdRes(activity,"iv_clear_password");
		int id_passwd_again_clear = getIdRes(activity,"iv_clear_password_again");


		if (id == id_ll_back) {
			// back
			finish();
		} else if (id == id_rl_country) {
			// 国家列表
			CountryPage countryPage = new CountryPage();
			countryPage.setCountryId(currentId);
			countryPage.setCountryRuls(countryRules);
			countryPage.showForResult(activity, null, this);
		}else if (id == id_btn_next) {

			// 请求发送短信验证码
			if (countryRules == null || countryRules.size() <= 0) {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				pd = CommonDialog.ProgressDialog(activity);
				if (pd != null) {
					pd.show();
				}

				SMSSDK.getSupportedCountries();
			}else{
				if(!checkBtnNext()) {
					return;
				}else{
					/**
					 * 向后台服务器发送phone、identify code、 passwd
					 */
					String phone = etPhoneNum.getText().toString().trim().replaceAll("\\s*", "");
					//String code = tvCountryNum.getText().toString().trim();
					String verifyCode = etIdentify.getText().toString();
					String newPassword = etPassword.getText().toString();
					//String newPassword_again = etPassword_again.getText().toString();
					ModifyPasswordNetManager.update(phone, verifyCode, newPassword, myHandler);

				}
			}

		}else if (id == id_btn_code) {
			// 请求发送短信验证码
			if (countryRules == null || countryRules.size() <= 0) {
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
				pd = CommonDialog.ProgressDialog(activity);
				if (pd != null) {
					pd.show();
				}
				SMSSDK.getSupportedCountries();
			} else {
				String phone = etPhoneNum.getText().toString().trim().replaceAll("\\s*", "");
				String code = tvCountryNum.getText().toString().trim();
				if(checkPhoneNum(phone, code)) {
					showDialog(phone, code);
				}
			}
		} else if (id == id_iv_clear) {
			// 清除电话号码输入框
			etPhoneNum.getText().clear();
		} else if (id == id_passwd_clear){
			// 清除密码
			etPassword.getText().clear();
		} else if (id == id_passwd_again_clear) {
			//清除密码确认
			etPassword_again.getText().clear();
		}
	}

	@SuppressWarnings("unchecked")
	public void onResult(HashMap<String, Object> data) {
		if (data != null) {
			int page = (Integer) data.get("page");
			if (page == 1) {
				// 国家列表返回
				currentId = (String) data.get("id");
				countryRules = (HashMap<String, String>) data.get("rules");
				String[] country = SMSSDK.getCountry(currentId);
				if (country != null) {
					currentCode = country[1];
					tvCountryNum.setText("+" + currentCode);
					tvCountry.setText(country[0]);
				}
			} else if (page == 2) {
				// 验证码校验返回
				Object res = data.get("res");
				HashMap<String, Object> phoneMap = (HashMap<String, Object>) data.get("phone");
				if (res != null && phoneMap != null) {
					int resId = getStringRes(activity, "smssdk_your_ccount_is_verified");
					if (resId > 0) {
						//Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
					}
					
//					String password = etPassword.getText().toString().trim();
//					phoneMap.put("password", password);
//					if (callback != null) {
//						callback.afterEvent(
//								SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE,
//								SMSSDK.RESULT_COMPLETE, phoneMap);
//					}
					finish();
				}
			}
		}
	}

	private void onCountryListGot(ArrayList<HashMap<String, Object>> countries) {
		// 解析国家列表
		for (HashMap<String, Object> country : countries) {
			String code = (String) country.get("zone");
			String rule = (String) country.get("rule");
			if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
				continue;
			}

			if (countryRules == null) {
				countryRules = new HashMap<String, String>();
			}
			countryRules.put(code, rule);
		}
		// 检查手机号码
		String phone = etPhoneNum.getText().toString().trim().replaceAll("\\s*", "");
		String code = tvCountryNum.getText().toString().trim();
		if(checkPhoneNum(phone, code)) {
			showDialog(phone, code);
		}
	}

	/** 分割电话号码 */
	private String splitPhoneNum(String phone) {
		StringBuilder builder = new StringBuilder(phone);
		builder.reverse();
		for (int i = 4, len = builder.length(); i < len; i += 5) {
			builder.insert(i, ' ');
		}
		builder.reverse();
		return builder.toString();
	}



	/** 检查密码 */
	private boolean checkPassword(String password){
		if (TextUtils.isEmpty(password)) {
			int resId = getStringRes(activity, "smssdk_regist_write_password");
			if (resId > 0) {
				Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
			}
			return false;
		}else{
			return true;
		}
	}


	/** 是否请求发送验证码，对话框 */
	public void showDialog(String phone, String code) {
		int resId = getStyleRes(activity, "CommonDialog");
		if (resId > 0) {
			if (code.startsWith("+")) {
				code = code.substring(1);
			}
			final String phoneNum = "+" + code + " " + splitPhoneNum(phone);
			final Dialog dialog = new Dialog(getContext(), resId);
			resId = getLayoutRes(activity, "smssdk_send_msg_dialog");
			if (resId > 0) {
				dialog.setContentView(resId);
				resId = getIdRes(activity, "tv_phone");
				((TextView) dialog.findViewById(resId)).setText(phoneNum);
				resId = getIdRes(activity, "tv_dialog_hint");
				TextView tv = (TextView) dialog.findViewById(resId);
				resId = getStringRes(activity, "smssdk_make_sure_mobile_detail");
				if (resId > 0) {
					String text = getContext().getString(resId);
					tv.setText(Html.fromHtml(text));
				}
				resId = getIdRes(activity, "btn_dialog_ok");

				final String fphone = phone;
				final String fcode = code;
				if (resId > 0) {
					((Button) dialog.findViewById(resId))
							.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									// 跳转到验证码页面
									dialog.dismiss();

									if (pd != null && pd.isShowing()) {
										pd.dismiss();
									}
									pd = CommonDialog.ProgressDialog(activity);
									if (pd != null) {
										pd.show();
									}
									Log.e("verification phone ==>>", fphone);
									SMSSDK.getVerificationCode(fcode, fphone.trim(), null);

									// 设置倒计时
									countDown();

									// btncode设置灰化
									btnCode.setEnabled(false);
									int resId = getBitmapRes(activity, "smssdk_btn_disenable");
									if (resId > 0) {
										btnCode.setBackgroundResource(resId);
									}

								}
							});
				}
				resId = getIdRes(activity, "btn_dialog_cancel");
				if (resId > 0) {
					((Button) dialog.findViewById(resId)).setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									dialog.dismiss();
								}
							});
				}
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
			}
		}
	}


	/** 倒数计时 */
	private void countDown() {
		runOnUIThread(new Runnable() {

			public void run() {

				tvUnreceiveIdentify.setVisibility(View.VISIBLE);

				time--;
				if (time == 0) {
					int resId = getStringRes(activity, "smssdk_unreceive_identify_code");
					if (resId > 0) {
						String unReceive = getContext().getString(resId, time);
						tvUnreceiveIdentify.setText(Html.fromHtml(unReceive));
					}
					tvUnreceiveIdentify.setEnabled(true);

					btnSounds.setVisibility(View.VISIBLE);
					time = RETRY_INTERVAL;
				} else {
					int resId = getStringRes(activity, "smssdk_receive_msg");
					if (resId > 0) {
						String unReceive = getContext().getString(resId, time);
						tvUnreceiveIdentify.setText(Html.fromHtml(unReceive));
					}
					tvUnreceiveIdentify.setEnabled(false);
					runOnUIThread(this, 1000);
				}
			}
		}, 1000);
	}


	private boolean checkBtnNext(){
		String phoneNum = etPhoneNum.getText().toString().trim().replaceAll("\\s*", "");
		String code = tvCountryNum.getText().toString().trim();
		String identifyCode = etIdentify.getText().toString();
		String passwd = etPassword.getText().toString();
		String passwd_again = etPassword_again.getText().toString();

		if( phoneNum.isEmpty() ){
			Toast.makeText(activity, "手机号不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(! checkPhoneNum(phoneNum, code)){
			//Toast.makeText(activity, "手机号格式不对", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(identifyCode.isEmpty()){
			Toast.makeText(activity, "验证码不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(passwd.isEmpty()){
			Toast.makeText(activity, "新密码不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(passwd_again.isEmpty()){
			Toast.makeText(activity, "确认密码不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(! TextUtils.equals(passwd, passwd_again)){
			Toast.makeText(activity, "新密码和确认密码不相同", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}


	/** 检查电话号码 */
	private boolean checkPhoneNum(String phone, String code) {
		if (code.startsWith("+")) {
			code = code.substring(1);
		}

		if (TextUtils.isEmpty(phone)) {
			int resId = getStringRes(activity, "smssdk_write_mobile_phone");
			if (resId > 0) {
				Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
			}
			return false;
		}

		String rule = countryRules.get(code);
		Pattern p = Pattern.compile(rule);
		Matcher m = p.matcher(phone);
		int resId = 0;
		if (!m.matches()) {
			resId = getStringRes(activity, "smssdk_write_right_mobile_phone");
			if (resId > 0) {
				Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
			}
			return false;
		}
		return true;
		//showDialog(phone, code);
	}

}
