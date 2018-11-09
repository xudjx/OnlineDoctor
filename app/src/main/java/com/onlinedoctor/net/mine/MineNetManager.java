package com.onlinedoctor.net.mine;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.QiniuUtil;
import com.onlinedoctor.net.message.ImageMessage;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.net.message.UploadFileToQiniuMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.pojo.mine.PackDoctorInfo;
import com.onlinedoctor.service.NotifyManager;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

// Mine和服务器交互的类
public class MineNetManager {
    private static final String MINE_URL = Common.MineUrl;
    private static final String IMAGE_URL = Common.ImageHttpUrl;
    private static final String QR_CODE_URL = Common.QRCodeUrl;

    private static final String TAG = "MineNetManager";
    private static SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
//	private static String sid = spManager.getOne("keySid");
//	private static String token = spManager.getOne("keyToken");

    public static final int DOWNLOAD_SUCCESSFUL = 1111;
    public static final int DOWNLOAD_FAIL = 2;
    public static final int INSERT_SUCCESSFUL = 3;
    public static final int INSERT_FAIL = 4;
    public static final int UPDATE_SUCCESSFUL = 5;
    public static final int UPDATE_FAIL = 6;

    public static final int DOWNLOAD_IMAGE_SUCCESSFUL = 11;
    public static final int DOWNLOAD_IMAGE_FAIL = 12;
    public static final int INSERT_IMAGE_SUCCESSFUL = 13;
    public static final int INSERT_IMAGE_FAIL = 14;
    public static final int UPDATE_IMAGE_SUCCESSFUL = 15;
    public static final int UPDATE_IMAGE_FAIL = 16;

    public static final int GET_QR_CODE_SUCCESSFUL = 17;
    public static final int GET_OR_CODE_FAIL = 18;

    public static final int INSERT_AUTH_PHOTO_SUCCESSFUL = 19;
    public static final int INSERT_AUTH_PHOTO_FAIL = 20;

    public static void download_image(final String url, final ImageView imageView) {
        DisplayImageOptions option = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.upload_btn)
                .showImageOnFail(R.drawable.upload_btn).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusRound)).build();
        ImageLoader.getInstance().displayImage(url, imageView, option);
    }

    public static void get_qrcode(final Handler handler) {
        Logger.d(TAG, "in get_qrcode");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(QR_CODE_URL + sid, params,
                new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d("get_qrcode", "in handler");
                        Logger.d("result", result);
                        Message msg = new Message();
                        msg.what = GET_QR_CODE_SUCCESSFUL;
                        msg.obj = result;
                        handler.sendMessage(msg);
                        return true;
                    }
                }, new HttpFailedCallBack() {
            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Message msg = new Message();
                msg.what = GET_OR_CODE_FAIL;
                msg.obj = errorMessage;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);

    }

    public static void download(final Handler handler) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("doctorId", sid));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(MINE_URL + "get", params,
                new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d(TAG, "in handle");
                        Logger.d(TAG, MINE_URL + "get");
                        String code = JsonUtil.getJsonStringByKey(result,
                                "code").toString();
                        Logger.d("result", result);
                        if (code.equals("200.0")) {
                            try {
                                String data = new JSONObject(result).getString("data");
                                Logger.d("data", data);
                                Message msg = new Message();
                                msg.what = DOWNLOAD_SUCCESSFUL;
                                msg.obj = data;
                                handler.sendMessage(msg);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Message msg = new Message();
                            msg.what = DOWNLOAD_FAIL;
                            msg.obj = result;
                            handler.sendMessage(msg);
                        }
                        return true;
                    }
                }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Logger.e(TAG, "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = DOWNLOAD_FAIL;
                msg.obj = errorCode;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }


    public static void insert_image(final Bitmap bitmap, final Handler handler) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            long len = stream.toByteArray().length;
            Logger.d("len = ", Long.toString(len));
            MessageManager manager = MessageManager.getMessageManager();
            if(Common.IsUploadToQiniu){
                String imgToken = spManager.getOne("keyQiniuTokenImg");
                UploadFileToQiniuMessage message = new UploadFileToQiniuMessage(null, new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d(TAG, "insert image successful");
                        List<String> qiniuKeys = new ArrayList<>();
                        qiniuKeys = (List<String>)JsonUtil.jsonToList(result);
                        HashMap<String, String> map = new HashMap<>();
                        for(String key: qiniuKeys){
                            map.put(Common.KEY_IMAGE_thumbnailPath, QiniuUtil.getThumbnailUrl(key, QiniuUtil.Type_Image, QiniuUtil.ThumbnailMode200x200));
                            map.put(Common.KEY_IMAGE_avaterPath, QiniuUtil.getDownloadUrl(key, QiniuUtil.Type_Image));
                            break;
                        }
                        String pathJson = JsonUtil.objectToJson(map);
                        Message msg = new Message();
                        msg.what = INSERT_IMAGE_SUCCESSFUL;
                        msg.obj = pathJson;
                        handler.sendMessage(msg);
                        return true;
                    }
                }, new HttpFailedCallBack() {
                    @Override
                    public boolean handle(int errorCode, String errorMessage) {
                        Logger.d(TAG, "<insert image failed> errorMessage: " + errorMessage);
                        return false;
                    }
                }, null, byteArray, imgToken);
                manager.sendMessage(message);
            }else{
                ImageMessage message = new ImageMessage(IMAGE_URL, byteArray,
                        new HttpCallBack() {
                            @Override
                            public boolean handle(String result) {
                                Logger.d(TAG, "insert image successful");
                                Message msg = new Message();
                                msg.what = INSERT_IMAGE_SUCCESSFUL;
                                msg.obj = result;
                                handler.sendMessage(msg);
                                return true;
                            }
                        }, new HttpFailedCallBack() {
                    @Override
                    public boolean handle(int errorCode, String errorMessage) {
                        Logger.d(TAG, "insert image failed");
                        return false;
                    }
                });
                manager.sendMessage(message);
            }
        }
    }

    public static void insert_auth_image(final Bitmap bitmap, final Handler handler){
        if (bitmap != null) {
            Logger.d(TAG, "insert_auth_image");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
            byte[] byteArray = stream.toByteArray();
            long len = stream.toByteArray().length;
            Logger.d("len = ", Long.toString(len));
            MessageManager manager = MessageManager.getMessageManager();
            if(Common.IsUploadToQiniu){
                String imgToken = spManager.getOne("keyQiniuTokenImg");
                UploadFileToQiniuMessage message = new UploadFileToQiniuMessage(null, new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d(TAG, "insert image successful");
                        List<String> qiniuKeys = new ArrayList<>();
                        qiniuKeys = (List<String>)JsonUtil.jsonToList(result);
                        HashMap<String, String> map = new HashMap<>();
                        for(String key: qiniuKeys){
                            map.put(Common.KEY_IMAGE_thumbnailPath, QiniuUtil.getThumbnailUrl(key, QiniuUtil.Type_Image, QiniuUtil.ThumbnailMode200x200));
                            map.put(Common.KEY_IMAGE_avaterPath, QiniuUtil.getDownloadUrl(key, QiniuUtil.Type_Image));
                            break;
                        }
                        String pathJson = JsonUtil.objectToJson(map);
                        Message msg = new Message();
                        msg.what = INSERT_AUTH_PHOTO_SUCCESSFUL;
                        msg.obj = pathJson;
                        handler.sendMessage(msg);
                        return true;
                    }
                }, new HttpFailedCallBack() {
                    @Override
                    public boolean handle(int errorCode, String errorMessage) {
                        Logger.d(TAG, "<insert image failed> errorMessage: " + errorMessage);
                        return false;
                    }
                }, null, byteArray, imgToken);
                manager.sendMessage(message);
            } else {
                ImageMessage message = new ImageMessage(IMAGE_URL, byteArray,
                        new SimpleMessage.HttpCallBack() {
                            @Override
                            public boolean handle(String result) {
                                Logger.d("result = ", result);
                                Logger.d(TAG, "insert image successful");
                                Message msg = new Message();
                                msg.what = INSERT_AUTH_PHOTO_SUCCESSFUL;
                                msg.obj = result;
                                handler.sendMessage(msg);
                                return true;
                            }
                        }, new SimpleMessage.HttpFailedCallBack() {
                    @Override
                    public boolean handle(int errorCode, String errorMessage) {
                        Logger.d(TAG, "insert image failed");
                        return false;
                    }
                });
                manager.sendMessage(message);
            }
        }
    }


    public static void insert(final DoctorInfo dInfo, final Handler handler) {
        Logger.d(TAG, "insert");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));

        params.add(new BasicNameValuePair("thumbnail", dInfo.getThumbnail()));
        params.add(new BasicNameValuePair("avatar", dInfo.getAvatar()));
        params.add(new BasicNameValuePair("name", dInfo.getName()));
        params.add(new BasicNameValuePair("birthday", Long.toString(dInfo.getBirth())));
        params.add(new BasicNameValuePair("gender", dInfo.getGender()));
        params.add(new BasicNameValuePair("email", dInfo.getEmail()));
        params.add(new BasicNameValuePair("addr", dInfo.getAddr()));
        params.add(new BasicNameValuePair("selfInfo", dInfo.getIntro()));
        params.add(new BasicNameValuePair("cid", dInfo.getCid()));
        params.add(new BasicNameValuePair("clinic", dInfo.getClinic()));
        params.add(new BasicNameValuePair("room", dInfo.getRoom()));
        params.add(new BasicNameValuePair("rank", dInfo.getRank()));
        params.add(new BasicNameValuePair("qrcode", dInfo.getQrcode()));
        params.add(new BasicNameValuePair("authPhoto", dInfo.getAuth_photo()));
        //params.add(new BasicNameValuePair("auth_status", Integer.toString(dInfo.getAuth_status())));
        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(MINE_URL + "add", params,
                new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d("insert", "in handle");
                        Logger.d(TAG, MINE_URL + "add");
                        Logger.d("result", result);

                        try {
                            JSONObject jo = new JSONObject(result);
                            int code = jo.getInt("code");
                            long data = jo.getLong("data");
                            Message msg = new Message();
                            if (code == 200) {
                                msg.what = INSERT_SUCCESSFUL;
                                msg.obj = data;
                            } else {
                                msg.what = INSERT_FAIL;
                                msg.obj = data;
                            }
                            handler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }, new HttpFailedCallBack() {
            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Logger.e(TAG, "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = INSERT_FAIL;
                msg.obj = errorCode;
                handler.sendMessage(msg);
                return true;
            }
        });
        manager.sendMessage(tm);
    }

    public static void update(final DoctorInfo dInfo, final DoctorInfo localDInfo,  final Handler handler) {
        Logger.d(TAG, "update");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));

        //DoctorInfo localDInfo = dInfoImpl.get(MinePageActivity.LOCAL_DOCTOR_TB_ITEM_ID);

        if(!(dInfo.getThumbnail().isEmpty() || dInfo.getThumbnail().equals(localDInfo.getThumbnail())))
            params.add(new BasicNameValuePair("thumbnail", dInfo.getThumbnail()));

        if(!(dInfo.getAvatar().isEmpty() || dInfo.getAvatar().equals(localDInfo.getAvatar())))
            params.add(new BasicNameValuePair("avatar", dInfo.getAvatar()));

        if(!(dInfo.getName().isEmpty() || dInfo.getName().equals(localDInfo.getName())))
            params.add(new BasicNameValuePair("name", dInfo.getName()));

        if(!(dInfo.getBirth().equals(localDInfo.getBirth()))) {
            Logger.d("dInfo = ", Long.toString(dInfo.getBirth()));
            Logger.d("localDInfo = ", Long.toString(localDInfo.getBirth()));
            params.add(new BasicNameValuePair("birthday", Long.toString(dInfo.getBirth())));
        }

        if(!(dInfo.getGender().isEmpty() || dInfo.getGender().equals(localDInfo.getGender())))
            params.add(new BasicNameValuePair("gender", dInfo.getGender()));

        if(!(dInfo.getEmail().isEmpty() || dInfo.getEmail().equals(localDInfo.getGender())))
            params.add(new BasicNameValuePair("email", dInfo.getEmail()));

        if(!(dInfo.getAddr().isEmpty() || dInfo.getAvatar().equals(localDInfo.getAddr())))
            params.add(new BasicNameValuePair("addr", dInfo.getAddr()));

        if(!(dInfo.getIntro().isEmpty() || dInfo.getIntro().equals(localDInfo.getIntro())))
            params.add(new BasicNameValuePair("selfInfo", dInfo.getIntro()));

        if(!(dInfo.getCid().isEmpty() || dInfo.getCid().equals(localDInfo.getCid())))
            params.add(new BasicNameValuePair("cid", dInfo.getCid()));

        if(!(dInfo.getClinic().isEmpty() || dInfo.getClinic().equals(localDInfo.getClinic())))
            params.add(new BasicNameValuePair("clinic", dInfo.getClinic()));

        if(!(dInfo.getRoom().isEmpty() || dInfo.getRoom().equals(localDInfo.getRoom())))
            params.add(new BasicNameValuePair("room", dInfo.getRoom()));

        if(!(dInfo.getRank().isEmpty() || dInfo.getRank().equals(localDInfo.getRank())))
            params.add(new BasicNameValuePair("rank", dInfo.getRank()));

        if(!(dInfo.getQrcode().isEmpty() || dInfo.getQrcode().equals(localDInfo.getQrcode())))
            params.add(new BasicNameValuePair("qrcode", dInfo.getQrcode()));

        if(!(dInfo.getAuth_photo().isEmpty() || dInfo.getAuth_photo().equals(localDInfo.getAuth_photo()))) {
            Logger.d("authPhoto", dInfo.getAuth_photo());
            params.add(new BasicNameValuePair("authPhoto", dInfo.getAuth_photo()));
        }

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(MINE_URL + "update", params,
                new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d("update", "in handle");
                        Logger.d(TAG, MINE_URL + "update");
                        Logger.d("result", result);
                        try {
                            JSONObject jo = new JSONObject(result);
                            int code = jo.getInt("code");
                            long data = jo.getLong("data");
                            Message msg = new Message();
                            if (code == 200) {
                                msg.what = UPDATE_SUCCESSFUL;
                                msg.obj = data;
                            } else {
                                msg.what = UPDATE_FAIL;
                                msg.obj = data;
                            }
                            handler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }, new HttpFailedCallBack() {
            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Logger.e(TAG, "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = UPDATE_FAIL;
                msg.obj = errorCode;
                handler.sendMessage(msg);
                return true;
            }
        });
        manager.sendMessage(tm);
    }


    public static void authStatusCheck(final Context context, final int oldStatus) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("doctorId", sid));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(MINE_URL + "get", params,
                new HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.d(TAG, "in handle");
                        Logger.d(TAG, MINE_URL + "get");
                        String code = JsonUtil.getJsonStringByKey(result,
                                "code").toString();
                        Logger.d("result", result);
                        if (code.equals("200.0")) {
                            try {
                                String data = new JSONObject(result).getString("data");
                                Logger.d("data", data);
                                PackDoctorInfo pdInfo = new Gson().fromJson(data, PackDoctorInfo.class);
                                int newStatus = pdInfo.getAuthStatus();
                                if(newStatus != oldStatus && newStatus == Common.AUTH_PASS){
                                    //认证状态变化,同时新的状态为认证通过
//                                    String newAuthStatusStr = MinePageActivity.authStatus2Str(context, newStatus);
//                                    String oldAuthStatusStr = MinePageActivity.authStatus2Str(context, oldStatus);
                                    NotifyManager.getManager().notifyAuth(pdInfo);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                        }
                        return true;
                    }
                }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Logger.e(TAG, "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                return false;
            }
        });
        manager.sendMessage(tm);
    }


}
