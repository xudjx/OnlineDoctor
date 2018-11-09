package com.onlinedoctor.adapter.tools;

import java.util.List;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DiseaseSurveyAdapter extends ArrayAdapter<Questionnaire> {
	private int resourceId;
	public DiseaseSurveyAdapter(Context context, int resource,
			List<Questionnaire> objects) {
		super(context, resource, objects);
		resourceId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Questionnaire ds = getItem(position);
		View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView title_tv = (TextView)view.findViewById(R.id.labelname);
		//TextView abstract_tv = (TextView)view.findViewById(R.id.abstract_tv);
		title_tv.setText(ds.getName());
		//abstract_tv.setText("");
		return view;
	}
	
}
