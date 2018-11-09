package com.onlinedoctor.activity.mine.homepage;

import com.onlinedoctor.activity.R;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyDoctorName extends Activity {
	private Context mContext = this;
	private EditText name_et = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_one_line_edittext);
		TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
		TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("姓名");
		TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
		save_tv.setText("");
		name_et = (EditText)findViewById(R.id.modify_name_cname_et);
		name_et.setHint("请输入您的名字");

		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		if(!name.isEmpty()){
			name_et.setText(name);
			name_et.setSelection(name_et.getText().length());
		}

		backTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				getName();
			}
		});
	}

	@Override
	public void onBackPressed() {
		getName();
		super.onBackPressed();
	}

	private void getName(){
		String name = name_et.getText().toString();
		if(name.isEmpty()){
			Toast.makeText(mContext,"名字不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("name", name);
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
