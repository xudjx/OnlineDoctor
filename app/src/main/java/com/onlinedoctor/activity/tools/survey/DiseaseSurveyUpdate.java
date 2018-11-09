package com.onlinedoctor.activity.tools.survey;

import org.json.JSONException;
import org.json.JSONObject;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.tools.SurveyNetManager;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;
import com.onlinedoctor.sqlite.dao.QuestionnaireImpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DiseaseSurveyUpdate extends Activity {
	private String TAG = "DiseaseSurveyUpdate";
	private EditText title_et = null;
	private EditText abstractStr_et = null;
	private TextView save_tv = null;
	private Button createBtn = null;
	private Context mContext = null;
	private FrameLayout progressBarHolder;
	private int globalId;
	private int localId;
	String questionJson;
	
	private final Handler SurveyUpdateHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
	        progressBarHolder.setVisibility(View.GONE);
			if(msg.what == SurveyNetManager._msg_update_questionnaire){
				try {
					JSONObject jo = new JSONObject(msg.obj.toString());
					long time = jo.getLong("time");
					int globalId = jo.getInt("id");
					Questionnaire q = new Questionnaire();
					q.setTimestamp(time);
					q.setId(globalId);
					q.localId = -1;
					QuestionnaireImpl dsImpl = new QuestionnaireImpl(mContext, null);
					if (dsImpl.updateQuestionnaire(q)){
						Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(mContext, "修改失败", Toast.LENGTH_SHORT).show();
					}
					DiseaseSurveyUpdate.this.finish();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(mContext, "无法解析服务器数据", Toast.LENGTH_SHORT).show();
				}
			}else if (msg.what == SurveyNetManager._msg_fail) {
				Toast.makeText(mContext, "获取服务器数据失败", Toast.LENGTH_SHORT).show();
			} else if (msg.what ==SurveyNetManager._msg_unknown_exception){
				Toast.makeText(mContext, "未知错误", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disease_survey_create_new);
		mContext = DiseaseSurveyUpdate.this;
		TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("更新问卷");
		save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
		save_tv.setText("");
		title_et = (EditText) findViewById(R.id.et_survey_title);
		//abstractStr_et = (EditText) findViewById(R.id.et_survey_abstract);
		createBtn = (Button)findViewById(R.id.bt_survey_create);
		createBtn.setText("更新");
		progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
		update();
		//创建模板标题和摘要
		Log.i(TAG,"before createSurvey()");
		createSurvey();
	}
	
	private void update(){
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		String abstractStr = intent.getStringExtra("abstractStr");
		title_et.setText(title);
		//abstractStr_et.setText(abstractStr);
		globalId = intent.getIntExtra("globalId", -1);
		localId = intent.getIntExtra("localId", -1);
		questionJson = intent.getStringExtra("questionJson");
	}
	private void createSurvey(){
		createBtn.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View arg0) {
				
				String strTitle = title_et.getText().toString();
				//String strAbstract = abstractStr_et.getText().toString();
				if(strTitle.isEmpty()){
					Log.d(TAG, "标题为空");
					Toast.makeText(DiseaseSurveyUpdate.this, "标题不能为空", Toast.LENGTH_SHORT).show();
					return;
				}else{
					Intent intent = null;
					Log.d(TAG,"将新创建的空的调查问卷保存到db");
					QuestionnaireImpl dsImpl = new QuestionnaireImpl(mContext, null);
					Questionnaire q = new Questionnaire();
					q.setId(globalId);
					q.localId = localId;
					q.setName(strTitle);
					q.setTimestamp(-1);
					dsImpl.updateQuestionnaire(q);
					SurveyNetManager.update(localId,strTitle, questionJson, globalId, SurveyUpdateHandler);
			        progressBarHolder.setVisibility(View.VISIBLE);
				}
			}
		});
	}
}
