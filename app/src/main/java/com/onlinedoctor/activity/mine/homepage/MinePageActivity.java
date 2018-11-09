package com.onlinedoctor.activity.mine.homepage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.onlinedoctor.activity.chats.ImageDetailActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.mine.MineNetManager;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.pojo.mine.PackDoctorInfo;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.util.BitmapUtil;
import com.onlinedoctor.util.DisplayUtil;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.util.UILImageLoader;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.finalteam.galleryfinal.GalleryConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

public class MinePageActivity extends Activity implements OnClickListener {
    public static final String TAG = "MinePageActivity";
    public static final String MINE_PAGE_DATA = "mine_page_data";

    public static final int PHOTO_CAPTURE = 1;
    public static final int PHOTO_GET = 2;
    public static final int PHOTO_CROP = 3;
    public static final int PHOTO_AUTH_GET = 221;
    public static final int PHOTO_AUTH_TAKE = 222;
    public static final int MODIFY_DOCTOR_NAME = 4;
    public static final int MODIFY_DOCTOR_EMAIL = 5;
    public static final int MODIFY_DOCTOR_ADDR = 6;
    public static final int MODIFY_DOCTOR_INTRO = 7;
    public static final int MODIFY_DOCTOR_CID = 8;

    public static final int MODIFY_DOCTOR_CLINIC = 9;
    public static final int MODIFY_DOCTOR_ROOM = 10;
    public static final int MODIFY_DOCTOR_RANK = 11;

    public static int UseImageFor = 1; // 1为选择头像， 2 为选择认证照片

    public static final int LOCAL_DOCTOR_TB_ITEM_ID = 1; //本地表中该医生的自增id

    private static final String HEAD_IMAGE_NAME = "onlinedoctor_head.jpg";
    private static final String AUTH_IMAGE = "auth_image.jpg";
    private Uri imageUri = null;
    private Uri authImageUri = null;
    private ImageView headImageView = null;
    private String head_image_path = null;

    private ImageView qrcodeView = null;
    private Dialog qrcode_dialog = null;

    private int crop = 180;
    private File outputImage = null;

    private static SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
    private DoctorInfoServiceImpl dInfoImpl = new DoctorInfoServiceImpl(this, null);

    private TextView name_tv;
    private TextView email_tv;
    private TextView addr_tv;
    private TextView intro_tv;
    private TextView gender_tv;
    private TextView birth_tv;
    private TextView save_tv;
    private TextView cid_tv;
    private TextView clinic_tv;
    private TextView room_tv;
    private TextView rank_tv;
    private TextView auth_tv;
    private TextView back_tv;

    private FrameLayout progressBarHolder;
    private ProgressBar progressBarHeader;
    private Context mContext = this;

    private DoctorInfo serverDoInfo = null; //从服务器上拿到的数据
    private DoctorInfo localDoInfo = null;  //从本地DB中都到的数据
    private DoctorInfo settingDoInfo = null; //医生正在设置的数据
    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MineNetManager.DOWNLOAD_SUCCESSFUL: {
                    Log.d(TAG, "download successful");
                    String data = msg.obj.toString();
                    Log.d("data", data);
                    //String synObject = new JSONObject(data).getString("synObject");
                    PackDoctorInfo pdInfo = new Gson().fromJson(data, PackDoctorInfo.class);

                    //将从服务器获得信息保存到本地DB
                    serverDoInfo = new DoctorInfo();
                    serverDoInfo.setTime(pdInfo.getLastTime());
                    serverDoInfo.setName(pdInfo.getName());
                    serverDoInfo.setGender(pdInfo.getGender());
                    serverDoInfo.setIntro(pdInfo.getSelfInfo());
                    serverDoInfo.setClinic(pdInfo.getClinic());
                    serverDoInfo.setRoom(pdInfo.getRoom());
                    serverDoInfo.setRank(pdInfo.getRank());
                    serverDoInfo.setThumbnail(pdInfo.getThumbnail());
                    serverDoInfo.setAvatar(pdInfo.getAvatar());
                    serverDoInfo.setCid(pdInfo.getCid());
                    serverDoInfo.setBirth(pdInfo.getBirthday());
                    serverDoInfo.setEmail(pdInfo.getEmail());
                    serverDoInfo.setQrcode(pdInfo.getQrcode());
                    serverDoInfo.setAuth_status(pdInfo.getAuthStatus());

                    if (dInfoImpl.isEmpty()) {
                        Log.d("本地表", "为空");
                        int id = dInfoImpl.insert(serverDoInfo);
                        serverDoInfo.setId(id);
                    } else {
                        Log.d("本地表", "不为空");
                        serverDoInfo.setId(LOCAL_DOCTOR_TB_ITEM_ID);
                        dInfoImpl.update(serverDoInfo);
                    }
                    readFromLocalDB();
                    settingDoInfo = new DoctorInfo(serverDoInfo);
                    progressBarHolder.setVisibility(View.GONE);

                    break;
                }
                case MineNetManager.DOWNLOAD_FAIL: {
                    Log.d(TAG, "服务器端没有该用户的信息纪录");
                    String connect_server_fail = getResources().getString(R.string.connect_server_fail);
                    Toast.makeText(mContext, connect_server_fail, Toast.LENGTH_SHORT).show();
                    readFromLocalDB();
                    settingDoInfo = new DoctorInfo(localDoInfo);
                    progressBarHolder.setVisibility(View.GONE);
                    break;
                }

                case MineNetManager.INSERT_IMAGE_SUCCESSFUL: {
                    if (Common.IsUploadToQiniu) {
                        String result = msg.obj.toString();
                        Log.d("result", result);
                        try {
                            String thumbnail = new JSONObject(result).getString("thumbnailPath");
                            String avatar = new JSONObject(result).getString("avaterPath");
                            Logger.d("thumbnail", thumbnail);
                            Logger.d("avatar", avatar);

                            settingDoInfo.setThumbnail(thumbnail);
                            settingDoInfo.setAvatar(avatar);
                            if (thumbnail != null) {
                                MineNetManager.download_image(thumbnail, headImageView);
                            } else {
                                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                headImageView.setImageBitmap(bitmap);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String result = msg.obj.toString();
                        Log.d("result", result);
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, Object>>() {
                        }.getType();
                        Map<String, Object> map = gson.fromJson(result, type);
                        int code = (int) Double.parseDouble(String.valueOf(map.get(Common.code)));
                        if (code == 200) {
                            String data = JsonUtil.getJsonStringByKey(result, "data").toString();
                            Log.d("data", data);
                            Map<String, Object> dataMap = (Map<String, Object>) map.get(Common.data);
                            String imageJson = JsonUtil.objectToJson(dataMap);
                            Log.d("imageJson", imageJson);
                            try {
                                String thumbnail = new JSONObject(imageJson).getString("thumbnailPath");
                                String avatar = new JSONObject(imageJson).getString("avaterPath");
                                Log.d("thumbnail", thumbnail);
                                Log.d("avatar", avatar);

                                settingDoInfo.setThumbnail(thumbnail);
                                settingDoInfo.setAvatar(avatar);
                                if (thumbnail != null) {
                                    MineNetManager.download_image(thumbnail, headImageView);


                                } else {
                                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                                    headImageView.setImageBitmap(bitmap);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }

                case MineNetManager.INSERT_AUTH_PHOTO_SUCCESSFUL: {
                    if (Common.IsUploadToQiniu) {
                        String result = msg.obj.toString();
                        Log.d("result", result);
                        try {
                            String thumbnail = new JSONObject(result).getString("thumbnailPath");
                            String avatar = new JSONObject(result).getString("avaterPath");
                            Log.d("thumbnail", thumbnail);
                            Log.d("avatar", avatar);
                            settingDoInfo.setAuth_photo(avatar);
                            auth_tv.setText(authStatus2Str(mContext, Common.AUTH_IN_PROGRESS));
                            progressBarHolder.setVisibility(View.GONE);
                            Toast.makeText(mContext, "上传认证照片成功!", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String result = msg.obj.toString();
                        Log.d("result", result);
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, Object>>() {
                        }.getType();
                        Map<String, Object> map = gson.fromJson(result, type);
                        int code = (int) Double.parseDouble(String.valueOf(map.get(Common.code)));
                        if (code == 200) {
                            String data = JsonUtil.getJsonStringByKey(result, "data").toString();
                            Log.d("data", data);
                            Map<String, Object> dataMap = (Map<String, Object>) map.get(Common.data);
                            String imageJson = JsonUtil.objectToJson(dataMap);
                            Log.d("imageJson", imageJson);
                            try {
                                String thumbnail = new JSONObject(imageJson).getString("thumbnailPath");
                                String avatar = new JSONObject(imageJson).getString("avaterPath");
                                Log.d("thumbnail", thumbnail);
                                Log.d("avatar", avatar);
                                settingDoInfo.setAuth_photo(avatar);
                                auth_tv.setText(authStatus2Str(mContext, Common.AUTH_IN_PROGRESS));
                                progressBarHolder.setVisibility(View.GONE);
                                Toast.makeText(mContext, "上传认证照片成功!", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
                case MineNetManager.INSERT_SUCCESSFUL: {
                    Long time = (Long) (msg.obj);
                    // spfEditor.putLong("time", time);
                    finish();
                    break;
                }
                case MineNetManager.INSERT_FAIL: {
                    Toast.makeText(mContext, "保存信息失败", Toast.LENGTH_SHORT).show();
                    break;
                }

                case MineNetManager.UPDATE_SUCCESSFUL: {
                    Long time = (Long) (msg.obj);
                    // spfEditor.putLong("time", time);
                    finish();
                    break;
                }
                case MineNetManager.UPDATE_FAIL: {
                    Toast.makeText(mContext, "保存信息失败", Toast.LENGTH_SHORT).show();
                    break;
                }

                case MineNetManager.GET_QR_CODE_SUCCESSFUL: {

                    String result = msg.obj.toString();
                    Log.d("qrcode_url = ", result);
                    settingDoInfo.setQrcode(result);
                    //dInfoImpl.update(settingDoInfo);
                    //localDoInfo = dInfoImpl.get(LOCAL_DOCTOR_TB_ITEM_ID);
                    showQRcode(result);

                    break;
                }
                default:
                    break;
            }
            progressBarHeader.setVisibility(View.GONE);
            progressBarHolder.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_info);

        // 修改actionbar标题
        ((TextView) findViewById(R.id.actionbar_common_backable_title_tv)).setText("编辑基本信息");

        // 修改actionbar右边按钮和逻辑
        save_tv = ((TextView) findViewById(R.id.actionbar_common_backable_right_tv));
        save_tv.setText("");
        back_tv = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);

        headImageView = (ImageView) findViewById(R.id.doctor_info_head_iv);

        name_tv = (TextView) findViewById(R.id.doctor_info_name_tv);
        cid_tv = (TextView) findViewById(R.id.doctor_info_serial_tv);
        gender_tv = (TextView) findViewById(R.id.doctor_info_gender_tv);
        birth_tv = (TextView) findViewById(R.id.doctor_info_birth_tv);
        email_tv = (TextView) findViewById(R.id.doctor_info_email_tv);
        addr_tv = (TextView) findViewById(R.id.doctor_info_detial_address_tv);
        intro_tv = (TextView) findViewById(R.id.doctor_info_introduce_tv);
        clinic_tv = (TextView) findViewById(R.id.doctor_info_clinic_addr_tv);
        room_tv = (TextView) findViewById(R.id.doctor_info_room_tv);
        rank_tv = (TextView) findViewById(R.id.doctor_info_rank_tv);
        auth_tv = (TextView) findViewById(R.id.doctor_info_auto_verify_status_tv);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        progressBarHeader = (ProgressBar) findViewById(R.id.progressBarHeader);

        // 监听修改其他信息
        //save_tv.setOnClickListener(this);
        back_tv.setOnClickListener(this);
        findViewById(R.id.doctor_info_head_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_name_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_serial_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_codebar_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_auto_verify_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_gender_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_birth_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_email_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_detial_address_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_introduce_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_clinic_addr_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_room_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_rank_view).setOnClickListener(this);
        findViewById(R.id.doctor_info_head_iv).setOnClickListener(this);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // this.back();
        this.save();
        finish();
    }

    private void init() {
        //从服务器上拉最新的文本信息，不包含从图片服务器上来头像（这个需要比对时间戳来决定）
        progressBarHolder.setVisibility(View.VISIBLE);
        MineNetManager.download(handler);
//        readFromLocalDB();

//        if (settingDoInfo == null) {
//            settingDoInfo = (DoctorInfo) localDoInfo.clone();
//        }

    }

    private void readFromLocalDB() {
        //测试本地SharedPreferences中是否有纪录
        if (dInfoImpl.isEmpty()) {
            Log.d(TAG, "本地DB表没有纪录");
            return;
        }

        Log.d(TAG, "从本地DB表读取纪录");
        localDoInfo = dInfoImpl.get(LOCAL_DOCTOR_TB_ITEM_ID);

        //直接从服务器端下载小头像
        MineNetManager.download_image(localDoInfo.getThumbnail(), headImageView);

        // 读取名字
        // String name = sPref.getString("name", "");
        String name = localDoInfo.getName();
        if (!name.isEmpty())
            name_tv.setText(name);

        // 读取CID
        //String cid = sPref.getString("cid", "");
        String cid = localDoInfo.getCid();
        if (!cid.isEmpty())
            cid_tv.setText(cid);

        // 读取地址
        // String addr = sPref.getString("addr", "");
        String addr = localDoInfo.getAddr();
        if (!addr.isEmpty())
            addr_tv.setText(addr);

        // 读取email
        // String email = sPref.getString("email", "");
        String email = localDoInfo.getEmail();
        if (!email.isEmpty())
            email_tv.setText(email);

        // 读取性别
        // String gender = sPref.getString("gender", "");
        String gender = localDoInfo.getGender();
        if (!gender.isEmpty())
            gender_tv.setText(gender);

        // 读取生日
        // String birth = sPref.getString("brith", "");
        Long longBirth = localDoInfo.getBirth();
        Log.d("~~longBrith = ", Long.toString(longBirth));
        String birth = new SimpleDateFormat("yyyy-MM-dd").format(new Date(longBirth));
        Log.d("~~birth = ", birth);
        if (!birth.isEmpty())
            birth_tv.setText(birth);

        // 读取介绍
        // String intro = sPref.getString("intro", "");
        String intro = localDoInfo.getIntro();
        if (!intro.isEmpty())
            intro_tv.setText(intro);

        // 读取诊所地址
        // String clinic = sPref.getString("clinic", "");
        String clinic = localDoInfo.getClinic();
        if (!clinic.isEmpty())
            clinic_tv.setText(clinic);

        // 读取科室
        // String room = sPref.getString("room", "");
        String room = localDoInfo.getRoom();
        if (!room.isEmpty())
            room_tv.setText(room);

        // 读取职称
        // String rank = sPref.getString("rank","");
        String rank = localDoInfo.getRank();
        if (!rank.isEmpty())
            rank_tv.setText(rank);


        // 二维码
        LinearLayout ll_qrcode = (LinearLayout) findViewById(R.id.doctor_info_codebar_view);
        ll_qrcode.setClickable(false);

        // 读取认证信息
        int authStatusId = localDoInfo.getAuth_status();
        String authStatusStr = authStatus2Str(mContext, authStatusId);
        if (authStatusId == Common.AUTH_PASS) {
            auth_tv.setTextColor(mContext.getResources().getColor(R.color.blue));
            setInvalid();
            setValid();
        } else if (authStatusId == Common.AUTH_IN_PROGRESS) {
            setInvalid();
        } else {
            auth_tv.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        auth_tv.setText(authStatusStr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            // 保存所有修改(actionbar右边按钮)
//            case R.id.actionbar_common_backable_right_tv:
//                save();
//                break;
            // 推出前比较是否修改，如果修改，提示用户保存
            case R.id.actionbar_common_backable_left_tv:
                // back();
                save();
                finish();
                break;
            // 编辑头像
            case R.id.doctor_info_head_view:
                //setHead();
                setHeadByGalleryFinal();
                break;
            // 查看头像
            case R.id.doctor_info_head_iv:
                getHead();
                break;
            // 编辑姓名
            case R.id.doctor_info_name_view:
                setName();
                break;
            // 编辑医行者号
            case R.id.doctor_info_serial_view:
                setCid();
                break;
            // 编辑二维码
            case R.id.doctor_info_codebar_view:
                getQRcode();
                break;
            // 权威认证
            case R.id.doctor_info_auto_verify_view:
                //setAuth();
                setAuthByGalleryFinal();
                break;
            // 性别
            case R.id.doctor_info_gender_view:
                setGender();
                break;
            // 生日
            case R.id.doctor_info_birth_view:
                setBirth();
                break;
            // 邮箱
            case R.id.doctor_info_email_view:
                setEmail();
                break;
            // 地址
            case R.id.doctor_info_detial_address_view:
                setAddr();
                break;
            // 门诊地址
            case R.id.doctor_info_clinic_addr_view:
                setClinic();
                break;
            // 科室
            case R.id.doctor_info_room_view:
                setRoom();
                break;
            // 职称
            case R.id.doctor_info_rank_view:
                setRank();
                break;
            // 介绍
            case R.id.doctor_info_introduce_view:
                setIntro();
                break;
            default:
                break;
        }

    }

    // 保存所有修改
    private void save() {
        Log.d(TAG, "将个人信息保存到服务器中");
        //progressBarHolder.setVisibility(View.VISIBLE);

        if (dInfoImpl.isEmpty()) {
            int id = dInfoImpl.insert(settingDoInfo);
            MineNetManager.insert(settingDoInfo, handler);
        } else {
            settingDoInfo.setId(LOCAL_DOCTOR_TB_ITEM_ID);
            dInfoImpl.update(settingDoInfo);
            MineNetManager.update(settingDoInfo, localDoInfo, handler);

        }
    }

    // 退出前比较是否修改，如果修改，提示用户保存
    private void back() {
        Logger.d("settingDoInfo: ", settingDoInfo.toString());
        Logger.d("localDoInfo: ", localDoInfo.toString());
        if ((!settingDoInfo.equals(localDoInfo)) ||
                !settingDoInfo.getAuth_photo().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("是否保存编辑?");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    save();
                }

            });
            builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });
            builder.create().show();
        } else {
            finish();
        }
    }

    // 通过GalleryFinal选择图片并裁剪
    private void setHeadByGalleryFinal() {
        UseImageFor = 1;
        GalleryConfig.Builder builder = new GalleryConfig.Builder(MinePageActivity.this);
        builder.imageloader(new UILImageLoader());
        builder.singleSelect();
        builder.enableEdit();
        builder.enableRotate();
        builder.enableCrop();
        builder.showCamera();
        builder.cropSquare();
        GalleryConfig config = builder.build();
        GalleryFinal.open(config);
    }


    // 设置头像
    private void setHead() {
        final String[] items = {"拍照", "相册"};
        // headImageView = (ImageView)findViewById(R.id.doctor_info_head_iv);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        outputImage = new File(Common.sdPicSave, HEAD_IMAGE_NAME);
                        Log.d("head_path =: ", Common.sdPicSave);
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        try {
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageUri = Uri.fromFile(outputImage);
//                        spfEditor.putString("imageUri", imageUri.toString());
                        switch (which) {

                            // 调用“拍照”功能
                            case 0:
                                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, PHOTO_CAPTURE); // 启动相机程序
                                break;

                            // 调用“相册”功能
                            case 1:
                                intent = new Intent("android.intent.action.PICK");
                                intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                                intent.putExtra("crop", "true");
                                intent.putExtra("aspectX", 1);
                                intent.putExtra("aspectY", 1);
                                intent.putExtra("outputX", crop);
                                intent.putExtra("outputY", crop);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, PHOTO_GET);
                                break;
                        }
                    }
                }).show();
    }

    // 查看头像
    private void getHead() {
        Intent intent = new Intent(this, ImageDetailActivity.class);
        if (settingDoInfo == null || settingDoInfo.getAvatar().isEmpty() || settingDoInfo.getAvatar() == null) {
            setHead();
            return;
        }
        intent.putExtra(Common.KEY_IMAGE_avaterPath, settingDoInfo.getAvatar());
        int[] location = new int[2];
        headImageView.getLocationOnScreen(location);
        intent.putExtra("locationX", location[0]);
        intent.putExtra("locationY", location[1]);

        intent.putExtra("width", headImageView.getWidth());
        intent.putExtra("height", headImageView.getHeight());
        startActivity(intent);
    }

    // 编辑姓名
    private void setName() {
        Intent intent = new Intent(this, ModifyDoctorName.class);
        String name = name_tv.getText().toString();
        intent.putExtra("name", name);
        startActivityForResult(intent, MODIFY_DOCTOR_NAME);
    }

    // 编辑二维码
    private void getQRcode() {
        Log.d(TAG, "in getQRcode");
        String qrcode_url = settingDoInfo.getQrcode();
        if (qrcode_url.isEmpty()) {
            Log.d(TAG, "qrcode_url is empty");
            MineNetManager.get_qrcode(handler);
        } else {
            Log.d(TAG, "qrcode_url is not empty");

            showQRcode(localDoInfo.getQrcode());
        }
    }

    private void setAuthByGalleryFinal() {
        UseImageFor = 2;
        GalleryConfig.Builder builder = new GalleryConfig.Builder(MinePageActivity.this);
        builder.imageloader(new UILImageLoader());
        builder.singleSelect();
        builder.enableEdit();
        builder.enableRotate();
        builder.enableCrop();
        builder.showCamera();
        int width = DisplayUtil.getDisplaymetrics(MinePageActivity.this).widthPixels;
        int height = DisplayUtil.getDisplaymetrics(MinePageActivity.this).heightPixels;
        builder.cropWidth(width/2);
        builder.cropHeight(height/2);
        GalleryConfig config = builder.build();
        GalleryFinal.open(config);
    }

    // 权威认证
    private void setAuth() {
        final String[] items = {"拍照", "相册"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        File outputImage = new File(Common.sdPicSave, AUTH_IMAGE);
                        Log.d("auth_path =: ", Common.sdPicSave);
                        if (outputImage.exists()) {
                            outputImage.delete();
                        }
                        try {
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        authImageUri = Uri.fromFile(outputImage);
                        switch (which) {
                            // 调用“拍照”功能
                            case 0:
                                intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputImage));
                                startActivityForResult(intent, PHOTO_AUTH_TAKE);
                                break;
                            // 调用“相册”功能
                            case 1:
                                intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent, PHOTO_AUTH_GET);
                                break;
                        }
                    }
                }).show();
    }

    private void showQRcode(final String qrcode_url) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.qrcode, null);
        ImageView qrcodeView = (ImageView) linearLayout.findViewById(R.id.qrcode_iv);
        TextView saveView = (TextView) linearLayout.findViewById(R.id.qrcode_save);
        final Dialog dialog = new AlertDialog.Builder(mContext).create();
        Log.d("qrcode_url", qrcode_url);
        MineNetManager.download_image(qrcode_url, qrcodeView);
        //MineNetManager.download_image(localDoInfo.getAvatar(), qrcodeView);

        dialog.show();
        dialog.setCancelable(true);
        dialog.getWindow().setContentView(linearLayout);

        saveView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageLoader.getInstance().loadImage(qrcode_url, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        String outPath = Common.sdPicSave + File.separator + "二维码名片.jpg";
                        try {
                            File file = new File(Common.sdPicSave);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            BitmapUtil.storeImageToSystem(bitmap, outPath);
                            Toast.makeText(MinePageActivity.this, "成功保存至" + outPath, Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }
        });

        linearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    // 设置性别
    private void setGender() {
        final String[] items = {"男", "女"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String gender = items[which];
                gender_tv.setText(gender);
                //spfEditor.putString("gender", gender);
                settingDoInfo.setGender(gender);
            }
        }).show();
    }

    // 设置生日
    private void setBirth() {
        final Calendar cal = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                cal.clear();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                String birth = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

                birth_tv.setText(birth);
                long longBirth = cal.getTime().getTime();
                Log.d("BIRTH = ", Long.toString(longBirth));
                //spfEditor.putString("brith", birth);
                settingDoInfo.setBirth(longBirth);
            }
        };
//        new DatePickerDialog(this,listener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();

        String birth = birth_tv.getText().toString();
        if (birth.isEmpty()) {
            Log.d("birth", "is empty");
            DatePickerDialog dpd = new DatePickerDialog(this,
                    listener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
            dpd.show();
        } else {
            String[] date = birth.split("-");
            Log.d("date[0] = ", date[0]);
            Log.d("date[1] = ", date[1]);
            Log.d("date[2] = ", date[2]);
            cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
            cal.set(Calendar.MONTH, Integer.parseInt(date[1]));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));

            // 为什么月份要减去1？
            DatePickerDialog dpd = new DatePickerDialog(this,
                    listener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) - 1,
                    cal.get(Calendar.DAY_OF_MONTH));

            dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
            dpd.show();
        }
    }

    // 编辑邮箱
    private void setEmail() {
        Intent intent = new Intent(this, ModifyDoctorEmail.class);
        String email = email_tv.getText().toString();
        intent.putExtra("email", email);
        startActivityForResult(intent, MODIFY_DOCTOR_EMAIL);
    }

    // 编辑地址
    private void setAddr() {
        Intent intent = new Intent(this, ModifyDoctorAddress.class);
        String addr = addr_tv.getText().toString();
        intent.putExtra("addr", addr);
        startActivityForResult(intent, MODIFY_DOCTOR_ADDR);
    }

    // 设置门诊地址
    private void setClinic() {
        Intent intent = new Intent(this, ModifyDoctorClinic.class);
        String clinic = clinic_tv.getText().toString();
        intent.putExtra("clinic", clinic);
        startActivityForResult(intent, MODIFY_DOCTOR_CLINIC);
    }

    // 设置科室
    private void setRoom() {
        Intent intent = new Intent(this, ModifyDoctorRoom.class);
        String room = room_tv.getText().toString();
        intent.putExtra("room", room);
        startActivityForResult(intent, MODIFY_DOCTOR_ROOM);

    }

    // 设置医生职称
    private void setRank() {
        final String[] items = getResources().getStringArray(R.array.rank);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String rank = items[which];
                rank_tv.setText(rank);
                settingDoInfo.setRank(rank);
            }
        }).show();
    }

    // 设置介绍
    private void setIntro() {
        Intent intent = new Intent(this, ModifyDoctorIntroduction.class);
        String intro = intro_tv.getText().toString();
        intent.putExtra("intro", intro);
        startActivityForResult(intent, MODIFY_DOCTOR_INTRO);
    }

    //设置Cid
    private void setCid() {
        Intent intent = new Intent(this, ModifyDoctorCid.class);
        String cid = cid_tv.getText().toString();
        intent.putExtra("cid", cid);
        startActivityForResult(intent, MODIFY_DOCTOR_CID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_CAPTURE:
                Log.i(TAG, "PHOTO_CAPTURE");
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", crop);
                    intent.putExtra("outputY", crop);
                    intent.putExtra("scale", true);
                    intent.putExtra("return-data", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, PHOTO_CROP);
                }
                break;
            case PHOTO_GET:
                if (resultCode == RESULT_OK) {
                    Logger.d(TAG, "PHOTO_GET");
                    Logger.d("imageUri = ", imageUri.toString());
                    Bitmap bitmap = null;
                    try {
                        String path = BitmapUtil.getImageAbsolutePath(mContext, imageUri);
                        Logger.d("path = ", path);
                        InputStream is = new FileInputStream(path);
                        bitmap = BitmapFactory.decodeStream(is, null, null);
                        MineNetManager.insert_image(bitmap, handler);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PHOTO_AUTH_GET:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    Logger.d("imageUri = ", imageUri.toString());
                    Bitmap bitmap = null;
                    try {
                        String path = BitmapUtil.getImageAbsolutePath(mContext, imageUri);
                        Logger.d("path = ", path);
                        InputStream is = new FileInputStream(path);
                        bitmap = BitmapFactory.decodeStream(is, null, null);
                        if (bitmap != null) {
                            progressBarHolder.setVisibility(View.VISIBLE);
                            MineNetManager.insert_auth_image(bitmap, handler);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PHOTO_AUTH_TAKE:
                if (authImageUri != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(authImageUri));
                        if (bitmap != null) {
                            progressBarHolder.setVisibility(View.VISIBLE);
                            MineNetManager.insert_auth_image(bitmap, handler);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PHOTO_CROP:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "PHOTO_CROP");
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    if (bitmap != null) {
                        MineNetManager.insert_image(bitmap, handler);
                    }
                }
                break;
            case MODIFY_DOCTOR_NAME:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    Log.d("name", name);
                    name_tv.setText(name);
                    //spfEditor.putString("name", name);
                    settingDoInfo.setName(name);
                }
                break;
            case MODIFY_DOCTOR_EMAIL:
                if (resultCode == RESULT_OK) {
                    String email = data.getStringExtra("email");
                    email_tv.setText(email);
                    //spfEditor.putString("email", email);
                    settingDoInfo.setEmail(email);
                }
                break;
            case MODIFY_DOCTOR_ADDR:
                if (resultCode == RESULT_OK) {
                    String addr = data.getStringExtra("addr");
                    Log.d("addr", addr);
                    addr_tv.setText(addr);
                    settingDoInfo.setAddr(addr);
                }
                break;
            case MODIFY_DOCTOR_INTRO:
                if (resultCode == RESULT_OK) {
                    String intro = data.getStringExtra("intro");
                    Log.d("intro", intro);
                    intro_tv.setText(intro);
                    settingDoInfo.setIntro(intro);
                }
                break;
            case MODIFY_DOCTOR_CID:
                if (resultCode == RESULT_OK) {
                    String cid = data.getStringExtra("cid");
                    Log.d("cid", cid);
                    cid_tv.setText(cid);
                    settingDoInfo.setCid(cid);
                }
                break;
            case MODIFY_DOCTOR_CLINIC:
                if (resultCode == RESULT_OK) {
                    String clinic = data.getStringExtra("clinic");
                    Log.d("clinic", clinic);
                    clinic_tv.setText(clinic);
                    settingDoInfo.setClinic(clinic);
                }
                break;
            case MODIFY_DOCTOR_ROOM:
                if (resultCode == RESULT_OK) {
                    String room = data.getStringExtra("room");
                    Log.d("room", room);
                    room_tv.setText(room);
                    settingDoInfo.setRoom(room);
                }
                break;
            case MODIFY_DOCTOR_RANK:
                if (resultCode == RESULT_OK) {
                    String rank = data.getStringExtra("rank");
                    Log.d("rank", rank);
                    rank_tv.setText(rank);
                    settingDoInfo.setRank(rank);
                }
                break;
            case GalleryFinal.GALLERY_REQUEST_CODE: {
                if (resultCode == GalleryFinal.GALLERY_RESULT_SUCCESS) {
                    if (UseImageFor == 1) {
                        List<PhotoInfo> photoInfoList = (List<PhotoInfo>) data.getSerializableExtra(GalleryFinal.GALLERY_RESULT_LIST_DATA);
                        if (photoInfoList != null) {
                            Bitmap bitmap = null;
                            try {
                                String path = photoInfoList.get(0).getPhotoPath();
                                imageUri = Uri.parse(path);
                                Logger.d("path = ", path);
                                InputStream is = new FileInputStream(path);
                                bitmap = BitmapFactory.decodeStream(is, null, null);
                                progressBarHeader.setVisibility(View.VISIBLE);
                                MineNetManager.insert_image(bitmap, handler);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (UseImageFor == 2) {
                        List<PhotoInfo> photoInfoList = (List<PhotoInfo>) data.getSerializableExtra(GalleryFinal.GALLERY_RESULT_LIST_DATA);
                        if (photoInfoList != null) {
                            Bitmap bitmap = null;
                            try {
                                String path = photoInfoList.get(0).getPhotoPath();
                                imageUri = Uri.parse(path);
                                Logger.d("path = ", path);
                                InputStream is = new FileInputStream(path);
                                bitmap = BitmapFactory.decodeStream(is, null, null);
                                if (bitmap != null) {
                                    progressBarHolder.setVisibility(View.VISIBLE);
                                    MineNetManager.insert_auth_image(bitmap, handler);
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    private void setInvalid() {
        LinearLayout ll_name = (LinearLayout) findViewById(R.id.doctor_info_name_view);
        LinearLayout ll_gender = (LinearLayout) findViewById(R.id.doctor_info_gender_view);
        LinearLayout ll_clinic = (LinearLayout) findViewById(R.id.doctor_info_clinic_addr_view);
        LinearLayout ll_room = (LinearLayout) findViewById(R.id.doctor_info_room_view);
        LinearLayout ll_rank = (LinearLayout) findViewById(R.id.doctor_info_rank_view);
        LinearLayout ll_auth = (LinearLayout) findViewById(R.id.doctor_info_auto_verify_view);


        ll_name.setClickable(false);
        ll_gender.setClickable(false);
        ll_clinic.setClickable(false);
        ll_room.setClickable(false);
        ll_rank.setClickable(false);
        ll_auth.setClickable(false);
//        ll_qrcode.setClickable(true);//qrcode只有在认证通过的时候才开启

//        ll_name.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_light));
//        ll_gender.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_light));
//        ll_clinic.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_light));
//        ll_room.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_light));
//        ll_rank.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_light));
//        ll_auth.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_light));

        ((TextView) findViewById(R.id.name_tv)).setTextColor(
                mContext.getResources().getColor(R.color.gray_font));
        ((TextView) findViewById(R.id.auth_tv)).setTextColor(
                mContext.getResources().getColor(R.color.gray_font));
        ((TextView) findViewById(R.id.gender_tv)).setTextColor(
                mContext.getResources().getColor(R.color.gray_font));
        ((TextView) findViewById(R.id.addr_tv)).setTextColor(
                mContext.getResources().getColor(R.color.gray_font));
        ((TextView) findViewById(R.id.room_tv)).setTextColor(
                mContext.getResources().getColor(R.color.gray_font));
        ((TextView) findViewById(R.id.rank_tv)).setTextColor(
                mContext.getResources().getColor(R.color.gray_font));
//        ((TextView)findViewById(R.id.qrcode_tv)).setTextColor(
//                mContext.getResources().getColor(R.color.black)); //qrcode只有在认证通过的时候才开启

        name_tv.setTextColor(mContext.getResources().getColor(R.color.gray_font));
        gender_tv.setTextColor(mContext.getResources().getColor(R.color.gray_font));
        addr_tv.setTextColor(mContext.getResources().getColor(R.color.gray_font));
        room_tv.setTextColor(mContext.getResources().getColor(R.color.gray_font));
        rank_tv.setTextColor(mContext.getResources().getColor(R.color.gray_font));

    }

    private void setValid() {
        LinearLayout ll_qrcode = (LinearLayout) findViewById(R.id.doctor_info_codebar_view);
        ll_qrcode.setClickable(true);
        ((TextView) findViewById(R.id.qrcode_tv)).setTextColor(
                mContext.getResources().getColor(R.color.black));

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    public static String authStatus2Str(Context context, int authStatusId) {
        String statusStr;
        switch (authStatusId) {
            case Common.AUTH_NOT_CHECK:
                statusStr = context.getString(R.string.auth_not_check);
                break;
            case Common.AUTH_IN_PROGRESS:
                statusStr = context.getString(R.string.auth_in_progress);
                break;
            case Common.AUTH_NOT_PASS:
                statusStr = context.getString(R.string.auth_not_pass);
                break;
            case Common.AUTH_PASS:
                statusStr = context.getString(R.string.auth_pass);
                break;
            default:
                statusStr = context.getString(R.string.auth_not_check);
                ;
                break;
        }
        return statusStr;
    }
}
