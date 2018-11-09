package com.onlinedoctor.service;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.onlinedoctor.activity.chats.ChatActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.mine.homepage.MinePageActivity;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.net.NetworkManager;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.PackDoctorInfo;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.util.DateTransfer;
import com.onlinedoctor.face.FaceConversionUtil;


public class NotifyManager {

    private static NotifyManager instance;
    private NotificationManager notificationManager = null;
    private Context context = MyApplication.context;
    private Map<String, Integer> notifyIds = new HashMap<String, Integer>();
    private long[] pattern = {500,500,500,500,500,500,500,500,500};

    private final int AuthId = 1001;

    private Gson gson = new Gson();
    private Type type = new TypeToken<Map<String, Object>>() {}.getType();

    private NotifyManager() {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotifyManager getManager() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) {
                    instance = new NotifyManager();
                }
            }
        }
        return instance;
    }

    // 认证结果的通知
    public void notifyAuth(PackDoctorInfo doctorInfo){
        final Notification messageNotification = new Notification();
        messageNotification.icon = R.drawable.ic_logo;
        // 添加声音提示
        messageNotification.defaults = Notification.DEFAULT_SOUND;

        // 添加震动
        messageNotification.defaults |= Notification.DEFAULT_VIBRATE;
        messageNotification.vibrate = pattern;

        // LED灯闪烁
        messageNotification.defaults |= Notification.DEFAULT_LIGHTS;
        messageNotification.ledARGB = 0xff00ff00;
        messageNotification.ledOnMS = 300;
        messageNotification.ledOffMS = 1000;
        messageNotification.flags |= Notification.FLAG_SHOW_LIGHTS;

        final RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.item_list_notify_auth);
        contentView.setImageViewResource(R.id.logo, R.drawable.ic_logo);
        contentView.setTextViewText(R.id.message, doctorInfo.getName() + context.getResources().getString(R.string.auth_notice));
        messageNotification.contentView = contentView;
        messageNotification.flags = Notification.FLAG_AUTO_CANCEL;

        Intent notificationIntent = new Intent(context, MinePageActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        messageNotification.contentIntent = contentIntent;
        notificationManager.notify(AuthId, messageNotification);
    }

    // Notify by BriefMessagePojo
    public void notifyMessage(BriefMessagePojo pojo) {
        // define the information
        final Notification messageNotification = new Notification();
        messageNotification.icon = R.drawable.ic_logo;
        // 添加声音提示
        messageNotification.defaults = Notification.DEFAULT_SOUND;

        // 添加震动
        messageNotification.defaults |= Notification.DEFAULT_VIBRATE;
        messageNotification.vibrate = pattern;

        // LED灯闪烁
        messageNotification.defaults |= Notification.DEFAULT_LIGHTS;
        messageNotification.ledARGB = 0xff00ff00;
        messageNotification.ledOnMS = 300;
        messageNotification.ledOffMS = 1000;
        messageNotification.flags |= Notification.FLAG_SHOW_LIGHTS;

        final RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.item_list_notify);
        contentView.setImageViewResource(R.id.face_image, R.drawable.ic_logo);
        contentView.setTextViewText(R.id.time, DateTransfer.transferLongToDate(DateTransfer.dateFormat2, pojo.getTime()));
        contentView.setTextViewText(R.id.user_name, pojo.getUserName());
        String msg = "";
        String contentType = pojo.getContentType();
        SpannableString spannableString = null;
        if (contentType.equals(Common.MESSAGE_TYPE_text)) {
            spannableString = FaceConversionUtil.getInstace().getExpressionString(MyApplication.context,
                    pojo.getMessage(), "small");
        } else if (contentType.equals(Common.MESSAGE_TYPE_image)) {
            msg = "[" + String.valueOf(pojo.getCount()) + "条] " + "[图片]";
        } else if (contentType.equals(Common.MESSAGE_TYPE_voice)) {
            msg = "[" + String.valueOf(pojo.getCount()) + "条] " + "[语音]";
        } else if (contentType.equals(Common.MESSAGE_TYPE_link)) {
            msg = "[" + String.valueOf(pojo.getCount()) + "条] " + "[链接]";
        } else if (contentType.equals(Common.MESSAGE_TYPE_survey)) {
            msg = "[" + String.valueOf(pojo.getCount()) + "条] " + "[病情调查]";
        }else if(contentType.equals(Common.MESSAGE_TYPE_paymentSuccess)){
            msg = "[" + String.valueOf(pojo.getCount()) + "条] " + pojo.getUserName() + "支付了你的收费项目";
        }else if(contentType.equals(Common.MESSAGE_TYPE_paymentRequest)){
            Map<String, Object> map = gson.fromJson(pojo.getMessage(), type);
            String title = (String)map.get("title");
            msg = "[" + String.valueOf(pojo.getCount()) + "条] " +"[收费项目]"+title;
        }

        if (contentType.equals(Common.MESSAGE_TYPE_text)) {
            contentView.setTextViewText(R.id.message, "[" + String.valueOf(pojo.getCount()) + "条] " + spannableString);
        } else {
            contentView.setTextViewText(R.id.message, msg);
        }
        messageNotification.contentView = contentView;
        messageNotification.flags = Notification.FLAG_AUTO_CANCEL;

        // define the information when drop-down
        Intent notificationIntent = new Intent(context, ChatActivity.class);
        Log.i("NotifyManager", "userId " + pojo.getUserId());
        Bundle bundle = new Bundle();
        Patient patient = new Patient(pojo);
        bundle.putSerializable("patient", patient);
        notificationIntent.putExtras(bundle);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        messageNotification.contentIntent = contentIntent;
        Integer id = notifyIds.get(pojo.getUserId());
        if (null == id) {
            Integer newId = generateId(pojo.getUserId());
            notifyIds.put(pojo.getUserId(), newId);
            id = newId;
        }
        final int fid = id;
        ImageLoader.getInstance().loadImage(pojo.getFaceImageUrl(), new DisplayImageOptions.Builder().cacheInMemory(true).imageScaleType(ImageScaleType.NONE).build(), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                notificationManager.notify(fid, messageNotification);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                contentView.setImageViewBitmap(R.id.face_image, bitmap);
                notificationManager.notify(fid, messageNotification);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

    }

    /*
     * Notification cancel by userId
     */
    public void cancelById(String userId) {
        Integer id = notifyIds.get(userId);
        if (null == id) {
            return;
        } else {
            notifyIds.remove(userId);
            notificationManager.cancel(id);
        }
    }

    /*
     * Notification cancel All
     */
    public void cancelAll() {
        Iterator iterator = notifyIds.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String)entry.getKey();
            Integer id = (Integer)entry.getValue();
            notificationManager.cancel(id);
        }
        notifyIds.clear();
    }

    private int generateId(String userId) {
        int id = 0;
        char[] cs = userId.toCharArray();
        for (int a : cs) {
            id += a;
        }
        Log.i("NotificationId: ", String.valueOf(id));
        return id;
    }
}
