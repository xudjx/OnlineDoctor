package com.onlinedoctor.activity.tools.prescription;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.chats.ChatActivity;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.log.manager.LogManager;
import com.onlinedoctor.net.QiniuUtil;
import com.onlinedoctor.net.message.ImageMessage;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.message.UploadFileToQiniuMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Record;
import com.onlinedoctor.pojo.tools.prescription.PrescriptionDO;
import com.onlinedoctor.sqlite.dao.PrescriptionServiceImpl;
import com.onlinedoctor.sqlite.dao.RecordServiceImpl;
import com.onlinedoctor.sqlite.service.PrescriptionService;
import com.onlinedoctor.sqlite.service.RecordService;
import com.onlinedoctor.util.BitmapUtil;
import com.onlinedoctor.util.DisplayUtil;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.view.CustomClipLoading;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

/**
 * Created by xuweidong on 15/10/31.
 * 开处方
 */
public class PrescriptionActivity extends Activity implements View.OnClickListener {

    private final String TAG = "PrescriptionActivity";
    private Context context;

    private EditText yizhuEditText;
    private GridLayout imagesLayout;
    private Button createPresBtn;

    private int columnCount = 3;  // default
    private int columnWidth = 90; // default
    private int chooseImageId = 0;

    private final int PICK_IMAGE_COUNT = 6;

    private Dialog mProgressDialog;

    private static final int PICK_IMAGE = 100; // 返回码
    private List<MediaItem> mImagesPicked = new ArrayList<MediaItem>(); // 存储已经选择的图片
    private List<MediaItem> mMediaSelectedList; // 多图选择，暂存
    private MediaOptions options = null;

    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions bImageOptions;

    private SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();

    // 保存上传图片list信息
    private List<String> prescriptionImages = new ArrayList<>();
    private PrescriptionDO prescriptionDO;
    private String patientId = "";
    private String doctorId = "";

    private final int UploadImageSuccess = 1;
    private final int UploadImageFailed = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UploadImageSuccess:
                {
                    ((TextView) mProgressDialog.findViewById(R.id.progress_dialog_txt)).setText("上传成功");
                    dismissProgressDialog();

                    String thumbnail = "";
                    // 根据返回的list key，构造PrescriptionDO中的images jsonArray数据
                    List<String> qiniuKeys = new ArrayList<>();
                    List<HashMap<String, String>> images = new ArrayList<>();
                    qiniuKeys = (List<String>)JsonUtil.jsonToList(msg.obj.toString());
                    for(String key: qiniuKeys){
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Common.KEY_IMAGE_thumbnailPath, QiniuUtil.generateWordMark(QiniuUtil.getThumbnailUrl(key, QiniuUtil.Type_Image, QiniuUtil.ThumbnailMode200x200),null));
                        map.put(Common.KEY_IMAGE_avaterPath, QiniuUtil.generateWordMark(QiniuUtil.getDownloadUrl(key, QiniuUtil.Type_Image), null));
                        images.add(map);
                    }
                    thumbnail = images.get(0).get(Common.KEY_IMAGE_thumbnailPath);
                    Logger.i("PrescriptionDO", "thumbnail: " + thumbnail);

                    prescriptionDO = new PrescriptionDO();
                    doctorId = spManager.getOne("keySid");
                    prescriptionDO.setPrescription_imgs(JsonUtil.objectToJson(images));
                    prescriptionDO.setDoctor_id(doctorId);
                    prescriptionDO.setRemark(yizhuEditText.getText().toString());
                    prescriptionDO.setCreate_time(System.currentTimeMillis());
                    prescriptionDO.setPatient_id(patientId);
                    Logger.i("PrescriptionDO", prescriptionDO.toString());

                    // upload to server
                    // todo

                    // insert to database prescription_tb
                    PrescriptionService prescriptionService = new PrescriptionServiceImpl();
                    boolean flag = prescriptionService.addPrescriptionInfo(prescriptionDO);
                    Logger.i("PrescriptionDO","<Insert prescriptionDO to prescription_tb success>");

                    // insert to record_tb
                    RecordService recordService = new RecordServiceImpl(null, null);
                    Record record = new Record();
                    record.setCreated(prescriptionDO.getCreate_time());
                    record.setPatientId(prescriptionDO.getPatient_id());
                    record.setDoctorId(prescriptionDO.getDoctor_id());
                    record.setRecordType(Record.TYPE_PRESCRIPTION);
                    record.setRecordPic(prescriptionDO.getPrescription_imgs());
                    record.setThumbnail(thumbnail);
                    int flag2 = recordService.addRecord(record);
                    Logger.i("PrescriptionDO","<Insert prescriptionDO to record_tb success>");

                    // 返回聊天界面
                    Intent intent = new Intent();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable("prescription",prescriptionDO);
                    intent.putExtras(mBundle);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                }
                case UploadImageFailed:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tool_prescription);
        context = this;

        doctorId = spManager.getOne("keysid");
        patientId = getIntent().getStringExtra("patientId");

        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("拍方抓药");
        TextView right_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        right_tv.setText("");

        yizhuEditText = (EditText) findViewById(R.id.yizhu_et);
        imagesLayout = (GridLayout) findViewById(R.id.prescription_images_layout);
        createPresBtn = (Button) findViewById(R.id.createPres_btn);
        createPresBtn.setOnClickListener(this);

        // 计算GridLayout最多可以放多少个ImageView
        calculateMacColumn();
        imagesLayout.setColumnCount(columnCount); // 设置每行显示多少张图片
        appendImageView(0, null);

        // Init ImageChoose Option
        MediaOptions.Builder builder = new MediaOptions.Builder();
        options = builder.canSelectMultiPhoto(true)
                .canSelectMultiVideo(true).canSelectBothPhotoVideo()
                .setMediaListSelected(mMediaSelectedList).build();

        bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageOnFail(R.drawable.ic_error).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();

        LogManager.getManager(MyApplication.context).registerCrashHandler();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createPres_btn: {
                uploadPrescriptions();
                break;
            }
            default:
                break;
        }
    }

    private void appendImageView(int flag, Uri imageUrl) {
        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(context, columnWidth), DisplayUtil.dip2px(context, columnWidth));
        params.setMargins(DisplayUtil.dip2px(context, 10), 10, DisplayUtil.dip2px(context, 10), 10);
        imageView.setLayoutParams(params);
        if (flag == 0) { // 选择图片的ImageView
            chooseImageId = View.generateViewId();
            imageView.setId(chooseImageId);
            imageView.setImageResource(R.drawable.image_add);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setOnClickListener(new ImageClickListener());
        } else if (flag == 1) { // 已选择的处方照片
            if (imageUrl == null) {
                return;
            }
            Logger.i("Append ImageUrl", imageUrl.toString());
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageLoader.displayImage(imageUrl.toString(), imageView, bImageOptions);
        }
        imagesLayout.addView(imageView);
        return;
    }

    private class ImageClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // chooseImageId可能会动态变化
            if (view.getId() == chooseImageId) {
                Logger.i(TAG, "Choose Image");
                if (options != null) {
                    MediaPickerActivity.open((Activity) context, PICK_IMAGE, options);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                mMediaSelectedList = MediaPickerActivity.getMediaItemSelected(data);
                Logger.i(TAG, mMediaSelectedList.toString());
                if (mMediaSelectedList == null) {
                    return;
                }
                mImagesPicked.addAll(mMediaSelectedList);
                Logger.i(TAG, "已选择图片大小：" + mImagesPicked.size());
                appendSelectedImages(mMediaSelectedList);
            }
        }
    }

    private void appendSelectedImages(List<MediaItem> images) {
        if (images == null || images.size() <= 0) {
            return;
        }
        int imageCount = imagesLayout.getChildCount();
        imagesLayout.removeViewAt(imageCount - 1);
        for (MediaItem item : images) {
            appendImageView(1, item.getUriOrigin());
        }

        // 再把加号的图标添加到ImagesLayout中
        if (mImagesPicked.size() < PICK_IMAGE_COUNT) {
            appendImageView(0, null);
        }
    }

    private void uploadPrescriptions() {
        if (mImagesPicked.size() <= 0) {
            Toast.makeText(context, "请选择处方图片", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressDialod();

        final MessageManager messageManager = MessageManager.getMessageManager();
        String token = spManager.getOne("keyQiniuTokenImg");

        // upload Image
        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < mImagesPicked.size(); i++) {
            MediaItem item = mImagesPicked.get(i);
            String imagePath = BitmapUtil.getImageAbsolutePath(MyApplication.context, item.getUriOrigin());
            fileList.add(new File(imagePath));
        }

        UploadFileToQiniuMessage qiniuMessage = new UploadFileToQiniuMessage(null, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Logger.i(TAG,"<prescription callback result>" + result);
                Message message = new Message();
                message.obj = result;
                message.what = UploadImageSuccess;
                handler.sendMessage(message);
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {
            @Override
            public boolean handle(int errorCode, String errorMessage) {
                dismissProgressDialog();
                Toast.makeText(context, "处方上传失败，请检查网络状况", Toast.LENGTH_LONG).show();
                return false;
            }
        }, fileList,null, token);
        messageManager.sendMessage(qiniuMessage);
    }

    private void calculateMacColumn() {
        DisplayMetrics metrics = DisplayUtil.getDisplaymetrics(PrescriptionActivity.this);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int widthDp = DisplayUtil.px2dip(PrescriptionActivity.this, width);
        Logger.i(TAG, "width=" + width + " widthDp=" + widthDp);
        // 考虑到View之间的Margin数值
        columnCount = (widthDp - (10 * 4)) / columnWidth;
        return;
    }

    private void showProgressDialod(){
        mProgressDialog = new Dialog(context, R.style.Dialogstyle);
        mProgressDialog.setContentView(R.layout.dialog_progress);
        CustomClipLoading loading = (CustomClipLoading) mProgressDialog.findViewById(R.id.progress_dialog_loading);
        TextView textView = (TextView) mProgressDialog.findViewById(R.id.progress_dialog_txt);
        textView.setText("正在上传处方...");
        mProgressDialog.show();
    }

    private void dismissProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogManager.getManager(MyApplication.context).unregisterCrashHandler();
    }
}
