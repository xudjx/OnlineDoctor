package com.onlinedoctor.adapter;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.chats.ChatActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.pojo.message_content.LinkPatientMsgDO;
import com.onlinedoctor.service.NotifyManager;
import com.onlinedoctor.util.DateTransfer;
import com.onlinedoctor.face.FaceConversionUtil;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.view.BadgeView;

/**
 * 主界面“信息” 中展示用户消息情况
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    public static final String TAG = "MessageListAdapter";
    private static Context mContext;
    private int rowLayout;
    private List<BriefMessagePojo> messageList;
    private RunDataContainer dataContainer;
    private DisplayImageOptions options;

    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, Object>>() {}.getType();

    private static int macWidth = 0;

    public MessageListAdapter(Context mContext, int rowLayout, List<BriefMessagePojo> messageList) {
        super();
        this.mContext = mContext;
        this.rowLayout = rowLayout;
        this.messageList = messageList;
        dataContainer = RunDataContainer.getContainer();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusSmall)).build();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        macWidth = width;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public TextView usernameView;
        public ImageView faceImageView;
        public ImageView msgStatusView;
        public TextView caogaoView;
        public TextView messageView;
        public TextView timeView;
        public ImageView disturbNoticeView;
        public RelativeLayout faceLayout;
        public BadgeView messageBadge;
        public TextView tagView;
        public LinearLayout colorLineLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.messagecarditem);
            faceImageView = (ImageView) itemView.findViewById(R.id.face_image);
            usernameView = (TextView) itemView.findViewById(R.id.user_name);
            tagView = (TextView) itemView.findViewById(R.id.user_tag);
            msgStatusView = (ImageView) itemView.findViewById(R.id.message_status);
            caogaoView = (TextView) itemView.findViewById(R.id.caogao);
            messageView = (TextView) itemView.findViewById(R.id.message);
            timeView = (TextView) itemView.findViewById(R.id.time);
            disturbNoticeView = (ImageView) itemView.findViewById(R.id.notice);
            faceLayout = (RelativeLayout)itemView.findViewById(R.id.left_layout);
            messageBadge = new BadgeView(mContext, faceImageView);
            colorLineLayout = (LinearLayout) itemView.findViewById(R.id.bottom_layout);

            // 状态设置
            messageView.setMaxWidth((int) (macWidth * 0.6));
        }

    }

    // use to refresh the list
    public void clearMessages() {
        int size = this.messageList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                messageList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    // use to refresh the list
    public void addMessages(List<BriefMessagePojo> pojo) {
        this.messageList.addAll(pojo);
        this.notifyItemRangeInserted(0, messageList.size() - 1);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final BriefMessagePojo entity = messageList.get(position);

        holder.msgStatusView.setVisibility(View.GONE);
        holder.caogaoView.setVisibility(View.GONE);

        String faceUrl = entity.getFaceImageUrl();
        if (faceUrl != null && !"".equals(faceUrl)) {
            ImageLoader.getInstance().displayImage(faceUrl, holder.faceImageView, options);
        }else{
            holder.faceImageView.setImageResource(R.drawable.user_default);
        }
        // set View by MessageList data
        int unreadCount = entity.getCount();
        if (unreadCount > 0 && unreadCount < 100) {
            holder.messageBadge.setText(String.valueOf(unreadCount));
            holder.messageBadge.setTextSize(12);
            holder.messageBadge.show();
        } else if(unreadCount >=100){
            holder.messageBadge.setText("99+");
            holder.messageBadge.setTextSize(12);
            holder.messageBadge.show();
        } else {
            holder.messageBadge.hide();
        }
        holder.usernameView.setText(entity.getUserName());

        // 显示标签
        if(entity.getLabels().equals("")){
            holder.tagView.setText("暂无标签");
            holder.colorLineLayout.removeAllViews();
        }else{
            String labels = entity.getLabels();
            List<Integer> colors = dataContainer.getLabelColors(labels);
            labels = dataContainer.getLabelNameString(labels);
            labels = labels.replaceAll("&","  |  ");

            holder.tagView.setText(labels);
            holder.colorLineLayout.removeAllViews();
            if(colors != null && colors.size() != 0) {
                for(Iterator<Integer> iter = colors.iterator(); iter.hasNext();){
                    int color = iter.next();
                    Log.d("COLOR_Debug", "color:" + String.valueOf(color));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    TextView tv = new TextView(mContext);
                    tv.setLayoutParams(lp);
                    tv.setBackgroundColor(color);
                    holder.colorLineLayout.addView(tv);
                }
            }

        }

        // 处理消息的不同状态显示
        int msgStatus = entity.getSendStatus();
        switch (msgStatus) {
            case 0:
                // 正在发送
                holder.msgStatusView.setImageResource(R.drawable.msg_sending);
                holder.msgStatusView.setVisibility(View.VISIBLE);
                holder.caogaoView.setVisibility(View.GONE);
                break;
            case 1:
                // 发送成功
                holder.msgStatusView.setVisibility(View.GONE);
                holder.caogaoView.setVisibility(View.GONE);
                break;
            case -1:
                // 发送失败
                holder.msgStatusView.setImageResource(R.drawable.msg_failed);
                holder.msgStatusView.setVisibility(View.VISIBLE);
                holder.caogaoView.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        // 草稿数据的处理
        final int draft = entity.getIsDraft();
        if (draft == 1) {
            holder.msgStatusView.setImageResource(R.drawable.msg_failed);
            holder.msgStatusView.setVisibility(View.VISIBLE);
            holder.caogaoView.setVisibility(View.VISIBLE);
        }

        String contentType = entity.getContentType();
        if (contentType.equals(Common.MESSAGE_TYPE_text)) {
            SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(mContext,
                    entity.getMessage(), "small");
            holder.messageView.setText(spannableString);
        } else if (contentType.equals(Common.MESSAGE_TYPE_image)) {
            holder.messageView.setText("[图片]");
        } else if (contentType.equals(Common.MESSAGE_TYPE_voice)) {
            holder.messageView.setText("[语音]");
        } else if (contentType.equals(Common.MESSAGE_TYPE_link)) {
            LinkPatientMsgDO messageContent = (LinkPatientMsgDO) JsonUtil.jsonToBean(entity.getMessage(), LinkPatientMsgDO.class);
            String str = "[链接]" + messageContent.getTitle();
            holder.messageView.setText(str);
        }else  if(contentType.equals(Common.MESSAGE_TYPE_survey)){
            holder.messageView.setText("[病情调查]");
        }else if(contentType.equals(Common.MESSAGE_TYPE_paymentSuccess)){
            String str = entity.getUserName() + "支付了你的收费项目";
            holder.messageView.setText(str);
        }else if(contentType.equals(Common.MESSAGE_TYPE_paymentRequest)){
            Map<String, Object> map = gson.fromJson(entity.getMessage(), type);
            String title = (String)map.get("title");
            holder.messageView.setText("[收费项目]"+title);
        }else if(contentType.equals(Common.MESSAGE_TYPE_prescription)){
            holder.messageView.setText("处方项目");
        }

        holder.timeView.setText(DateTransfer.transferLongToDate(DateTransfer.dateFormat2, entity.getTime()));
        if (entity.isIfNoDisturbing())
            holder.disturbNoticeView.setVisibility(View.GONE);
        else
            holder.disturbNoticeView.setVisibility(View.GONE);

        // ListItem
        final int index = position;
        final Patient patient = new Patient(entity);
        holder.cardView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                BriefMessagePojo pojo = new BriefMessagePojo(messageList.get(index));
                // 点击之后更新未读消息数 count
                pojo.setCount(0);
                dataContainer.updateBriefMessage(pojo);

                Message message = new Message();
                message.what = Common.MSG_WHAT_BRIEFCHANGE_MESSAGE;
                HandlerManager.getManager().getMainHandler().sendMessage(message);

                // 通知栏清楚对应的用户未读提示
                NotifyManager.getManager().cancelById(pojo.getUserId());

                Intent intent = new Intent(mContext, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("patient", patient);
                bundle.putInt("draft", draft);
                bundle.putString("draft_message", messageList.get(index).getMessage());
                intent.putExtras(bundle);

                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

}
