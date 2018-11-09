package com.onlinedoctor.adapter.tools;

import java.util.List;
import java.util.zip.Inflater;

import com.onlinedoctor.activity.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

public class DiseaseSurveyQuestionTypeChoicesAdapter extends ArrayAdapter<EditText> {
	
	private int resourceId;
	private List<EditText> dataList = null;
	private LayoutInflater inflater = null;
	public DiseaseSurveyQuestionTypeChoicesAdapter(Context context,
			int resource, List<EditText> objects) {
		super(context, resource, objects);
		resourceId = resource;
		dataList = objects;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EditText et = getItem(position);
//		View view = inflater.inflate(R.layout.edittext_survey_choices, null);
//		EditText edittext = (EditText)view.findViewById(R.id.et_choice);
//		edittext.setText();
//		return view;
		return et;
	}

}
