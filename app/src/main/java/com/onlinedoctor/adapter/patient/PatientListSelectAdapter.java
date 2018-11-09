package com.onlinedoctor.adapter.patient;

import java.util.HashMap;
import java.util.List;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.patient.Patient;
public class PatientListSelectAdapter extends BaseAdapter {

	private final Context mContext;
	//private List<PatientListModel> list;
	private List<Patient> list;
	private int patientListLen;
	private ImageLoader imageLoader;
	private HashMap<Integer, Boolean> isSelectedMap;
	
	private DisplayImageOptions bImageOptions;

	public static class ViewHolder {
		public ImageView head;
		public TextView letter;
		public TextView title;
		public CheckBox isSelect;
	}

	//public PatientListSelectAdapter(Context context, List<PatientListModel> list, HashMap<Integer, Boolean> isSelectedMap) {
	public PatientListSelectAdapter(Context context, List<Patient> list, HashMap<Integer, Boolean> isSelectedMap) {
		this.mContext = context;
		this.list = list;
		//final Resources res = context.getResources();
		this.patientListLen = list.size();
		this.isSelectedMap = isSelectedMap;
		imageLoader = ImageLoader.getInstance();
		bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
		.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(80)).build();
	}

	//public void updateListView(List<PatientListModel> list, boolean isShowLabel) {
	public void updateListView(List<Patient> list, boolean isShowLabel) {
		this.list = list;
		this.patientListLen = list.size();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return patientListLen;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	public int getPositionForSection(int section) {
		int totalLen = getCount();
		for (int i = 0; i < totalLen; i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.patient_list_select, null);
			vh = new ViewHolder();
			vh.title = (TextView) convertView.findViewById(R.id.search_contact_name);
			vh.head = (ImageView) convertView.findViewById(R.id.search_contact_headIcon);
			vh.letter = (TextView) convertView.findViewById(R.id.search_contact_catalog);
			vh.isSelect = (CheckBox) convertView.findViewById(R.id.patient_check_box);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		//final PatientListModel mContent = list.get(position);
		final Patient mContent = list.get(position);
		int section = getSectionForPosition(position);
		if (position == getPositionForSection(section)) {
			vh.letter.setVisibility(View.VISIBLE);
			vh.letter.setText(mContent.getSortLetters());
		} else {
			vh.letter.setVisibility(View.GONE);
		}
		//imageLoader.displayImage(this.list.get(position).getImgUrl(), vh.head, bImageOptions);
		imageLoader.displayImage(this.list.get(position).getThumbnail(), vh.head, bImageOptions);
		vh.title.setText(this.list.get(position).getName());
		vh.isSelect.setChecked(this.isSelectedMap.get(position));
		return convertView;
	}

}
