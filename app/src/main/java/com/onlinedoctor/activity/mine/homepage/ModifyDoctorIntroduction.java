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

public class ModifyDoctorIntroduction extends Activity {
	private TextView save_tv;
	private EditText intro_et = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_multi_lines_edittext);
		TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
		TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("介绍");
		save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
		save_tv.setText("");
		intro_et = (EditText)findViewById(R.id.modify_name_cname_et);
		intro_et.setHint("输入介绍信息");

		Intent intent = getIntent();
		String intro = intent.getStringExtra("intro");
		if(!intro.isEmpty()){
			intro_et.setText(intro);
			intro_et.setSelection(intro_et.getText().length());
		}

		backTextView.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View arg0){
				getIntro();
			}
		});
	}

	@Override
	public void onBackPressed() {
		getIntro();
		super.onBackPressed();
	}

	private void getIntro(){
		String intro = intro_et.getText().toString();
		Intent intent = new Intent();
		intent.putExtra("intro", intro);
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
