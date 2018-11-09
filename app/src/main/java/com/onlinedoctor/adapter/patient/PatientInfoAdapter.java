package com.onlinedoctor.adapter.patient;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.adapter.MedicalRecordAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.pojo.patient.Record;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.service.LabelService;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;
import com.onlinedoctor.view.MedicalRecordGridView;
import com.onlinedoctor.view.photoview.ViewPagerActivity;

public class PatientInfoAdapter extends BaseAdapter{
	private final Context mContext;
	private List<List<Record>> list;
	private DisplayImageOptions uilOptions;
	private ListView areaCheckListView;
	private String patientId;
	private PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(MyApplication.context, null);
	private LabelService l = new LabelServiceImpl(MyApplication.context, null);
	private RunDataContainer rdc = RunDataContainer.getContainer();
	private List<String> allRecrodUrlList = null;
	private List<Integer> oneDateCount = null;
	private HashMap<Long,List<String>> prescriptions = null;

	private HashMap<Integer, MedicalRecordAdapter> gridViews = null;
	
	private class ViewHolder2{
		TextView in_record_tv_time, in_record_tv_type;
		MedicalRecordGridView gridView;
	}
	
	public PatientInfoAdapter(Context context, String patientId, List<List<Record>> list, List<String> allRecordUrlList,
							  HashMap<Long,List<String>> prescriptions) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.list = list;
		this.patientId = patientId;
		this.allRecrodUrlList = allRecordUrlList;
		this.prescriptions = prescriptions;
		oneDateCount = new ArrayList<Integer>();
		gridViews = new HashMap<Integer, MedicalRecordAdapter>();
		int tmp = 0;
		oneDateCount.add(tmp);
		for(Iterator<List<Record>> iter = list.iterator(); iter.hasNext();){
			List<Record> oneList = iter.next();
			tmp += oneList.size();
			oneDateCount.add(tmp);
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder2 vh2 = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_referral_info, null);
			vh2 = new ViewHolder2();
			vh2.in_record_tv_time = (TextView) convertView.findViewById(R.id.in_record_tv_time);
			vh2.gridView = (MedicalRecordGridView) convertView.findViewById(R.id.gv_pickedimg);
			convertView.setTag(vh2);
		}
		else{
			vh2 = (ViewHolder2) convertView.getTag();
		}

		long created = list.get(position).get(0).getCreated();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String date = sdf.format(new Date(created));
		vh2.in_record_tv_time.setText(date);
		vh2.gridView.setVisibility(View.VISIBLE);
		final int timeLinePosition = position;
		if(!this.gridViews.containsKey(position)){
			MedicalRecordAdapter medicalRecordAdapter = new MedicalRecordAdapter(mContext, list.get(position));
			gridViews.put(position, medicalRecordAdapter);
			vh2.gridView.setAdapter(medicalRecordAdapter);
		}
		else{
			MedicalRecordAdapter medicalRecordAdapter = gridViews.get(position);
			vh2.gridView.setAdapter(medicalRecordAdapter);
		}
		final int final_date_position = position;
		vh2.gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(mContext, ViewPagerActivity.class);
				Bundle bundle = new Bundle();
				Record record = list.get(timeLinePosition).get(position);
				if(record.getRecordType() == Record.TYPE_PRESCRIPTION){
					bundle.putSerializable("urlist",(ArrayList<String>)prescriptions.get(record.getId()));
				}else{
					bundle.putSerializable("urlist",(ArrayList<String>)allRecrodUrlList);
					bundle.putInt("currentItem", oneDateCount.get(final_date_position) + position);
				}
				intent.putExtras(bundle);
				Log.d("UrlTest", String.valueOf(allRecrodUrlList.get(oneDateCount.get(final_date_position) + position)));
				mContext.startActivity(intent);
			}
		});

		return convertView;
	}
}
