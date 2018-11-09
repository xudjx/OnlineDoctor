package com.onlinedoctor.activity.mine.wallet;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.MyWalletNetManager;
import com.onlinedoctor.pojo.mine.MyWallet;
import com.onlinedoctor.sqlite.dao.MyWalletServiceImpl;
import com.onlinedoctor.util.EditTextUtil;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WithdrawActivity extends Activity {

	private EditText et_amount;
	private TextView tv_type;
	String guid,type;
	Button btn_submit;
	private FrameLayout progressBarHolder;
	private CommonActionbarBackableRelativeLayout title;
	private MyWalletServiceImpl myWalletImpl = null;
	private final Context mContext = this;
	Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			progressBarHolder.setVisibility(View.GONE);
			if(msg.what == MyWalletNetManager._msg_withdraw){
				Toast.makeText(WithdrawActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
				WithdrawActivity.this.finish();
			}else if (msg.what == MyWalletNetManager._msg_money_not_enough) {
			
				Toast.makeText(WithdrawActivity.this, "余额不足", Toast.LENGTH_SHORT).show();
			}else if (msg.what == MyWalletNetManager._msg_fail) {
			
				Toast.makeText(WithdrawActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
			} else if (msg.what ==MyWalletNetManager._msg_unknown_exception){
				Toast.makeText(WithdrawActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_withdraw);
		title = (CommonActionbarBackableRelativeLayout) findViewById(R.id.title);
		((TextView) findViewById(R.id.actionbar_common_backable_title_tv))
		.setText("提现");
		((TextView) findViewById(R.id.actionbar_common_backable_right_tv))
		.setVisibility(View.GONE);

		myWalletImpl = new MyWalletServiceImpl(this, null);
		final MyWallet myWallet = myWalletImpl.get(1);
		float available = myWallet.getAvailable();
		available = (float)(Math.round(available*100))/100;

		et_amount = (EditText)findViewById(R.id.et_amount);
		EditTextUtil.valueAfterDot(et_amount, 2);

		tv_type = (TextView)findViewById(R.id.tv_type);
		btn_submit = (Button)findViewById(R.id.submit);
		progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
		Intent intent = getIntent();
		guid = intent.getStringExtra("guid");
		type = intent.getStringExtra("type");
		tv_type.setText(type);


		et_amount.setHint(String.format(getResources().getString(R.string.max_withdraw), available));
		et_amount.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				for (int i = 0; i < s.length(); i++)
					if (s.charAt(i) == '.') {
						if (s.length() > i + 3) {
							s.delete(i + 3, s.length() - 1);
						}
					}
				if (s.length() == 0) {
					btn_submit.setBackground(getResources().getDrawable(R.drawable.gray_blue));
					btn_submit.setEnabled(false);
				} else {
					btn_submit.setBackground(getResources().getDrawable(R.drawable.shape_blue));
					btn_submit.setEnabled(true);
				}
			}

		});
		btn_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(et_amount.getText().length()>0) {
					float withdraw = Float.parseFloat(et_amount.getText().toString());
					if(Float.compare(withdraw, 0.0f) == 0){
						Toast.makeText(mContext,"提现金额不能为0!",Toast.LENGTH_SHORT).show();
					}else if(withdraw <= myWallet.getAvailable()) {
						progressBarHolder.setVisibility(View.VISIBLE);
						MyWalletNetManager.withdraw(et_amount.getText().toString(), guid, mhandler);
					}else{
						Toast.makeText(mContext, "提现金额不能超过可用金额!", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(mContext, "提现金额不能为空!", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
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
