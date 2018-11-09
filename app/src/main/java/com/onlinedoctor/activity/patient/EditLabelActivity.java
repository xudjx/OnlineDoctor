package com.onlinedoctor.activity.patient;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.view.CommonActionbarCancelRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditLabelActivity extends Activity{
	
	private Context mContext;
	private EditText updateLabelName;
	private TextView rightView;
	private CommonActionbarCancelRelativeLayout cacrl = null;
	private String updateId;
	private String preName;
	private RunDataContainer rdc = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_content);
		rdc = RunDataContainer.getContainer();
		mContext = getApplicationContext();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		updateId = bundle.getString("id");
		preName = bundle.getString("name");
		getView();
		setListener();
	}
	
	private void getView(){
		updateLabelName = (EditText) this.findViewById(R.id.input_content);
		updateLabelName.setText(preName);
		cacrl = (CommonActionbarCancelRelativeLayout) this.findViewById(R.id.edit_content_title);
		cacrl.setTitle(this.getResources().getString(R.string.edit_label));
		cacrl.setRight(this.getResources().getString(R.string.finish_update_label));
		rightView = (TextView) cacrl.getRightView();
	}
	
	private void setListener(){
		rightView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String updateName = updateLabelName.getText().toString();
				if (!updateName.isEmpty()) {
					/*LabelServiceImpl lsImpl = new LabelServiceImpl(mContext, null);
					Label l = new Label();
					l.setLabelName(updateName);
					int ret = lsImpl.updateLabelNameById(Long.valueOf(updateId), updateName);*/
					int ret = rdc.updateLabelNameById(Long.valueOf(updateId), updateName);
					if (ret == -1) {
						Toast.makeText(mContext, "编辑失败", Toast.LENGTH_SHORT).show();
					} else if (ret == -3) {
						Toast.makeText(mContext, "名称已存在", Toast.LENGTH_SHORT).show();
					} else {
						if (ret == -2) {
							Toast.makeText(mContext, "新老分组名称一致", Toast.LENGTH_SHORT).show();
						}
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
