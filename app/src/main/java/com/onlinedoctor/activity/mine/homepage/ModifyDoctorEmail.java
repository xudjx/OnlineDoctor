package com.onlinedoctor.activity.mine.homepage;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.util.Check;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyDoctorEmail extends Activity {
	private Context mContext = this;
	private EditText email_et = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_one_line_edittext);

		TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
		TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("邮箱");
		TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
		save_tv.setText("");
		email_et = (EditText)findViewById(R.id.modify_name_cname_et);
		email_et.setHint("请输入您的常用邮箱");

		Intent intent = getIntent();
		String email = intent.getStringExtra("email");
		if(!email.isEmpty()){
			email_et.setText(email);
			email_et.setSelection(email_et.getText().length());
		}


		backTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				getEmail();
			}
		});

	}


	@Override
	public void onBackPressed() {
		getEmail();
		super.onBackPressed();
	}


	private void getEmail(){
		String email = email_et.getText().toString();
		if(email.isEmpty()){
			Toast.makeText(mContext, "邮箱不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if(!Check.isEmail(email)){
			Toast.makeText(mContext, "邮箱格式不对", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("email", email);
		setResult(RESULT_OK, intent);
		finish();
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