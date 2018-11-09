package com.onlinedoctor.net.mine;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.TextHttpsMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.SelfDefinedFee;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

public class SelfDefinedFeeNetManager {

    private static SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
    //	private static String sid = spManager.getOne("keySid");
//	private static String token = spManager.getOne("keyToken");
    private static final String TAG = "SDFNetManager";
    public static final String SelfDefinedFeeHttpsUrl = Common.URL_SELF_DEFINE_FEE;
    public final static int DOWNLOAD_SUCCESSFUL = 1;
    public final static int DOWNLOAD_FAIL = 2;
    public final static int DELETE_SUCCESSFUL = 3;
    public final static int DELETE_FAIL = 4;
    public final static int UPDATE_SUCCESSFUL = 5;
    public final static int UPDATE_FAIL = 6;
    public final static int INSERT_SUCCESSFUL = 7;
    public final static int INSERT_FAIL = 8;

    public final static int REQUEST_SUCCESSFUL = 9;
    public final static int REQUEST_FAIL = 10;

    public static void download(final Handler handler) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(Common.SelfDefinedFeeUrl + "getAll",
                params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d(TAG, "in handle");
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Message msg = new Message();
                if (code.equals("200.0")) {
                    msg.what = DOWNLOAD_SUCCESSFUL;
                    msg.obj = result;
                    handler.sendMessage(msg);

                }

                return false;
            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.obj = errorCode;
                msg.what = DOWNLOAD_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

    public static void delete(final SelfDefinedFee sdf, final Handler handler) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));
        Log.d("delete global_id = ", Long.toString(sdf.getGlobal_id()));
        params.add(new BasicNameValuePair("globalId", Long.toString(sdf
                .getGlobal_id())));
        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(Common.SelfDefinedFeeUrl + "delete",
                params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d(TAG, "in handle");
                Log.d(TAG, Common.SelfDefinedFeeUrl + "delete");
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Log.d("code", code);
                Log.d("result", result);
                Message msg = new Message();
                if (code.equals("200.0")) {
                    String data = JsonUtil.getJsonStringByKey(result,
                            "data").toString();
                    msg.obj = data;
                    msg.what = DELETE_SUCCESSFUL;
                    handler.sendMessage(msg);
                }
                return true;
            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.e("TAG", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = DELETE_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

    public static void update(final SelfDefinedFee sdf, final Handler handler) {
        Log.d(TAG, "update");
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("globalId", Long.toString(sdf.getGlobal_id())));
        params.add(new BasicNameValuePair("title", sdf.getTitle()));
        params.add(new BasicNameValuePair("description", sdf.getDescription()));

        //Fee在客户端保存的单位是［元］，但在服务器保存的是［分］，因此需要做相应的转化
        float feeFloat = Float.valueOf(sdf.getFee());
        int feeInt = (int)(feeFloat * 100);
        params.add(new BasicNameValuePair("fee", Integer.toString(feeInt)));

        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(Common.SelfDefinedFeeUrl + "update", params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d(TAG, "in handle");
                Log.d(TAG, Common.SelfDefinedFeeUrl + "update");
                String code = JsonUtil.getJsonStringByKey(result, "code").toString();
                Log.d("result", result);

                if (code.equals("200.0")) {
                    try {
                        JSONObject jo = new JSONObject(result);
                        String data = jo.getJSONObject("data").toString();
                        Log.d("data", data);
                        long globalId = new JSONObject(data).getLong("globalId");
                        //long update_time = ((Long)JsonUtil.getJsonStringByKey(data,"time")).longValue();
                        long update_time = new JSONObject(data).getLong("time");
                        Message msg = new Message();
                        msg.what = UPDATE_SUCCESSFUL;
                        Bundle bundle = new Bundle();
                        bundle.putLong("globalId", globalId);
                        bundle.putLong("update_time", update_time);
                        msg.obj = bundle;
                        handler.sendMessage(msg);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

//					sdf.setUpdate_time(update_time);
//					Log.d("time", Long.toString(update_time));
//					sdfImpl.update(sdf);
                }
                return true;
            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.e("TAG", "ErrorCode=" + errorCode + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = UPDATE_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }


    public static void insert(final SelfDefinedFee sdf, final Handler handler) {
        Log.d(TAG, "insert");
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("title", sdf.getTitle()));
        params.add(new BasicNameValuePair("description", sdf.getDescription()));

        //Fee在客户端保存的单位是［元］，但在服务器保存的是［分］，因此需要做相应的转化
        float feeFloat = Float.valueOf(sdf.getFee());
        int feeInt = (int)(feeFloat * 100);
        params.add(new BasicNameValuePair("fee", Integer.toString(feeInt)));

        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(Common.SelfDefinedFeeUrl + "insert", params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d(TAG, "in handle");
                Log.d(TAG, Common.SelfDefinedFeeUrl + "insert");
                String code = JsonUtil.getJsonStringByKey(result, "code").toString();
                Log.d("result", result);
                if (code.equals("200.0")) {
                    String data = JsonUtil.getJsonStringByKey(result, "data").toString();
                    Log.d("data", data);
                    long globalId = ((Double) JsonUtil.getJsonStringByKey(data, "globalId")).longValue();
                    Log.d("globalId", Long.toString(globalId));
                    long time = ((Double) JsonUtil.getJsonStringByKey(data, "time")).longValue();
                    Log.d("update_time", Long.toString(time));


                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putLong("globalId", globalId);
                    bundle.putLong("update_time", time);
                    bundle.putInt("id", sdf.getId());
                    msg.obj = bundle;
                    msg.what = INSERT_SUCCESSFUL;
                    handler.sendMessage(msg);
//					sdf.setGlobal_id(globalId);
//					sdf.setUpdate_time(time);
//					sdfImpl.update(sdf);
                }
                return true;

            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.e("TAG", "ErrorCode=" + errorCode + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = INSERT_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

    public static void request(final Handler chatHandler, final Handler sdfHandler, final SelfDefinedFee sdf, final String patientId) {
        Log.d(TAG, "request");
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("title", sdf.getTitle()));
        params.add(new BasicNameValuePair("description", sdf.getDescription()));
        params.add(new BasicNameValuePair("id", Long.toString(sdf.getGlobal_id())));
        //Fee在客户端保存的单位是［元］，但在服务器保存的是［分］，因此需要做相应的转化
        float feeFloat = Float.valueOf(sdf.getFee());
        int feeInt = (int)(feeFloat * 100);
        params.add(new BasicNameValuePair("fee", Integer.toString(feeInt)));

        params.add(new BasicNameValuePair("patientId", patientId));
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));
        Log.d("patientId = ", patientId);
        Log.d("sid = ", sid);
        Log.d("token = ", token);
        MessageManager manager = MessageManager.getMessageManager();
        TextHttpsMessage tm = new TextHttpsMessage(SelfDefinedFeeHttpsUrl + "request", params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d(TAG, SelfDefinedFeeHttpsUrl + "request");
                try {
                    Log.d("result = ", result);
                    JSONObject jo = new JSONObject(result);
                    int code = jo.getInt("code");
                    Message msg = new Message();
                    if (code == 200) {
                        msg.obj = sdf;
                        msg.what = Common.MSG_WHAT_MINE_CHARGING;

                    } else {
                        msg.what = REQUEST_FAIL;
                    }
                    chatHandler.sendMessage(msg);

                    Message msg2 = new Message();
                    msg2.copyFrom(msg);
                    sdfHandler.sendMessage(msg2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.e("TAG", "ErrorCode=" + errorCode + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = REQUEST_FAIL;
                chatHandler.sendMessage(msg);

                Message msg2 = new Message();
                msg2.copyFrom(msg);
                sdfHandler.sendMessage(msg2);
                return false;
            }
        });
        manager.sendMessage(tm);
    }
}
