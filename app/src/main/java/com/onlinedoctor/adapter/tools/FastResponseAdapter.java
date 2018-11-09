package com.onlinedoctor.adapter.tools;

import java.util.List;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.tools.FastResponseMessage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FastResponseAdapter extends ArrayAdapter<FastResponseMessage> {
	private int resourceId;
	
	public FastResponseAdapter(Context context, int resource,
			List<FastResponseMessage> objects) {
		super(context, resource, objects);
		resourceId = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FastResponseMessage frm = getItem(position);
		View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
		TextView msg_tv = (TextView)view.findViewById(R.id.labelname);
		msg_tv.setText(frm.getMsg());
		return view;
	}
		
}
