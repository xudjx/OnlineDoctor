package com.onlinedoctor.activity.patient;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.view.CommonActionbarCancelRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddLabelActivity extends Activity{
	
	private Context mContext;
	private EditText newLabelName;
	private TextView rightView;
	private CommonActionbarCancelRelativeLayout cacrl = null;
	private RunDataContainer rdc = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_content);
		rdc = RunDataContainer.getContainer();
		mContext = getApplicationContext();
		getView();
		setListener();
	}
	
	private void getView(){
		newLabelName = (EditText) this.findViewById(R.id.input_content);
		newLabelName.setHint(this.getResources().getString(R.string.add_label_name_hint));
		cacrl = (CommonActionbarCancelRelativeLayout) this.findViewById(R.id.edit_content_title);
		cacrl.setTitle(this.getResources().getString(R.string.add_label));
		cacrl.setRight(this.getResources().getString(R.string.finish_add_label));
		rightView = (TextView) cacrl.getRightView();
	}
	
	private void setListener(){
		rightView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String newName = newLabelName.getText().toString();
				if (!newName.isEmpty()) {
					/*
					LabelServiceImpl lsImpl = new LabelServiceImpl(mContext, null);
					Label l = new Label();
					l.setLabelName(newName);
					int ret = lsImpl.addLabelOne(l);*/
					Label l = new Label();
					l.setLabelName(newName);
					int ret = rdc.addLabel(l);
					if (ret == -1) {
						Toast.makeText(mContext, "创建失败", Toast.LENGTH_SHORT).show();
					} else if (ret == -2) {
						Toast.makeText(mContext, "标签已存在", Toast.LENGTH_SHORT).show();
					} else if (ret == 0) {
						finish();
					}
				} else {
					Toast.makeText(mContext, "请输入分组名称", Toast.LENGTH_SHORT).show();
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
