package com.onlinedoctor.adapter.mine;

import java.util.List;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.mine.SelfDefinedFee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SelfDefinedFeesAdapter extends ArrayAdapter<SelfDefinedFee> {
	public SelfDefinedFeesAdapter(Context context, int resource, List<SelfDefinedFee> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SelfDefinedFee sdf = getItem(position);
		View view = LayoutInflater.from(getContext()).inflate(R.layout.item_labels_2, null);
		TextView title_tv = (TextView)view.findViewById(R.id.labelname);
		TextView abstract_tv = (TextView)view.findViewById(R.id.labelcount);
		title_tv.setText(sdf.getTitle());
		abstract_tv.setText(sdf.getFee() + " 元");
		return view;
	}
}
