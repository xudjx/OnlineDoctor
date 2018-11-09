package com.onlinedoctor.activity.chats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.widget.TextView;

import com.onlinedoctor.adapter.ForwardAdapter;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.view.ForwardDialog;
import com.umeng.analytics.MobclickAgent;

public class ForwardActivity extends AppCompatActivity {

	private RecyclerView messgeListView;
	private Context context;
	private ForwardAdapter adapter;
	private PatientMessage forwardMessage;
	private List<BriefMessagePojo> messagePojos = null;

	public static final int REQUESTCODE_SELECTOR = 10001;
	public static final String CLASS_NAME_STRING = "FORWARD_ACTIVITY";

	private RunDataContainer container = RunDataContainer.getContainer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting_forward);
		context = ForwardActivity.this;

		/*
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("转发");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// Handle Back Navigation :D
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});*/

		TextView add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
		add_tv.setText("");
		TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
		title_tv.setText("转发");

		init();
	}

	private void init() {
		Intent intent = this.getIntent();
		forwardMessage = (PatientMessage) intent.getSerializableExtra("ForwardMessage");
		messgeListView = (RecyclerView) findViewById(R.id.chatRecyclerView);
		messgeListView.setLayoutManager(new LinearLayoutManager(this));
		messagePojos = new ArrayList<BriefMessagePojo>();
		messagePojos = container.getBriefMessage();
		adapter = new ForwardAdapter(context, messagePojos, forwardMessage);
		messgeListView.setAdapter(adapter);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE_SELECTOR && resultCode == RESULT_OK && data!= null) {
			Bundle bundle = data.getExtras();
			List<Patient> patientList = (List<Patient>) bundle.get("patientList");
			Logger.i("PatientSelector Response:", patientList.toString());
			List<PatientMessage> messages = new ArrayList<PatientMessage>();
			for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext();) {
				Patient patient = iterator.next();
				PatientMessage message = new PatientMessage(forwardMessage);
				message.setGuid(UUID.randomUUID().toString());
				message.setToID(patient.getPatientId());
				messages.add(message);
			}
			ForwardDialog dialog = new ForwardDialog(context, R.style.Theme_dialog, messages, patientList);
			dialog.show();
		}
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
