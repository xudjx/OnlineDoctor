package com.onlinedoctor.adapter.patient;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CheckBox;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.PatientActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.patient.LabelActivity;
import com.onlinedoctor.activity.patient.NewPatientActivity;
import com.onlinedoctor.activity.patient.PatientInfoActivity;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.view.BadgeView;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class PatientListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final Context mContext;
    //private List<PatientListModel> list;
    private List<Patient> list;
    private HashMap<String, List<String>> patientLabel;
    private final String[] funcs;
    private final TypedArray images;
    private int selfListLen, patientListLen;
    private final int TYPE_ONE = 0, TYPE_TWO = 1;
    //AsyncBitmapLoader abl;
    private ImageLoader imageLoader;
    private DisplayImageOptions bImageOptions;
    private String mode;
    final String MODE_GROUPSEND = "groupsend";
    private PatientActivity patientActivity;
    private RunDataContainer rdc = RunDataContainer.getContainer();

   // public PatientListAdapter(Context context, int funcArrayRes, int imageArrayRes, List<PatientListModel> list) {
    public PatientListAdapter(Context context, int funcArrayRes, int imageArrayRes, List<Patient> list, HashMap<String, List<String>> patientLabel, String mode, PatientActivity patientActivity) {
        this.mContext = context;
        this.list = list;
        this.patientLabel = patientLabel;
        this.mode = mode;
        this.patientActivity = patientActivity;
        final Resources res = context.getResources();
        if (funcArrayRes != 0) {
            this.funcs = res.getStringArray(funcArrayRes);
            //this.selfListLen = funcs.length;
            this.selfListLen = 1;
        } else {
            this.funcs = null;
            this.selfListLen = 0;
        }
        if (imageArrayRes != 0) {
            this.images = res.obtainTypedArray(imageArrayRes);
        } else {
            this.images = null;
        }
        this.patientListLen = list.size();
        //this.abl = new AsyncBitmapLoader();
        imageLoader = ImageLoader.getInstance();
		/*bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(40)).build();
		*/
        /*bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(80)).build();*/
        bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusSmall)).build();

    }

    public class ViewHolder1 extends RecyclerView.ViewHolder{
        public ImageView nCardHead, lCardHead;
        public TextView nCardTitle, lCardTitle;
        public CardView newPatientCardView, labelPatientCardView;
        public RelativeLayout newPatientLayout;
        public BadgeView newPatientBadge;

        ViewHolder1(View view){
            super(view);
            nCardHead = (ImageView) view.findViewById(R.id.new_patient_card_icon);
            lCardHead = (ImageView) view.findViewById(R.id.label_patient_card_icon);
            nCardTitle = (TextView) view.findViewById(R.id.new_patient_card_title);
            lCardTitle = (TextView) view.findViewById(R.id.label_patient_card_title);
            newPatientCardView = (CardView) view.findViewById(R.id.new_patient_card);
            labelPatientCardView = (CardView) view.findViewById(R.id.label_patient_card);
            newPatientLayout = (RelativeLayout) view.findViewById(R.id.new_patient_layout);
            newPatientBadge = new BadgeView(mContext, newPatientLayout, BadgeView.POSITION_RIGHT,5);
            newPatientBadge.setTextColor(Color.WHITE);
            newPatientBadge.setTextSize(12);
        }
    }

    public static class ViewHolder2 extends RecyclerView.ViewHolder{
        public ImageView head;
        public TextView nameValue, beizhuValue, labelValue, letter;
        public CardView cardView;
        public CheckBox checkBox;

        ViewHolder2(View view){
            super(view);
            checkBox = (CheckBox) view.findViewById(R.id.item_patient_check_box);
            head = (ImageView) view.findViewById(R.id.search_contact_headIcon);
            nameValue = (TextView) view.findViewById(R.id.user_name_value);
            beizhuValue = (TextView) view.findViewById(R.id.beizhu_value);
            labelValue = (TextView) view.findViewById(R.id.label_value);
            letter = (TextView) view.findViewById(R.id.search_contact_catalog);
            cardView = (CardView) view.findViewById(R.id.carditem);
        }
    }

    public int getSectionForPosition(int position) {
        return list.get(position - selfListLen).getSortLetters().charAt(0);
    }

    public int getPositionForSection(int section) {
        int totalLen = getItemCount();
        for (int i = selfListLen; i < totalLen; i++) {
            String sortStr = list.get(i - selfListLen).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ONE){
            View v = LayoutInflater.from(mContext).inflate(R.layout.corner_list_item, parent, false);
            return new ViewHolder1(v);
        }else{
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_patient_list, parent, false);
            return new ViewHolder2(v);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        final int final_position = position;
        switch (type) {
            case TYPE_ONE:
                ViewHolder1 vh1 = (ViewHolder1) holder;
                vh1.nCardHead.setImageDrawable(images.getDrawable(0));
                vh1.lCardHead.setImageDrawable(images.getDrawable(1));
                vh1.nCardTitle.setText(funcs[0]);
                vh1.lCardTitle.setText(funcs[1]);
                int count = rdc.getNewPatientNum();
                if(count == 0){
                    vh1.newPatientBadge.hide();
                }
                else{
                    vh1.newPatientBadge.setText(String.valueOf(count));
                    vh1.newPatientBadge.show();
                }
                /*
                vh1.head.setImageDrawable(images.getDrawable(position));
                vh1.title.setText(funcs[position]);
                */
                final ViewHolder1 final_vh1 = vh1;
                vh1.newPatientCardView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rdc.newPatientAllChecked();
                        Message msg = new Message();
                        msg.obj = null;
                        msg.what = Common.MSG_WHAT_PATIENTCOUNTCHANGE_MESSAGE;
                        HandlerManager handlerManager = HandlerManager.getManager();
                        if (handlerManager.getMainHandler() != null) {
                            handlerManager.getMainHandler().sendMessage(msg);
                        }
                        final_vh1.newPatientBadge.hide();
                        Intent intent = new Intent(mContext, NewPatientActivity.class);
                        patientActivity.resumeChanged = true;
                        mContext.startActivity(intent);
                    }
                });
                vh1.labelPatientCardView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, LabelActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("mode", "view");
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                        patientActivity.resumeChanged = true;
                    }
                });
                break;
            case TYPE_TWO:
                ViewHolder2 vh2 = (ViewHolder2) holder;
                if(mode.equals(MODE_GROUPSEND)){
                    vh2.checkBox.setVisibility(View.VISIBLE);
                }
                //final PatientListModel mContent = list.get(position - selfListLen);
                final Patient mContent = list.get(position - selfListLen);
                int section = getSectionForPosition(position);
                if (position == getPositionForSection(section)) {
                    vh2.letter.setVisibility(View.VISIBLE);
                    vh2.letter.setText(mContent.getSortLetters());
                } else {
                    vh2.letter.setVisibility(View.GONE);
                }

                imageLoader.displayImage(mContent.getThumbnail(), vh2.head, bImageOptions);
                vh2.nameValue.setText(mContent.getName());
                String beizhu = mContent.getAllCommentString();
                vh2.beizhuValue.setText(beizhu);
                String showLabels = "";
                if(patientLabel.containsKey(mContent.getPatientId())) {
                    List<String> labelNames = patientLabel.get(mContent.getPatientId());
                    for (Iterator<String> iter = labelNames.iterator(); iter.hasNext(); ) {
                        String one = iter.next();
                        showLabels += " " + one + " |";
                    }
                }
                if(showLabels.isEmpty()){
                    vh2.labelValue.setText("标签： 无");
                }else {
                    showLabels = showLabels.substring(0, showLabels.length() - 1);
                    vh2.labelValue.setText("标签：" + showLabels);
                }
                final String labelString = showLabels;
                final ViewHolder2 final_vh2 = vh2;
                vh2.cardView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        PatientListModel patientListModel = (PatientListModel) getItem(final_position);
                        String userId = patientListModel.getUserId();
                        Intent intent = new Intent(mContext, PatientInfoActivity.class);
                        intent.putExtra("userId", userId);
                        mContext.startActivity(intent);*/
                        if(mode.equals( MODE_GROUPSEND)){
                            final_vh2.checkBox.toggle();
                            PatientActivity pa = (PatientActivity) mContext;
                            pa.isSelectedMap.put(final_position, final_vh2.checkBox.isChecked());
                            if(final_vh2.checkBox.isChecked()){
                                pa.selectedPatients.add(list.get(position));
                                Log.d("CheckBox", "add " + list.get(final_position).getName());
                            }else{
                                pa.selectedPatients.remove(list.get(final_position));
                                Log.d("CheckBox", "delete " + list.get(final_position).getName());
                            }
                        }
                        else {
                            Patient patient = (Patient) getItem(final_position);
                            patientActivity.resumeChanged = true;
                            Intent intent = new Intent(mContext, PatientInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("patient", patient);
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return selfListLen + patientListLen;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < selfListLen)
            return TYPE_ONE;
        else
            return TYPE_TWO;
    }

    public Object getItem(int position) {
        // 
        if (this.getItemViewType(position) == TYPE_ONE) {
            return funcs[position];
        } else {
            return list.get(position - selfListLen);
        }
    }

    public void updateView(List<Patient> list, boolean isShowLabel, HashMap<String, List<String>> patientLabel) {
        this.list = list;
        this.patientLabel = patientLabel;
        this.patientListLen = list.size();
        if (isShowLabel) {
            this.selfListLen = 0;
        } else {
            if (this.funcs != null) {
                //this.selfListLen = this.funcs.length;
                this.selfListLen = 1;
            } else {
                this.selfListLen = 0;
            }
        }
        notifyDataSetChanged();
    }


}
