package com.onlinedoctor.activity.patient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.onlinedoctor.activity.chats.ChatActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.R.id;
import com.onlinedoctor.adapter.patient.PatientInfoAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.pojo.patient.Record;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientDocRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.RecordServiceImpl;
import com.onlinedoctor.sqlite.service.LabelService;
import com.onlinedoctor.sqlite.service.PatientDocRelationService;
import com.onlinedoctor.sqlite.service.PatientInfoService;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;
import com.onlinedoctor.sqlite.service.RecordService;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.umeng.analytics.MobclickAgent;

public class PatientInfoActivity extends Activity{
	private static final String TAG = "PatientInfoActivity";
	
	private ListView lv = null;
	private TextView labelView = null;
	private TextView commentView = null;
	private CommonActionbarBackableRelativeLayout cabrl = null;
	private List<List<Record>> inputList;
	private PatientInfoAdapter adapter;
	private PatientInfoService patientInfoService = new PatientInfoServiceImpl(MyApplication.context, null);
	private PatientDocRelationService patientDocRelationService = new PatientDocRelationServiceImpl(
			MyApplication.context, null);
	private RecordService recordService = new RecordServiceImpl(MyApplication.context, null);
	private PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(MyApplication.context, null);
	private LabelService l = new LabelServiceImpl(MyApplication.context, null);
	private SharedpreferenceManager spManager = null;
	private Patient patient = null;
	private int mMotionY;
	private String labelString = null;
	private String comment = null;
	private List<Record> allRecordList = null;
	private List<String> allRecordUrlList = null;
	private HashMap<Long, List<String>> prescriptions = null;
	private ImageView noContentView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_info);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		patient = (Patient) bundle.getSerializable("patient");

		lv = (ListView) findViewById(R.id.listView_question);
		TextView btnConversation = (TextView) findViewById(R.id.btn_conversation);
		TextView btnLabel = (TextView) findViewById(R.id.btn_label);
		TextView btnComment = (TextView) findViewById(id.btn_remark);
		noContentView = (ImageView) findViewById(id.patient_no_content_pic);
		cabrl = (CommonActionbarBackableRelativeLayout) findViewById(R.id.patient_info_bar);
		cabrl.setTitle("患者详情");
		cabrl.setRight("");
		TextView nameView = (TextView) findViewById(R.id.nameman);
		nameView.setText(patient.getName());

		labelView = (TextView) findViewById(R.id.labelman);
		commentView = (TextView) findViewById(id.remarkman);

		spManager = SharedpreferenceManager.getInstance();

		btnLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PatientInfoActivity.this, LabelActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("mode", "add");
				bundle.putSerializable("patient", patient);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		btnConversation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(PatientInfoActivity.this, ChatActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("patient", patient);
				bundle.putString("FromActivity", "PatientInfoActivity");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		btnComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PatientInfoActivity.this, EditCommentActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("patient", patient);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		loadData();
	}

	private void loadData(){
		fillData(patient.getPatientId());
		if(allRecordUrlList.size() == 0){
			noContentView.setImageResource(R.drawable.patient_no_content);
			noContentView.setVisibility(View.VISIBLE);
		}
		else{
			noContentView.setVisibility(View.GONE);
		}
		adapter = new PatientInfoAdapter(PatientInfoActivity.this, patient.getPatientId(), inputList, allRecordUrlList, prescriptions);
		lv.setAdapter(adapter);
	}


	private void updateActivity(){
		//need to combine with onCreate
		LabelService ls = new LabelServiceImpl(this, null);
		PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(this, null);
		List<Long> labels = plr.listLabelByPatientId(patient.getPatientId());
		labelString = "";
		for(Iterator<Long> iter = labels.iterator(); iter.hasNext();){
			Long labelId = iter.next();
			Label l = ls.getLabelById(labelId);
			labelString += " " + l.getLabelName() + " |";
		}
		if(labelString.equals("")){
			labelString = "标签： 无";
		}
		else {
			labelString = "标签：" + labelString.substring(0, labelString.length() - 1);
		}
		labelView.setText(labelString);
		PatientInfoService ps = new PatientInfoServiceImpl(this, null);
		patient = ps.viewPatientInfoByPatientId(patient.getPatientId());
		comment = patient.getAllCommentString();
		commentView.setText(comment);
	}
	
	private void fillData(String patientId){
		PatientDoctorRel pdr = null;
		String doctorId = spManager.getOne("keySid");
		pdr = patientDocRelationService.viewRelationInfo(doctorId, patientId);
		if(pdr == null || pdr.status == 0){
			//to do 处理没有关注的情况
			return;
		}

		//病历信息加载
		inputList = new ArrayList<List<Record>>();
		allRecordList = recordService.listRecords(patientId, doctorId);
		allRecordUrlList = new ArrayList<String>();
		prescriptions = new HashMap<>();
		if(allRecordList.size() == 0){
			//to do 
		}
		else{
			long oneDay = 3600000*24, lastDay = -1;
			List<Record> oneList = null;
			for(Iterator<Record> iter = allRecordList.iterator(); iter.hasNext();){
				Record record = iter.next();
				if(record.getRecordType() == Record.TYPE_PRESCRIPTION){
					allRecordUrlList.add(record.getThumbnail());
					List<LinkedTreeMap<String, String>> images = (List<LinkedTreeMap<String, String>>)JsonUtil.jsonToList(record.getRecordPic());
					List<String> urls = new ArrayList<>();
					for(LinkedTreeMap<String,String> item:images){
						urls.add(item.get(Common.KEY_IMAGE_avaterPath));
					}
					prescriptions.put(record.getId(), urls);
					Logger.i(TAG, "prescriptions: " + prescriptions.toString());
				}else{
					allRecordUrlList.add(record.getRecordPic());
				}
				long curDay = record.getCreated() / oneDay;
				if(curDay != lastDay){
					oneList = new ArrayList<Record>();
					inputList.add(oneList);
					lastDay = curDay;
				}
				oneList.add(record);
			}
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
		MobclickAgent.onResume(this);
		updateActivity();
		Log.i(TAG, "onResume");
	}

	@Override
	protected void onStart(){
		super.onStart();
		Log.i(TAG,"onStart");
	}

	@Override
	protected void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
		Log.i(TAG, "onPause");
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}
}
