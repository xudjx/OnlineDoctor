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

public class ModifyDoctorCid extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_one_line_edittext);
		TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("编辑CID编号");
		TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
		save_tv.setText("保存");
		final EditText cid_et = (EditText)findViewById(R.id.modify_name_cname_et);
		cid_et.setHint("请输入您的CID编号");

		Intent intent = getIntent();
		String cid = intent.getStringExtra("cid");
		if(!cid.isEmpty()){
			cid_et.setText(cid);
			cid_et.setSelection(cid_et.getText().length());
		}

		save_tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String cid = cid_et.getText().toString();
				Intent intent = new Intent();
				intent.putExtra("cid", cid);
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
