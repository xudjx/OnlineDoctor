package com.onlinedoctor.activity.patient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.LabelListAdapter;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;
import com.onlinedoctor.view.CommonActionbarBackableRelativeLayout;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 标签患者和标记患者公用界面
 */
public class LabelActivity extends Activity{
	private Context mContext = null;
	private RecyclerView labelPatientList = null;
	private LabelListAdapter adapter;
	public List<Label> labelList = null;
	public HashMap<Long, Integer> labelCountMap = null;
	public HashSet<Long> patientLabelIdSet = null;
	private CommonActionbarBackableRelativeLayout cabrl = null;
	private String MODE_ADD = "add";
	private String MODE_VIEW = "view";
	private Patient patient = null;
	private String mode = null;

	public List<Label> allLabels = null;
	public HashMap<Integer, Boolean> isSelectedMap = null;

	private RunDataContainer rdc = null;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_labels);
		mContext = getApplicationContext();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mode = bundle.getString("mode");
		if(mode.equals(MODE_ADD)){
			this.patient = (Patient) bundle.getSerializable("patient");
		}
		getView();
		loadData();
		loadToAdapter();
	}
	
	private void getView(){
		labelPatientList = (RecyclerView) this.findViewById(R.id.label_patient_list);
		labelPatientList.setLayoutManager(new LinearLayoutManager(this));
		labelPatientList.setItemAnimator(new DefaultItemAnimator());
		cabrl = (CommonActionbarBackableRelativeLayout) this.findViewById(R.id.patient_info_bar);
		if(mode.equals(MODE_VIEW)) {
			cabrl.setTitle(this.getResources().getString(R.string.title_label_patient));
			cabrl.setRight(this.getResources().getString(R.string.add_label));
		}
		else if(mode.equals(MODE_ADD)){
			cabrl.setTitle(this.getResources().getString(R.string.label_patient));
			//cabrl.setRight(this.getResources().getString(R.string.finish_update_label));
			cabrl.setRight(this.getResources().getString(R.string.add_label));
		}

		registerForContextMenu(labelPatientList);
		TextView rightView = cabrl.getRightView();
		TextView leftView = cabrl.getLeftView();

		if(mode.equals(MODE_VIEW)) {
			rightView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(LabelActivity.this, AddLabelActivity.class);
					startActivity(intent);
				}
			});

		}
		else{
			rightView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(LabelActivity.this, AddLabelActivity.class);
					startActivity(intent);
				}
			});

			leftView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					// 获取勾选的标签，并保存
					saveLabelChoose();
				}
			});
		}

	}

	private void saveLabelChoose(){
		final PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(mContext, null);
		int index = 0;
		for(Iterator<Label> iter = labelList.iterator(); iter.hasNext();index++){
			Label l = iter.next();
			if(isSelectedMap.get(index) && !patientLabelIdSet.contains(l.getId())){
				plr.addRelationInfoOne(patient.getPatientId(), l.getId());
			}
			else if(!isSelectedMap.get(index) && patientLabelIdSet.contains(l.getId())) {
				plr.deleteRelationInfoOne(patient.getPatientId(), l.getId());
			}
		}
		rdc = RunDataContainer.getContainer();
		rdc.updateLabelInBM(patient.getPatientId());
		finish();
	}
	
	public void loadData(){
		LabelServiceImpl lsImpl = new LabelServiceImpl(this, null);
		labelCountMap = new HashMap<Long, Integer>();
		labelList = lsImpl.listLabels();
		PatientLabelRelationServiceImpl plrImpl = new PatientLabelRelationServiceImpl(mContext, null);
		if(mode.equals(MODE_ADD)){
			this.isSelectedMap = new HashMap<Integer, Boolean>();
			this.patientLabelIdSet = plrImpl.getLabelByPatientId(patient.getPatientId());
		}
		int index = 0;
		for(Iterator<Label> iter = labelList.iterator(); iter.hasNext(); index++){
			Label l = iter.next();
			List<String> pl = plrImpl.listPatientByLabelId(l.getId());
			labelCountMap.put(l.getId(), pl.size());
			if(mode.equals(MODE_ADD)){
				if(patientLabelIdSet.contains(l.getId())){
					this.isSelectedMap.put(index, true);
				}
				else {
					this.isSelectedMap.put(index, false);
				}
			}
		}
		if(labelList.size() == 0) {
			setNoContent(true);
		}
		else{
			setNoContent(false);
		}
	}

	public void setNoContent(boolean isShow){
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.label_no_content_layout);
		if(isShow){
			rl.setVisibility(View.VISIBLE);
			ImageView iv = (ImageView) findViewById(R.id.label_no_content_pic);
			iv.setImageResource(R.drawable.patient_no_content);
			//iv.setImageResource(R.drawable.label_no_content);
			//ivFont.setImageResource(R.drawable.label_no_content_font);
		}
		else{
			rl.setVisibility(View.GONE);
		}
	}
	
	private void loadToAdapter(){
		adapter = new LabelListAdapter(LabelActivity.this, labelList, labelCountMap, mode, this);
		labelPatientList.setAdapter(adapter);
	}

	@Override
	protected void onRestart(){
		super.onRestart();
		loadData();
		adapter.updateListView(labelList, labelCountMap);
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

