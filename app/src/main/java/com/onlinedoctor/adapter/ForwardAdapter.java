package com.onlinedoctor.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.onlinedoctor.activity.chats.ForwardActivity;
import com.onlinedoctor.activity.PatientActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.view.ForwardDialog;

public class ForwardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private LayoutInflater listContainer;
	private List<BriefMessagePojo> messageList;
	private ImageLoader imageLoader;
	private DisplayImageOptions bImageOptions;
	private PatientMessage forwardMessage;

	public final int ItemType_Text = 0;
	public final int ItemType_Patient = 1;

	public ForwardAdapter(Context mContext, List<BriefMessagePojo> messageList,PatientMessage forwardMessage) {
		super();
		this.mContext = mContext;
		this.messageList = messageList;
		listContainer = LayoutInflater.from(mContext);
		imageLoader = ImageLoader.getInstance();
		this.forwardMessage = forwardMessage;

		bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusSmall)).build();

	}

	public static class ViewHolderText extends RecyclerView.ViewHolder{
		public TextView textView;
		public CardView cardView;

		ViewHolderText(View view) {
			super(view);
			textView = (TextView)view.findViewById(R.id.choose_patient);
			cardView = (CardView)view.findViewById(R.id.forwardcarditem);
		}
	}

	public static class ViewHolderPatient extends RecyclerView.ViewHolder{
		public ImageView head;
		public TextView name;
		public CardView cardView;

		ViewHolderPatient(View view) {
			super(view);
			head = (ImageView)view.findViewById(R.id.face_image);
			name = (TextView)view.findViewById(R.id.user_name);
			cardView = (CardView)view.findViewById(R.id.forwardcarditem);
		}
	}

	@Override
	public int getItemCount() {
		return messageList.size() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return ItemType_Text;
		} else {
			return ItemType_Patient;
		}
	}

	public int getViewTypeCount() {
		return 2;
	}

	public Object getItem(int position) {
		if (position == 0) {
			return ViewHolderText.class;
		} else {
			return messageList.get(position - 1);
		}
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if(viewType == ItemType_Text){
			View v = LayoutInflater.from(mContext).inflate(R.layout.simple_list_item, parent, false);
			return new ViewHolderText(v);
		}else{
			View v = LayoutInflater.from(mContext).inflate(R.layout.forward_list_item, parent, false);
			return new ViewHolderPatient(v);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int type = getItemViewType(position);
		final int final_position = position;
		switch (type) {
			case ItemType_Text:
				ViewHolderText viewHolderText = (ViewHolderText)holder;
				viewHolderText.textView.setText("选择患者");

				viewHolderText.cardView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(mContext, PatientActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("mode", "groupsend");
						bundle.putString("ActivityType", ForwardActivity.CLASS_NAME_STRING);
						intent.putExtras(bundle);
						((ForwardActivity)mContext).startActivityForResult(intent, ForwardActivity.REQUESTCODE_SELECTOR);
					}
				});
				break;
			case ItemType_Patient:
				final BriefMessagePojo entity = messageList.get(position-1);
				ViewHolderPatient viewHolderPatient = (ViewHolderPatient)holder;
				imageLoader.displayImage(entity.getFaceImageUrl(), viewHolderPatient.head,
						bImageOptions);
				viewHolderPatient.name.setText(entity.getUserName());

				viewHolderPatient.cardView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String toID = entity.getUserId();
						forwardMessage.setGuid(UUID.randomUUID().toString());
						forwardMessage.setToID(toID);
						List<PatientMessage> messages = new ArrayList<PatientMessage>();
						PatientMessage message = new PatientMessage(forwardMessage);
						messages.add(message);
						List<Patient> patientList = new ArrayList<Patient>();
						Patient model = new Patient();
						model.setThumbnail(entity.getFaceImageUrl());
						model.setName(entity.getUserName());
						patientList.add(model);
						ForwardDialog dialog = new ForwardDialog(mContext, R.style.Theme_dialog, messages, patientList);
						dialog.show();
					}
				});
				break;
		}
	}
}
