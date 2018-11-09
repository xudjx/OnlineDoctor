package com.onlinedoctor.activity.mine.wallet;

import java.util.regex.Pattern;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.MyWalletNetManager;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddAlipayActivity extends Activity {
	private TextView account;
	private Button btn_add;
	
	private CommonActionbarBackableRelativeLayout title;
	
	private FrameLayout progressBarHolder;

	private Context mContext = this;
	Handler mhandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			progressBarHolder.setVisibility(View.GONE);
			if(msg.what == MyWalletNetManager._msg_update_alipay){
				progressBarHolder.setVisibility(View.GONE);
				AddAlipayActivity.this.finish();
			}else if (msg.what == MyWalletNetManager._msg_fail) {
				String tipInfo = getResources().getString(R.string.wallet_fail_request);
				Toast.makeText(mContext, tipInfo, Toast.LENGTH_SHORT).show();
			} else if (msg.what == MyWalletNetManager._msg_unknown_exception) {
				String tipInfo = getResources().getString(R.string.wallet_unknown_error);
				Toast.makeText(mContext, tipInfo, Toast.LENGTH_SHORT).show();
			}  else if (msg.what == MyWalletNetManager._msg_invaild_alipay) {
				String tipInfo = getResources().getString(R.string.wallet_invaild_alipay);
				Toast.makeText(mContext, tipInfo, Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.frag_add_alipay);
		account = (TextView)findViewById(R.id.zh);
		btn_add = (Button)findViewById(R.id.add);
		progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
		btn_add.setEnabled(false);
		
		title = (CommonActionbarBackableRelativeLayout)findViewById(R.id.title);
		((TextView)findViewById(R.id.actionbar_common_backable_title_tv)).setText("支付宝");
		((TextView)findViewById(R.id.actionbar_common_backable_right_tv)).setVisibility(View.GONE);
		Intent intent = getIntent();
		if(intent.getStringExtra("alipay")!=null) {
			account.setText(intent.getStringExtra("alipay"));
		}
		
		btn_add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(checkAlipay(account.getText().toString())) {
					progressBarHolder.setVisibility(View.VISIBLE);
					MyWalletNetManager.updateAlipay(account.getText().toString(), mhandler);
				}
			}
		});
		
		account.addTextChangedListener(new TextWatcher() {
			
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
				if(checkAlipay(s.toString())) {
					btn_add.setEnabled(true);
					btn_add.setBackground(getResources().getDrawable(R.drawable.shape_blue));
				}else {
					btn_add.setEnabled(false);
					btn_add.setBackground(getResources().getDrawable(R.drawable.gray_blue));
				}
			}
		});
	}
	
	
	boolean checkAlipay(String s) {
		Pattern p1 = Pattern.compile("1\\d{10}");
		Pattern p2  = Pattern.compile("[a-zA-Z0-9-_\\.]+@[a-zA-Z0-9-_\\.]+\\.[a-zA-Z0-9-_\\.]+");
		return p1.matcher(s).matches() || p2.matcher(s).matches();
		
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
