package com.onlinedoctor.adapter.chats;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.chats.ChatActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.mine.fee.SelfDefinedFeesActivity;
import com.onlinedoctor.activity.tools.prescription.PrescriptionActivity;
import com.onlinedoctor.activity.tools.survey.DiseaseSurveyActivity;
import com.onlinedoctor.activity.tools.response.FastResponseActivity;
import com.onlinedoctor.activity.tools.clinic.OfficeHoursActivity;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.sqlite.service.DoctorInfoService;
import com.onlinedoctor.util.SharedpreferenceManager;

import java.io.File;

public class ChatToolsAdapter extends BaseAdapter {

    private final Context context;
    private final String[] funcs;
    private final TypedArray images;
    private final LayoutInflater container;
    private String patientId;

    private final class GridItemView {
        public TextView func;
        public ImageView image;
    }

    @SuppressLint("Recycle")
    public ChatToolsAdapter(Context context, int funcArrayRes, int imageArrayRes, String patientId) {
        super();
        this.context = context;
        this.funcs = context.getResources().getStringArray(funcArrayRes);
        this.images = context.getResources().obtainTypedArray(imageArrayRes);
        container = LayoutInflater.from(context);
        this.patientId = patientId;
        Log.i("ImageAdapter", "imageAdapter init success" + funcs.length);
    }

    public int getCount() {
        return funcs.length;
    }

    public Object getItem(int position) {
        return funcs[position];
    }

    public long getItemId(int position) {
        Log.i("ImageAdapter", "get item:" + funcs.length);
        return position;
    }

    public View getView(int position, View view, ViewGroup parentGroup) {
        GridItemView holderView = null;
        if (view == null) {
            view = container.inflate(R.layout.tool_grid_item, null);
            holderView = new GridItemView();
            holderView.image = (ImageView) view.findViewById(R.id.toolImage);
            holderView.func = (TextView) view.findViewById(R.id.toolName);
            view.setTag(holderView);
        } else {
            holderView = (GridItemView) view.getTag();
        }
        holderView.image.setImageDrawable(images.getDrawable(position));
        holderView.func.setText(funcs[position]);
        holderView.image.setOnClickListener(new Listener(position, funcs[position]));

        return view;
    }

    public class Listener implements View.OnClickListener {

        private int type;
        private String func;

        public Listener(int position, String func) {
            this.type = position;
            this.func = func;
        }

        @Override
        public void onClick(View arg0) {
            switch (type) {
                case 0: {
                    if ("图片".equals(func)) {
                        // 启动相册
                        Intent intent = new Intent("android.intent.action.PICK");
                        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
//					int crop = 180;
//					intent.putExtra("crop", "true");
//					intent.putExtra("aspectX", 1);
//					intent.putExtra("aspectY", 1);
//					intent.putExtra("outputX", crop);
//					intent.putExtra("outputY", crop);
                        ((Activity) context).startActivityForResult(intent, ChatActivity.PHOTO_CROP);
                        Log.i("Lauch PicPick", "Launch PHOTO_CROP");
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else if ("文件".equals(func)) {
                        Toast.makeText(context, "暂不支持文件发送", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                case 1: {
                    // 调用相机
                    Intent intent = new Intent();
                    Intent intent_camera = context.getPackageManager().getLaunchIntentForPackage("com.android.camera");
                    if (intent_camera != null) {
                        intent.setPackage("com.android.camera");
                    }
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    File out = new File(Common.sdPicSave);
                    if (!out.exists()) {
                        out.mkdirs();
                    }
                    File file = new File(ChatActivity.TempPicPath);
                    Uri uri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    ((Activity) context).startActivityForResult(intent, ChatActivity.TAKE_PICTURE);
                    break;
                }
                case 2: {
                    // 快捷回复
                    Intent intent = new Intent();
                    intent.setClass(context, FastResponseActivity.class);
                    intent.putExtra("flag", FastResponseActivity.FLAG_FROM_CHAT_ACTIVITY);
                    ((Activity) context).startActivityForResult(intent, ChatActivity.FAST_RESPONSE);
                    break;
                }
                case 3: //门诊时间
                {
                    SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
                    String clinicPlanJson = spManager.getOne(OfficeHoursActivity.KEY_CLINIC_JSON);
                    String clinicPlanText = OfficeHoursActivity.convertFromJson2String(clinicPlanJson);
                    Message msg = new Message();
                    msg.what = Common.MSG_WHAT_CLINIC_PLAN;
                    msg.obj = clinicPlanText;
                    HandlerManager.getManager().getChatHandler().sendMessage(msg);
                    break;
                }
                case 4: // 二维码
                {
                    DoctorInfoService dInfoService = new DoctorInfoServiceImpl(context, null);
                    DoctorInfo doctorInfo = dInfoService.get(1);
                    if ((!doctorInfo.getQrcode().equals("")) && (doctorInfo.getQrcode() != null)) {
                        Message msg = new Message();
                        msg.obj = doctorInfo.getQrcode();
                        msg.what = Common.MSG_WHAT_QRCODE;
                        HandlerManager.getManager().getChatHandler().sendMessage(msg);
                    }
                    break;
                }
                case 5: // 自定义收费项目
                {
                    Intent intent = new Intent(context, SelfDefinedFeesActivity.class);
                    intent.putExtra("patientId", patientId);
                    intent.putExtra("check", true);
                    context.startActivity(intent);
                    break;
                }
                case 6: // 病情调查
                {
                    Intent intent = new Intent(context, DiseaseSurveyActivity.class);
                    intent.setType("selectQuestionnaire");
                    ((Activity) context).startActivityForResult(intent, ChatActivity.QUESTIONNAIRE_SELECTION);
                    break;
                }
                case 7: // 处方
                {
                    Intent intent = new Intent(context, PrescriptionActivity.class);
                    intent.putExtra("patientId", patientId);
                    ((Activity)context).startActivityForResult(intent, ChatActivity.PRESCRIPTION_RESULT);
                    break;
                }
                default: {
                    break;
                }
            }
            Message message = new Message();
            message.what = Common.MSG_WHAT_HIDE;
            HandlerManager.getInstance().getChatHandler().sendMessage(message);
        }

    }
}
