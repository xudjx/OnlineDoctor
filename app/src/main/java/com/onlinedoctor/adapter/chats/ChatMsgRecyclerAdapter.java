package com.onlinedoctor.adapter.chats;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.mine.homepage.MinePageActivity;
import com.onlinedoctor.activity.mine.fee.SelfDefinedFeeDetail;
import com.onlinedoctor.activity.mine.settings.ServiceProtocol;
import com.onlinedoctor.activity.patient.PatientInfoActivity;
import com.onlinedoctor.activity.tools.prescription.PrescriptionManageActivity;
import com.onlinedoctor.activity.tools.survey.DiseaseSurveyQuestionsActivity;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.pojo.message_content.LinkPatientMsgDO;
import com.onlinedoctor.pojo.tools.prescription.PrescriptionDO;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.QuestionnaireImpl;
import com.onlinedoctor.sqlite.service.DoctorInfoService;
import com.onlinedoctor.util.DateTransfer;
import com.onlinedoctor.face.FaceConversionUtil;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.util.VoiceUtil;
import com.onlinedoctor.view.ChatMessagHandleDialog;
import com.onlinedoctor.view.photoview.ViewPagerActivity;

/**
 * 聊天界面中展示消息内容
 */
public class ChatMsgRecyclerAdapter extends RecyclerView.Adapter<ChatMsgRecyclerAdapter.ViewHolder> {

    private final String TAG = ChatMsgRecyclerAdapter.class.getSimpleName();

    private List<PatientMessage> msgs;
    private Context context;
    private LayoutInflater mInflater;
    private Patient currentPatient = new Patient();
    private String doctorId = null;
    private DoctorInfo doctorInfo = null;
    private SharedpreferenceManager preManager;
    private ImageLoader imageLoader;
    private RunDataContainer dataContainer;

    private DisplayImageOptions bImageOptions;
    private DisplayImageOptions sImageOptions;

    private Bitmap patientFaceImage = null, doctorFaceImage = null;

    private int macWidth;

    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, Object>>() {}.getType();

    public ChatMsgRecyclerAdapter(Context context, Patient patient, List<PatientMessage> msgs) {
        super();
        this.msgs = msgs;
        this.context = context;
        this.currentPatient = patient;
        mInflater = LayoutInflater.from(context);
        preManager = SharedpreferenceManager.getInstance();
        imageLoader = ImageLoader.getInstance();
        dataContainer = RunDataContainer.getContainer();
        doctorId = preManager.getOne("keySid");
        DoctorInfoService doctorInfoService = new DoctorInfoServiceImpl(context, null);
        doctorInfo = doctorInfoService.get(1);
        Log.i("Current Doctor: ", doctorId);

        bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusMid)).build();
        sImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusSmall)).build();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        macWidth = width;
    }

    public int getCount() {
        return msgs.size();
    }

    public Object getItem(int position) {
        return msgs.get(position);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        Logger.d("onViewRecycled", "position = " + holder.getAdapterPosition());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatting_item_msg, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {{
        final PatientMessage entity = msgs.get(position);
        boolean isComMsg = judgeComeMessage(position);

        long surveyGlobalId = 0;

        // View setting and Handle
        if(isComMsg){ // 处理接收的消息
            viewHolder.goneRight(); // 把发送消息的显示UI全部gone
            viewHolder.visibleLeft();
            // 设置初始状态
            viewHolder.leftViewHolder.tvSendTimeLeft.setVisibility(View.GONE);
            viewHolder.leftViewHolder.voiceUnreadViewLeft.setVisibility(View.GONE);
            viewHolder.leftViewHolder.messageBarLeft.setVisibility(View.GONE);
            viewHolder.leftViewHolder.imageBarLeft.setVisibility(View.GONE);
            viewHolder.leftViewHolder.tvVoiceTimeLeft.setVisibility(View.GONE);
            viewHolder.leftViewHolder.chargeResponseLayoutLeft.setVisibility(View.GONE);
            viewHolder.leftViewHolder.linkLayoutLeft.setVisibility(View.GONE);

            String avaterPath = null;
            String voice = "";
            String voicePath = "";

            if(currentPatient.getThumbnail() != null && !(currentPatient.getThumbnail().equals(""))){
                imageLoader.displayImage(currentPatient.getThumbnail(), viewHolder.leftViewHolder.faceImageViewLeft, sImageOptions);
            }else{
                viewHolder.leftViewHolder.faceImageViewLeft.setImageResource(R.drawable.user_default);
            }

            // 显示时间
            if (showTimeByTimeGap(position, position - 1)) {
                viewHolder.leftViewHolder.tvSendTimeLeft.setText(DateTransfer.transferLongToDate(DateTransfer.dateFormat3,
                        entity.getTimestamp()));
                viewHolder.leftViewHolder.tvSendTimeLeft.setVisibility(View.VISIBLE);
            } else {
                viewHolder.leftViewHolder.tvSendTimeLeft.setVisibility(View.GONE);
            }

            // 根据不同的消息类型进行显示
            final String contentType = entity.getContentType();
            if (contentType.equals(Common.MESSAGE_TYPE_text)) {
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.VISIBLE);
                SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context,
                        entity.getContent(), "normal");
                viewHolder.leftViewHolder.tvContentLeft.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                viewHolder.leftViewHolder.tvContentLeft.setText(spannableString);
            } else if (contentType.equals(Common.MESSAGE_TYPE_image)) {
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setBackground(null);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.VISIBLE);
                Map<String, Object> map = gson.fromJson(entity.getContent(), type);
                String thumbnailPath = (String) map.get(Common.KEY_IMAGE_thumbnailPath);
                avaterPath = (String) map.get(Common.KEY_IMAGE_avaterPath);
                imageLoader.displayImage(thumbnailPath, viewHolder.leftViewHolder.tvImageViewLeft, bImageOptions);
            }else if(contentType.equals(Common.MESSAGE_TYPE_voice)){
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvVoiceTimeLeft.setVisibility(View.VISIBLE);

                voice = entity.getContent();
                if (!"".equals(voice)) {
                    voicePath = Common.sdAudioSave + File.separator + voice.substring(voice.lastIndexOf("/") + 1);
                }
                long duration = VoiceUtil.getAmrDuration(voicePath);
                String time = milliToStr(duration);
                viewHolder.leftViewHolder.tvVoiceTimeLeft.setText(time);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) judgeVoiceLength(duration), RelativeLayout.LayoutParams.WRAP_CONTENT);
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setLayoutParams(lp);
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.VISIBLE);
                // 来的消息如果是未读消息，显示未读标识
                if (entity.getIsRead() == 0) {
                    viewHolder.leftViewHolder.voiceUnreadViewLeft.setVisibility(View.VISIBLE);
                }
            }else if (contentType.equals(Common.MESSAGE_TYPE_survey)) {
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.VISIBLE);
                QuestionnaireImpl impl = new QuestionnaireImpl(context, null);
                surveyGlobalId = Long.parseLong(entity.getContent());
                Questionnaire q = impl.getQuestionnairebyGlobalId(surveyGlobalId);
                if (q != null) {
                    SpannableString spannableString = new SpannableString(
                            "为了更好了解你的情况和需要，希望你仔细回答病情调查\n>" + q.getName());
                    int end = spannableString.length();
                    //设置下划线
                    spannableString.setSpan(new UnderlineSpan(), 26, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 26, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.leftViewHolder.tvContentLeft.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    viewHolder.leftViewHolder.tvContentLeft.setText(spannableString);
                } else {
                    viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.GONE);
                }
            } else if (contentType.equals(Common.MESSAGE_TYPE_surveyFeedback)) {
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.VISIBLE);
                String content = entity.getContent();
                surveyGlobalId = Long.parseLong(content.substring(content.indexOf("questionNaireId=") + 16, content.length()));
                QuestionnaireImpl impl = new QuestionnaireImpl(context, null);
                Questionnaire q = impl.getQuestionnairebyGlobalId(surveyGlobalId);
                if (q != null) {
                    SpannableString spannableString = new SpannableString(
                            "点击查看病情调查结果反馈\n>" + q.getName());
                    int end = spannableString.length();
                    //设置下划线
                    spannableString.setSpan(new UnderlineSpan(), 12, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 26, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.leftViewHolder.tvContentLeft.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    viewHolder.leftViewHolder.tvContentLeft.setText(spannableString);
                } else {
                    viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.GONE);
                }
            } else if(contentType.equals(Common.MESSAGE_TYPE_paymentSuccess)){
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.faceImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.chargeResponseLayoutLeft.setVisibility(View.VISIBLE);
                ((TextView)(viewHolder.leftViewHolder.chargeResponseLayoutLeft.findViewById(R.id.charge_response))).setText(currentPatient.getName()+"支付你的收费项目");
            }else if(contentType.equals(Common.MESSAGE_TYPE_link)){
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.GONE);
                String content = entity.getContent();
                LinkPatientMsgDO messageContent = (LinkPatientMsgDO)JsonUtil.jsonToBean(content, LinkPatientMsgDO.class);
                viewHolder.leftViewHolder.linkTitleViewLeft.setText(messageContent.getTitle());
                viewHolder.leftViewHolder.linkDecViewLeft.setText(messageContent.getDescription());
                imageLoader.displayImage(messageContent.getImageUrl(), viewHolder.leftViewHolder.linkImageLeft, sImageOptions);
                viewHolder.leftViewHolder.linkLayoutLeft.setVisibility(View.VISIBLE);
            }else {
                viewHolder.leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
                viewHolder.leftViewHolder.tvContentLeft.setVisibility(View.VISIBLE);
                SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context,
                        entity.getContent(), "normal");
                viewHolder.leftViewHolder.tvContentLeft.setText(spannableString);
            }

            int sendStatus = entity.getSendStatus();
            switch (sendStatus) {
                case 0:// 发送
                {
                    if (contentType.equals(Common.MESSAGE_TYPE_image)) {
                        viewHolder.leftViewHolder.messageBarLeft.setVisibility(View.INVISIBLE);
                        viewHolder.leftViewHolder.imageBarLeft.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.leftViewHolder.messageBarLeft.setVisibility(View.VISIBLE);
                        viewHolder.leftViewHolder.imageBarLeft.setVisibility(View.INVISIBLE);
                    }
                    viewHolder.leftViewHolder.sendStatusViewLeft.setVisibility(View.GONE);
                    break;
                }
                case 1:// 发送成功
                {
                    viewHolder.leftViewHolder.imageBarLeft.setVisibility(View.INVISIBLE);
                    viewHolder.leftViewHolder.messageBarLeft.setVisibility(View.INVISIBLE);
                    viewHolder.leftViewHolder.sendStatusViewLeft.setVisibility(View.GONE);
                    break;
                }
                case -1:// 发送失败
                {
                    viewHolder.leftViewHolder.imageBarLeft.setVisibility(View.GONE);
                    viewHolder.leftViewHolder.messageBarLeft.setVisibility(View.GONE);
                    viewHolder.leftViewHolder.sendStatusViewLeft.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    break;
            }

            final long globalId = surveyGlobalId;
            final String survetResultUrl = entity.getContent();
            viewHolder.leftViewHolder.tvContentLeft.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_survey)) {
                        if (globalId > 0) {
                            Intent intent = new Intent(context, DiseaseSurveyQuestionsActivity.class);
                            intent.putExtra("fromActivity", "ChatActivity");
                            intent.putExtra("globalId", globalId);
                            context.startActivity(intent);
                        }
                    } else if (entity.getContentType().equals(Common.MESSAGE_TYPE_surveyFeedback)) {
                        if (globalId > 0) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(survetResultUrl);
                            intent.setData(content_url);
                            context.startActivity(intent);
                        }
                    }
                }
            });

            viewHolder.leftViewHolder.tvContentLeft.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            viewHolder.leftViewHolder.chargeResponseLayoutLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.d("ChatAdapter","Clicked");
                }
            });

            final String imagePath = avaterPath;
            viewHolder.leftViewHolder.tvImageViewLeft.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_image)) {
                        // 多图浏览、多点触摸
                        ArrayList<String> urlList = new ArrayList<String>();
                        int currentItem = 0;
                        int item = 0;
                        for (Iterator<PatientMessage> iterator = msgs.iterator(); iterator.hasNext(); ) {
                            PatientMessage message = iterator.next();
                            if (message.getContentType().equals(Common.MESSAGE_TYPE_image)) {
                                Map<String, Object> map = gson.fromJson(message.getContent(), type);
                                String thumbnailPath = (String) map.get(Common.KEY_IMAGE_thumbnailPath);
                                String avaterPath = (String) map.get(Common.KEY_IMAGE_avaterPath);
                                if (avaterPath.equals(imagePath)) {
                                    currentItem = item;
                                }
                                urlList.add(avaterPath);
                                item++;
                            }
                        }
                        Logger.i(TAG, "ViewPage images: " + urlList.size());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("urlist", urlList);
                        bundle.putInt("currentItem", currentItem);
                        Intent intent = new Intent(context, ViewPagerActivity.class);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                }
            });

            viewHolder.leftViewHolder.tvImageViewLeft.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            final ProgressBar msgBar = viewHolder.leftViewHolder.messageBarLeft;
            final ImageView sendView = viewHolder.leftViewHolder.sendStatusViewLeft;
            viewHolder.leftViewHolder.sendStatusViewLeft.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("重发该消息?").setCancelable(false)
                            .setPositiveButton("重发", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Message message = new Message();
                                    message.what = Common.MSG_WAHT_SEND_AGAIN;
                                    message.obj = entity;
                                    entity.setSendStatus(0);
                                    HandlerManager.getManager().getChatHandler().sendMessage(message);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            // 处理语音消息点击事件
            final ImageView unreadView = viewHolder.leftViewHolder.voiceUnreadViewLeft;
            viewHolder.leftViewHolder.tvVoiceLayoutLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_voice)) {
                        if(entity.getIsRead() == 0){
                            unreadView.setVisibility(View.GONE);
                            PatientMessage message = new PatientMessage(entity);
                            message.setIsRead(1);
                            dataContainer.updatePatientMessage(message, entity.getFromID());
                            // 更新msgs，否则滑动listview红点依然存在
                            entity.setIsRead(1);
                        }
                        String voice = entity.getContent();
                        String voicePath = Common.sdAudioSave + File.separator
                                + voice.substring(voice.lastIndexOf("/") + 1);
                        if (VoiceUtil.exists(voicePath)) {
                            VoiceUtil.playMusic(voicePath);
                        } else {
                            Toast.makeText(context, "不可播放，文件已被删除", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            // 处理长按事件
            viewHolder.leftViewHolder.tvVoiceLayoutLeft.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            viewHolder.leftViewHolder.faceImageViewLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("patient", currentPatient);
                    bundle.putString("labelString", "");
                    Intent intent = new Intent(context, PatientInfoActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

            // link点击
            viewHolder.leftViewHolder.linkLayoutLeft.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinkPatientMsgDO messageContent = (LinkPatientMsgDO)JsonUtil.jsonToBean(entity.getContent(), LinkPatientMsgDO.class);
                    String linkUrl = messageContent.getLink();
                    Intent intent = new Intent(context, ServiceProtocol.class);
                    intent.putExtra("flag","LinkMessage");
                    if((linkUrl != null) && !linkUrl.equals("")){
                        intent.putExtra("LinkUrl",linkUrl);
                    }
                    context.startActivity(intent);
                }
            });

            viewHolder.leftViewHolder.linkLayoutLeft.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

        }else{ // 处理发送的消息

            viewHolder.goneLeft(); // 把发送消息的显示UI全部gone
            viewHolder.visibleRight();

            //设置初始状态
            viewHolder.rightViewHolder.chargeLayoutRight.setVisibility(View.GONE);
            viewHolder.rightViewHolder.tvSendTimeRight.setVisibility(View.GONE);
            viewHolder.rightViewHolder.voiceUnreadViewRight.setVisibility(View.GONE);
            viewHolder.rightViewHolder.messageBarRight.setVisibility(View.GONE);
            viewHolder.rightViewHolder.imageBarRight.setVisibility(View.GONE);
            viewHolder.rightViewHolder.tvVoiceTimeRight.setVisibility(View.GONE);
            viewHolder.rightViewHolder.linkLayoutRight.setVisibility(View.GONE);

            String avaterPath = null;
            String voice = "";
            String voicePath = "";

            imageLoader.displayImage(doctorInfo.getThumbnail(), viewHolder.rightViewHolder.faceImageViewRight, sImageOptions);

            // 显示时间
            if (showTimeByTimeGap(position, position - 1)) {
                viewHolder.rightViewHolder.tvSendTimeRight.setText(DateTransfer.transferLongToDate(DateTransfer.dateFormat3,
                        entity.getTimestamp()));
                viewHolder.rightViewHolder.tvSendTimeRight.setVisibility(View.VISIBLE);
            } else {
                viewHolder.rightViewHolder.tvSendTimeRight.setVisibility(View.GONE);
            }

            // 根据不同的消息类型进行显示
            final String contentType = entity.getContentType();
            if (contentType.equals(Common.MESSAGE_TYPE_text)) {
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.VISIBLE);
                SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context,
                        entity.getContent(), "normal");
                viewHolder.rightViewHolder.tvContentRight.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                viewHolder.rightViewHolder.tvContentRight.setText(spannableString);
            } else if (contentType.equals(Common.MESSAGE_TYPE_image)) {
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setBackground(null);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.VISIBLE);
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> map = gson.fromJson(entity.getContent(), type);
                String thumbnailPath = (String) map.get(Common.KEY_IMAGE_thumbnailPath);
                avaterPath = (String) map.get(Common.KEY_IMAGE_avaterPath);
                imageLoader.displayImage(thumbnailPath, viewHolder.rightViewHolder.tvImageViewRight, bImageOptions);
            }else if(contentType.equals(Common.MESSAGE_TYPE_voice)){
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvVoiceTimeRight.setVisibility(View.VISIBLE);
                voicePath = entity.getContent();
                long duration = VoiceUtil.getAmrDuration(voicePath);
                String time = milliToStr(duration);
                viewHolder.rightViewHolder.tvVoiceTimeRight.setText(time);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) judgeVoiceLength(duration), RelativeLayout.LayoutParams.WRAP_CONTENT);
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setLayoutParams(lp);
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.VISIBLE);
            }else if (contentType.equals(Common.MESSAGE_TYPE_survey)) {
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                QuestionnaireImpl impl = new QuestionnaireImpl(context, null);
                surveyGlobalId = Long.parseLong(entity.getContent());
                Questionnaire q = impl.getQuestionnairebyGlobalId(surveyGlobalId);
                if (q != null) {
                    /*
                    SpannableString spannableString = new SpannableString(
                            "为了更好了解你的情况和需要，希望你仔细回答病情调查\n>" + q.getName());
                    int end = spannableString.length();
                    //设置下划线
                    spannableString.setSpan(new UnderlineSpan(), 26, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 26, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.rightViewHolder.tvContentRight.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    viewHolder.rightViewHolder.tvContentRight.setText(spannableString);
                    */
                    viewHolder.rightViewHolder.linkTitleViewRight.setText("病情调查: " + q.getName());
                    String questions = "";
                    int count = 0;
                    for(BaseQuestionDTO question: q.toQuesionDTOList()){
                        count ++;
                        if(count <= 2){
                            questions += question.getTitle();
                            questions += "\n";
                        }else{
                            questions += "...";
                            break;
                        }
                    }
                    viewHolder.rightViewHolder.linkDecViewRight.setText(questions);
                    Logger.d("Survey","<survey message>" + q.toString());
                    viewHolder.rightViewHolder.linkImageRight.setImageResource(R.drawable.ico_tool_follow_template);
                    viewHolder.rightViewHolder.linkLayoutRight.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.rightViewHolder.linkLayoutRight.setVisibility(View.GONE);
                }
            } else if (contentType.equals(Common.MESSAGE_TYPE_surveyFeedback)) {
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.VISIBLE);
                String content = entity.getContent();
                surveyGlobalId = Long.parseLong(content.substring(content.indexOf("questionNaireId=") + 16, content.length()));
                QuestionnaireImpl impl = new QuestionnaireImpl(context, null);
                Questionnaire q = impl.getQuestionnairebyGlobalId(surveyGlobalId);
                if (q != null) {
                    SpannableString spannableString = new SpannableString(
                            "点击查看病情调查结果反馈\n>" + q.getName());
                    int end = spannableString.length();
                    //设置下划线
                    spannableString.setSpan(new UnderlineSpan(), 12, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 26, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.rightViewHolder.tvContentRight.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    viewHolder.rightViewHolder.tvContentRight.setText(spannableString);
                } else {
                    viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                }
            }else if(contentType.equals(Common.MESSAGE_TYPE_paymentRequest)){
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.chargeLayoutRight.setVisibility(View.VISIBLE);
                Map<String, Object> map = gson.fromJson(entity.getContent(), type);
                String title = (String)map.get("title");
                String fee = (String)map.get("fee") + "元";
                ((TextView)(viewHolder.rightViewHolder.chargeLayoutRight.findViewById(R.id.charge_title))).setText(title);
                ((TextView)(viewHolder.rightViewHolder.chargeLayoutRight.findViewById(R.id.charge_money))).setText(fee);
            }else if(contentType.equals(Common.MESSAGE_TYPE_link)){
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                String content = entity.getContent();
                Logger.d("Link", content);
                LinkPatientMsgDO messageContent = (LinkPatientMsgDO)JsonUtil.jsonToBean(content, LinkPatientMsgDO.class);
                viewHolder.rightViewHolder.linkTitleViewRight.setText(messageContent.getTitle());
                viewHolder.rightViewHolder.linkDecViewRight.setText(messageContent.getDescription());
                Logger.d("Link", "Loading link image" + messageContent.getImageUrl());
                imageLoader.displayImage(messageContent.getImageUrl(), viewHolder.rightViewHolder.linkImageRight, sImageOptions);
                viewHolder.rightViewHolder.linkLayoutRight.setVisibility(View.VISIBLE);
            } else if(contentType.equals(Common.MESSAGE_TYPE_prescription)){
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.GONE);
                String content = entity.getContent();
                Logger.d("Link", content);
                PrescriptionDO prescriptionDO = (PrescriptionDO)JsonUtil.jsonToBean(content, PrescriptionDO.class);
                viewHolder.rightViewHolder.linkTitleViewRight.setText("处方");
                viewHolder.rightViewHolder.linkDecViewRight.setText(prescriptionDO.getRemark());
                imageLoader.displayImage(prescriptionDO.getFirstImage(), viewHolder.rightViewHolder.linkImageRight, sImageOptions);
                viewHolder.rightViewHolder.linkLayoutRight.setVisibility(View.VISIBLE);
            } else {
                viewHolder.rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
                viewHolder.rightViewHolder.tvContentRight.setVisibility(View.VISIBLE);
                SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context,
                        entity.getContent(), "normal");
                viewHolder.rightViewHolder.tvContentRight.setText(spannableString);
            }

            int sendStatus = entity.getSendStatus();
            switch (sendStatus) {
                case 0:// 发送
                {
                    if (contentType.equals(Common.MESSAGE_TYPE_image)) {
                        viewHolder.rightViewHolder.messageBarRight.setVisibility(View.INVISIBLE);
                        viewHolder.rightViewHolder.imageBarRight.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.rightViewHolder.messageBarRight.setVisibility(View.VISIBLE);
                        viewHolder.rightViewHolder.imageBarRight.setVisibility(View.INVISIBLE);
                    }
                    viewHolder.rightViewHolder.sendStatusViewRight.setVisibility(View.GONE);
                    break;
                }
                case 1:// 发送成功
                {
                    viewHolder.rightViewHolder.imageBarRight.setVisibility(View.INVISIBLE);
                    viewHolder.rightViewHolder.messageBarRight.setVisibility(View.INVISIBLE);
                    viewHolder.rightViewHolder.sendStatusViewRight.setVisibility(View.GONE);
                    break;
                }
                case -1:// 发送失败
                {
                    viewHolder.rightViewHolder.imageBarRight.setVisibility(View.GONE);
                    viewHolder.rightViewHolder.messageBarRight.setVisibility(View.GONE);
                    viewHolder.rightViewHolder.sendStatusViewRight.setVisibility(View.VISIBLE);
                    break;
                }
                default:
                    break;
            }

            final long globalId = surveyGlobalId;
            final String survetResultUrl = entity.getContent();
            viewHolder.rightViewHolder.tvContentRight.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_survey)) {
                        if (globalId > 0) {
                            Intent intent = new Intent(context, DiseaseSurveyQuestionsActivity.class);
                            intent.putExtra("fromActivity", "ChatActivity");
                            intent.putExtra("globalId", globalId);
                            context.startActivity(intent);
                        }
                    } else if (entity.getContentType().equals(Common.MESSAGE_TYPE_surveyFeedback)) {
                        if (globalId > 0) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(survetResultUrl);
                            intent.setData(content_url);
                            context.startActivity(intent);
                        }
                    }
                }
            });

            viewHolder.rightViewHolder.tvContentRight.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            final String imagePath = avaterPath;
            viewHolder.rightViewHolder.tvImageViewRight.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_image)) {
                        // 多图浏览、多点触摸
                        ArrayList<String> urlList = new ArrayList<String>();
                        int currentItem = 0;
                        int item = 0;
                        for (Iterator<PatientMessage> iterator = msgs.iterator(); iterator.hasNext(); ) {
                            PatientMessage message = iterator.next();
                            if (message.getContentType().equals(Common.MESSAGE_TYPE_image)) {
                                Map<String, Object> map = gson.fromJson(message.getContent(), type);
                                String thumbnailPath = (String) map.get(Common.KEY_IMAGE_thumbnailPath);
                                String avaterPath = (String) map.get(Common.KEY_IMAGE_avaterPath);
                                if (avaterPath.equals(imagePath)) {
                                    currentItem = item;
                                }
                                urlList.add(avaterPath);
                                item++;
                            }
                        }
                        Logger.i(TAG, "ViewPage images: " + urlList.size());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("urlist", urlList);
                        bundle.putInt("currentItem", currentItem);
                        Intent intent = new Intent(context, ViewPagerActivity.class);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                }
            });

            viewHolder.rightViewHolder.tvImageViewRight.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            final ProgressBar msgBar = viewHolder.rightViewHolder.messageBarRight;
            final ImageView sendView = viewHolder.rightViewHolder.sendStatusViewRight;
            viewHolder.rightViewHolder.sendStatusViewRight.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("重发该消息?").setCancelable(false)
                            .setPositiveButton("重发", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Message message = new Message();
                                    message.what = Common.MSG_WAHT_SEND_AGAIN;
                                    message.obj = entity;
                                    entity.setSendStatus(0);
                                    HandlerManager.getManager().getChatHandler().sendMessage(message);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            // 处理语音消息点击事件
            final ImageView unreadView = viewHolder.rightViewHolder.voiceUnreadViewRight;
            viewHolder.rightViewHolder.tvVoiceLayoutRight.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_voice)) {
                        unreadView.setVisibility(View.GONE);
                        PatientMessage message = new PatientMessage(entity);
                        message.setIsRead(1);
                        dataContainer.updatePatientMessage(message, entity.getContentType());
                        String voice = entity.getContent();
                        String voicePath = Common.sdAudioSave + File.separator
                                + voice.substring(voice.lastIndexOf("/") + 1);
                        if (VoiceUtil.exists(voicePath)) {
                            VoiceUtil.playMusic(voicePath);
                        } else {
                            Toast.makeText(context, "不可播放，文件已被删除", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            // 处理长按事件
            viewHolder.rightViewHolder.tvVoiceLayoutRight.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            // 处理头像点击事件
            viewHolder.rightViewHolder.faceImageViewRight.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MinePageActivity.class);
                    context.startActivity(intent);
                }
            });

            // 自定义收费跳转
            viewHolder.rightViewHolder.chargeLayoutRight.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, Object> map = gson.fromJson(entity.getContent(), type);
                    Intent intent = new Intent(context, SelfDefinedFeeDetail.class);
                    intent.putExtra("title",(String)map.get("title"));
                    intent.putExtra("fee",(String)map.get("fee"));
                    intent.putExtra("description",(String)map.get("description"));
                    intent.putExtra("flag",3);
                    context.startActivity(intent);
                }
            });

            // link点击
            viewHolder.rightViewHolder.linkLayoutRight.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (entity.getContentType().equals(Common.MESSAGE_TYPE_survey)) {
                        if (globalId > 0) {
                            Intent intent = new Intent(context, DiseaseSurveyQuestionsActivity.class);
                            intent.putExtra("fromActivity", "ChatActivity");
                            intent.putExtra("globalId", globalId);
                            context.startActivity(intent);
                        }
                    }else if(entity.getContentType().equals(Common.MESSAGE_TYPE_link)){
                        LinkPatientMsgDO messageContent = (LinkPatientMsgDO)JsonUtil.jsonToBean(entity.getContent(), LinkPatientMsgDO.class);
                        String linkUrl = messageContent.getLink();
                        Intent intent = new Intent(context, ServiceProtocol.class);
                        intent.putExtra("flag","LinkMessage");
                        if((linkUrl!= null) && !linkUrl.equals("")){
                            intent.putExtra("LinkUrl",linkUrl);
                        }
                        context.startActivity(intent);
                    }else if(entity.getContentType().equals(Common.MESSAGE_TYPE_prescription)){
                        Intent intent = new Intent(context, PrescriptionManageActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

            viewHolder.rightViewHolder.linkLayoutRight.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

            viewHolder.rightViewHolder.chargeLayoutRight.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ChatMessagHandleDialog dialog = new ChatMessagHandleDialog(context, R.style.Theme_dialog, entity);
                    dialog.show();
                    return true;
                }
            });

        }
        return;
    }

    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return msgs == null?0:msgs.size();
    }

    private boolean judgeComeMessage(int position) {
        PatientMessage entity = msgs.get(position);
        if (entity.getFromID().equals(doctorId)) {
            return false;
        } else {
            return true;
        }
    }

    private String milliToStr(long duration) {
        int time = (int) duration;
        StringBuilder builder = new StringBuilder();
        int minute = time / 60000;
        int sec = (time % 60000) / 1000;
        int milli = time % 1000;
        if (minute > 0) {
            builder.append(minute);
            builder.append("'");
        } else if (sec > 0) {
            builder.append(sec);
            builder.append("''");
        } else{
            builder.append("1''");
        }
        //builder.append(milli); // 不显示毫秒
        return builder.toString();
    }

    // 计算Voice消息的显示长度
    @SuppressWarnings("unused")
    private double judgeVoiceLength(long duration) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        Log.d("Msg Voice", String.valueOf(width));
        double milli = duration / 1000.0;
        Log.d("Msg Voice", "duration=" + milli);
        if (milli < 1.99) {
            return width / 8;
        } else if (milli < 5) {
            return width / 6;
        } else if (milli < 10) {
            return width / 4;
        } else if (milli < 20) {
            return width / 2.5;
        } else {
            return width / 2;
        }
    }

    // 比较两个ListItem时间间隔,判断是否显示时间
    private boolean showTimeByTimeGap(int currentPos, int prePos) {
        if (currentPos == 0) {
            return true;
        }
        long currentTime = msgs.get(currentPos).getTimestamp();
        long preTime = msgs.get(prePos).getTimestamp();
        if ((currentTime - preTime) < 60000*5) {
            return false;
        }
        return true;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public boolean isComMsg = true;
        public RelativeLayout chatLayout;
        public LeftViewHolder leftViewHolder;
        public RightViewHolder rightViewHolder;

        public ViewHolder(View convertView){
            super(convertView);
            leftViewHolder = new LeftViewHolder();
            rightViewHolder = new RightViewHolder();
            chatLayout = (RelativeLayout)convertView.findViewById(R.id.chat_layout);
            // Init LeftView
            {
                leftViewHolder.leftLayout = (RelativeLayout) convertView.findViewById(R.id.chat_left_layout);
                leftViewHolder.faceImageViewLeft = (ImageView) convertView.findViewById(R.id.tv_userhead_left);
                leftViewHolder.tvSendTimeLeft = (TextView) convertView.findViewById(R.id.tv_sendtime_left);
                leftViewHolder.tvContentLeft = (TextView) convertView.findViewById(R.id.tv_chatcontent_left);
                leftViewHolder.tvImageViewLeft = (ImageView) convertView.findViewById(R.id.tv_image_left);
                leftViewHolder.tvVoiceViewLeft = (ImageView) convertView.findViewById(R.id.tv_voiceView_left);
                leftViewHolder.tvVoiceLayoutLeft = (RelativeLayout) convertView.findViewById(R.id.voice_layout_left);
                leftViewHolder.tvVoiceTimeLeft = (TextView) convertView.findViewById(R.id.tv_time_left);
                leftViewHolder.voiceUnreadViewLeft = (ImageView) convertView.findViewById(R.id.voice_unread_left);
                leftViewHolder.imageBarLeft = (ProgressBar) convertView.findViewById(R.id.image_bar_left);
                leftViewHolder.sendStatusViewLeft = (ImageView) convertView.findViewById(R.id.send_status_view_left);
                leftViewHolder.messageBarLeft = (ProgressBar) convertView.findViewById(R.id.message_bar_left);
                leftViewHolder.chargeResponseLayoutLeft = (RelativeLayout) convertView.findViewById(R.id.charge_response_layout);
                leftViewHolder.linkLayoutLeft = (RelativeLayout)convertView.findViewById(R.id.link_layout_left);
                leftViewHolder.linkTitleViewLeft = (TextView)convertView.findViewById(R.id.link_title_left);
                leftViewHolder.linkDecViewLeft = (TextView)convertView.findViewById(R.id.link_description_left);
                leftViewHolder.linkImageLeft = (ImageView)convertView.findViewById(R.id.link_image_left);
            }

            // Init RightView
            {
                rightViewHolder.rightLayout = (RelativeLayout) convertView.findViewById(R.id.chat_right_layout);
                rightViewHolder.faceImageViewRight = (ImageView) convertView.findViewById(R.id.tv_userhead_right);
                rightViewHolder.tvSendTimeRight = (TextView) convertView.findViewById(R.id.tv_sendtime_right);
                rightViewHolder.tvContentRight = (TextView) convertView.findViewById(R.id.tv_chatcontent_right);
                rightViewHolder.tvImageViewRight = (ImageView) convertView.findViewById(R.id.tv_image_right);
                rightViewHolder.tvVoiceViewRight = (ImageView) convertView.findViewById(R.id.tv_voiceView_right);
                rightViewHolder.tvVoiceLayoutRight = (RelativeLayout) convertView.findViewById(R.id.voice_layout_right);
                rightViewHolder.tvVoiceTimeRight = (TextView) convertView.findViewById(R.id.tv_time_right);
                rightViewHolder.voiceUnreadViewRight = (ImageView) convertView.findViewById(R.id.voice_unread_right);
                rightViewHolder.imageBarRight = (ProgressBar) convertView.findViewById(R.id.image_bar_right);
                rightViewHolder.sendStatusViewRight = (ImageView) convertView.findViewById(R.id.send_status_view_right);
                rightViewHolder.messageBarRight = (ProgressBar) convertView.findViewById(R.id.message_bar_right);
                rightViewHolder.chargeLayoutRight = (RelativeLayout) convertView.findViewById(R.id.charge_layout);
                rightViewHolder.linkLayoutRight = (RelativeLayout)convertView.findViewById(R.id.link_layout_right);
                rightViewHolder.linkTitleViewRight = (TextView)convertView.findViewById(R.id.link_title_right);
                rightViewHolder.linkDecViewRight = (TextView)convertView.findViewById(R.id.link_description_right);
                rightViewHolder.linkImageRight = (ImageView)convertView.findViewById(R.id.link_image_right);
            }
        }

        public void goneLeft(){
            leftViewHolder.leftLayout.setVisibility(View.GONE);
            leftViewHolder.faceImageViewLeft.setVisibility(View.GONE);
            leftViewHolder.imageBarLeft.setVisibility(View.GONE);
            leftViewHolder.messageBarLeft.setVisibility(View.GONE);
            leftViewHolder.sendStatusViewLeft.setVisibility(View.GONE);
            leftViewHolder.tvContentLeft.setVisibility(View.GONE);
            leftViewHolder.tvImageViewLeft.setVisibility(View.GONE);
            leftViewHolder.tvSendTimeLeft.setVisibility(View.GONE);
            leftViewHolder.tvVoiceViewLeft.setVisibility(View.GONE);
            leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvVoiceTimeLeft.setVisibility(View.GONE);
            leftViewHolder.voiceUnreadViewLeft.setVisibility(View.GONE);
            leftViewHolder.chargeResponseLayoutLeft.setVisibility(View.GONE);
            leftViewHolder.linkLayoutLeft.setVisibility(View.GONE);
        }

        public void visibleLeft(){
            leftViewHolder.leftLayout.setVisibility(View.VISIBLE);
            leftViewHolder.faceImageViewLeft.setVisibility(View.VISIBLE);
            leftViewHolder.imageBarLeft.setVisibility(View.VISIBLE);
            leftViewHolder.messageBarLeft.setVisibility(View.VISIBLE);
            leftViewHolder.sendStatusViewLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvContentLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvImageViewLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvSendTimeLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvVoiceViewLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvVoiceLayoutLeft.setVisibility(View.VISIBLE);
            leftViewHolder.tvVoiceTimeLeft.setVisibility(View.VISIBLE);
            leftViewHolder.voiceUnreadViewLeft.setVisibility(View.VISIBLE);
            leftViewHolder.chargeResponseLayoutLeft.setVisibility(View.VISIBLE);
            leftViewHolder.linkLayoutLeft.setVisibility(View.VISIBLE);
        }

        public void goneRight(){
            rightViewHolder.rightLayout.setVisibility(View.GONE);
            rightViewHolder.faceImageViewRight.setVisibility(View.GONE);
            rightViewHolder.imageBarRight.setVisibility(View.GONE);
            rightViewHolder.messageBarRight.setVisibility(View.GONE);
            rightViewHolder.sendStatusViewRight.setVisibility(View.GONE);
            rightViewHolder.tvContentRight.setVisibility(View.GONE);
            rightViewHolder.tvImageViewRight.setVisibility(View.GONE);
            rightViewHolder.tvSendTimeRight.setVisibility(View.GONE);
            rightViewHolder.tvVoiceViewRight.setVisibility(View.GONE);
            rightViewHolder.tvVoiceLayoutRight.setVisibility(View.GONE);
            rightViewHolder.tvVoiceTimeRight.setVisibility(View.GONE);
            rightViewHolder.voiceUnreadViewRight.setVisibility(View.GONE);
            rightViewHolder.chargeLayoutRight.setVisibility(View.GONE);
            rightViewHolder.linkLayoutRight.setVisibility(View.GONE);
        }

        public void visibleRight(){
            rightViewHolder.rightLayout.setVisibility(View.VISIBLE);
            rightViewHolder.faceImageViewRight.setVisibility(View.VISIBLE);
            rightViewHolder.imageBarRight.setVisibility(View.VISIBLE);
            rightViewHolder.messageBarRight.setVisibility(View.VISIBLE);
            rightViewHolder.sendStatusViewRight.setVisibility(View.VISIBLE);
            rightViewHolder.tvContentRight.setVisibility(View.VISIBLE);
            rightViewHolder.tvImageViewRight.setVisibility(View.VISIBLE);
            rightViewHolder.tvSendTimeRight.setVisibility(View.VISIBLE);
            rightViewHolder.tvVoiceViewRight.setVisibility(View.VISIBLE);
            rightViewHolder.tvVoiceLayoutRight.setVisibility(View.VISIBLE);
            rightViewHolder.tvVoiceTimeRight.setVisibility(View.VISIBLE);
            rightViewHolder.voiceUnreadViewRight.setVisibility(View.VISIBLE);
            rightViewHolder.chargeLayoutRight.setVisibility(View.VISIBLE);
            rightViewHolder.linkLayoutRight.setVisibility(View.VISIBLE);
        }

        public class LeftViewHolder {
            public RelativeLayout leftLayout;
            public ImageView faceImageViewLeft;
            public TextView tvSendTimeLeft;
            public TextView tvContentLeft;
            public ImageView tvImageViewLeft;
            public ImageView tvVoiceViewLeft;
            public RelativeLayout tvVoiceLayoutLeft;
            public TextView tvVoiceTimeLeft;
            public ImageView voiceUnreadViewLeft;
            public ProgressBar imageBarLeft;
            public ProgressBar messageBarLeft;
            public ImageView sendStatusViewLeft;
            public RelativeLayout chargeResponseLayoutLeft; // 自定义收费回复信息
            public RelativeLayout linkLayoutLeft;   // 显示link信息
            public TextView linkTitleViewLeft;
            public TextView linkDecViewLeft;
            public ImageView linkImageLeft;
        }

        public class RightViewHolder {
            public RelativeLayout rightLayout;
            public ImageView faceImageViewRight;
            public TextView tvSendTimeRight;
            public TextView tvContentRight;
            public ImageView tvImageViewRight;
            public ImageView tvVoiceViewRight;
            public RelativeLayout tvVoiceLayoutRight;
            public TextView tvVoiceTimeRight;
            public ImageView voiceUnreadViewRight;
            public ProgressBar imageBarRight;
            public ProgressBar messageBarRight;
            public ImageView sendStatusViewRight;
            public RelativeLayout chargeLayoutRight; // 显示自定义收费项目信息
            public RelativeLayout linkLayoutRight; // 显示link信息
            public TextView linkTitleViewRight;
            public TextView linkDecViewRight;
            public ImageView linkImageRight;
        }
    }
}
