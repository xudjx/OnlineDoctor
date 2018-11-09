package com.onlinedoctor.activity.mine.homepage;

import com.onlinedoctor.activity.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class ModifyDoctorAddress extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_one_line_edittext);
		TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("编辑详细地址");
		TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
		save_tv.setText("保存");
		final EditText addr_et = (EditText)findViewById(R.id.modify_name_cname_et);
		addr_et.setHint("请输入您的详细地址");

		Intent intent = getIntent();
		String addr = intent.getStringExtra("addr");
		if(!addr.isEmpty()){
			addr_et.setText(addr);
			addr_et.setSelection(addr_et.getText().length());
		}

		save_tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String addr = addr_et.getText().toString();
				Intent intent = new Intent();
				intent.putExtra("addr", addr);
				setResult(RESULT_OK, intent);
				finish();
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
