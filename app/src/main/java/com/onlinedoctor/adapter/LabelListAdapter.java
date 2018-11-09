package com.onlinedoctor.adapter;

import java.util.HashMap;
import java.util.List;

import com.onlinedoctor.activity.PatientActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.patient.EditLabelActivity;
import com.onlinedoctor.activity.patient.LabelActivity;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;


public class LabelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

	private final Context mContext;
	private List<Label> list;
	private HashMap<Long, Integer> labelCountMap;
	private String mode;
	private String MODE_ADD = "add";
	private String MODE_VIEW = "view";
	private LabelActivity mParent = null;
	private RunDataContainer rdc = null;
	
	public LabelListAdapter(Context context, List<Label> list, HashMap<Long, Integer> labelCountMap, String mode, LabelActivity mParent) {
		this.mContext = context;
		this.list = list;
		this.labelCountMap = labelCountMap;
		this.mode = mode;
		this.mParent = mParent;
		rdc = RunDataContainer.getContainer();
	}


	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView labelName, labelCount, labelIconColor;
		CardView cardView;
		CheckBox checkBox;
		public int position;

		ViewHolder(View view) {
			super(view);
			labelName = (TextView) view.findViewById(R.id.labelname);
			labelCount = (TextView) view.findViewById(R.id.labelcount);
			cardView = (CardView) view.findViewById(R.id.carditem);
			checkBox = (CheckBox) view.findViewById(R.id.labelcheck);
			labelIconColor = (TextView) view.findViewById(R.id.labelIconColor);
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.item_labels, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewHolder vh = (ViewHolder) holder;
		vh.position = position;
		final int final_position = position;
		vh.labelName.setText(list.get(position).getLabelName());
		vh.labelIconColor.setBackgroundColor(list.get(position).getLabelColor());
		if(mode.equals(MODE_ADD)){
			vh.checkBox.setVisibility(View.VISIBLE);
			vh.checkBox.setChecked(mParent.isSelectedMap.get(position));
			vh.labelCount.setVisibility(View.GONE);
			final ViewHolder final_vh = vh;
			vh.cardView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final_vh.checkBox.toggle();
					mParent.isSelectedMap.put(final_position, final_vh.checkBox.isChecked());
				}
			});
		}
		else{
			Long id = list.get(position).getId();
			String countString = "";
			if(labelCountMap.containsKey(id)){
				countString += Integer.toString(labelCountMap.get(id));
			}
			else{
				countString += "0";
			}
			vh.labelCount.setText(countString + "人");
			vh.cardView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
						Label l = list.get(final_position);
						Intent intent = new Intent(mContext, PatientActivity.class);
						intent.putExtra("id", String.valueOf(l.getId()));
						intent.putExtra("mode", "label");
						mContext.startActivity(intent);
				}
			});

			vh.cardView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					final String[] items = {"编辑", "删除", "换色"};
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case 0:
									editLabel(list.get(final_position));
									break;
								case 1:
									deleteLabel(list.get(final_position));
									break;
								case 2:
									Label l = list.get(final_position);
									int color = rdc.genLabelColor();
									if(rdc.updateLabelColorById(l.getId(), color) == 0){
										l.setLabelColor(color);
									}
									LabelActivity la = (LabelActivity) mContext;
									if(list.size() == 0) {
										la.setNoContent(true);
									}
									else{
										la.setNoContent(false);
									}
									notifyDataSetChanged();
									break;
							}
						}
					}).show();
					return false;
				}
			});
		}
	}

	public Label getItem(int position){
		return list.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public void updateListView(List<Label> list, HashMap<Long, Integer> labelCountMap){
		this.list = list;
		this.labelCountMap = labelCountMap;
		notifyDataSetChanged();
	}


	private void editLabel(Label l){
		Intent intent = new Intent(mContext, EditLabelActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("id", String.valueOf(l.getId()));
		bundle.putString("name", l.getLabelName());
		intent.putExtras(bundle);
		mContext.startActivity(intent);
	}

	private void deleteLabel(Label l){
		/*LabelServiceImpl lsImpl = new LabelServiceImpl(mContext, null);
		if(!lsImpl.deleteLabelById(l.getId())){
			Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
		}*/
		if(rdc.delLabel(l.getId()) != 0){
			Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
		}
		PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(mContext, null);
		plr.deleteRelationInfoByLabelId(l.getId());
		mParent.loadData();
		updateListView(mParent.labelList, mParent.labelCountMap);
	}

}
