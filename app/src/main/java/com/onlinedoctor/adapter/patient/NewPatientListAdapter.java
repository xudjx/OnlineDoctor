package com.onlinedoctor.adapter.patient;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.patient.PatientInfoActivity;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.patient.NewPatient;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.service.LabelService;
import com.onlinedoctor.sqlite.service.PatientInfoService;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;

public class NewPatientListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
	
	private final Context mContext;
	private List<NewPatient> list;
	private DisplayImageOptions uilOptions;
	private ImageLoader imageLoader;

	public NewPatientListAdapter(Context context, List<NewPatient> list) {
		this.mContext = context;
		this.list = list;
		
		uilOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusSmall)).build();
		
		imageLoader = ImageLoader.getInstance();
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	private static class ViewHolder extends RecyclerView.ViewHolder{
		public ImageView head;
		public TextView name, time, attention;
		public CardView cardView;

		ViewHolder(View view){
			super(view);
			head = (ImageView) view.findViewById(R.id.newuserview);
			name = (TextView) view.findViewById(R.id.newusername);
			time = (TextView) view.findViewById(R.id.newuserintro);
			attention = (TextView) view.findViewById(R.id.newuseraccepted);
			cardView = (CardView) view.findViewById(R.id.carditem);
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.item_new_patient, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final int final_position = position;
		NewPatient np = list.get(position);
		ViewHolder vh = (ViewHolder) holder;
		imageLoader
				.displayImage(np.getThumbnail(), vh.head, uilOptions);
		vh.name.setText(np.getName());
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		vh.time.setText(format.format(np.getFollowTime()));
		if(np.getStatus() == 1){
			vh.attention.setText("已关注");
			vh.cardView.setClickable(true);
		}
		else if(np.getStatus() == 0){
			vh.attention.setText("已取消");
			vh.cardView.setClickable(false);
		}
		vh.cardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NewPatient np = list.get(final_position);
				String userId = np.getPatientId();
				String labelString = "";
				PatientInfoService ps = new PatientInfoServiceImpl(mContext, null);
				Patient patient = ps.viewPatientInfoByPatientId(userId);
				Intent intent = new Intent(mContext, PatientInfoActivity.class);
				PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(mContext, null);
				List<Long> labelIdList = plr.listLabelByPatientId(userId);
				for(Iterator<Long> iter = labelIdList.iterator(); iter.hasNext();){
					long id = iter.next();
					LabelService ls = new LabelServiceImpl(mContext, null);
					Label label = ls.getLabelById(id);
					labelString += label.getLabelName();
				}
				Bundle bundle = new Bundle();
				bundle.putSerializable("patient", patient);
				bundle.putString("labelString", labelString);
				intent.putExtras(bundle);
				mContext.startActivity(intent);
			}
		});
	}

}
