package com.onlinedoctor.activity.patient;

import java.util.List;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.patient.NewPatientListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import com.onlinedoctor.pojo.patient.NewPatient;
import com.onlinedoctor.sqlite.dao.NewPatientInfoServiceImpl;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.umeng.analytics.MobclickAgent;

public class NewPatientActivity extends Activity{

	private RecyclerView lv_contactList = null;
	private CommonActionbarBackableRelativeLayout cabrl = null;
	public NewPatientActivity() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_new_patient);

		lv_contactList = (RecyclerView) this.findViewById(R.id.new_patient_list);
		lv_contactList.setLayoutManager(new LinearLayoutManager(this));
		lv_contactList.setItemAnimator(new DefaultItemAnimator());

		cabrl = (CommonActionbarBackableRelativeLayout) this.findViewById(R.id.patient_info_bar);
		cabrl.setTitle("新的患者");
		cabrl.setRight("");

		initData();
	}
	
	private void initData(){
		final List<NewPatient> list = fillData();
		final NewPatientListAdapter adapter = new NewPatientListAdapter(NewPatientActivity.this, list);
		lv_contactList.setAdapter(adapter);
		/*
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {		
				NewPatient np = (NewPatient) list.get(position);
				String userId = np.getPatientId();
				Intent intent = new Intent(NewPatientActivity.this, PatientInfoActivity.class);
				intent.putExtra("userId", userId);
				startActivity(intent);
			}
		});*/
		//to do 已经取消的抹黑
		
	}
	
	
	
	private List<NewPatient> fillData(){
		NewPatientInfoServiceImpl npImpl = new NewPatientInfoServiceImpl(getApplicationContext(), null);
		return npImpl.listAll();
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
