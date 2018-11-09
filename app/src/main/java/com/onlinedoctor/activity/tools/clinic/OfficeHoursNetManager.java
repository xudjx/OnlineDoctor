package com.onlinedoctor.activity.tools.clinic;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

public class OfficeHoursNetManager {
    private static final String URL = Common.ClinicPlanUrl;
    public static final int GET_SUCCESSFUL = 31;
    public static final int INSERT_SUCCESSFUL = 32;
    public static final int UPDATE_SUCCESSFUL = 33;
    public static final int UPDATE_FAIL = 34;
    public static final int GET_FAIL = 35;

    private static SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
//    private static String sid = spManager.getOne("keySid");
//    private static String token = spManager.getOne("keyToken");
    private static final String TAG = "OfficeHoursNetManager";

    public static void get(final Handler handler) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Logger.d(TAG,"1");
        String sid = spManager.getOne("keySid");
        Logger.d(TAG,"1");
        params.add(new BasicNameValuePair("doctorId", sid));
        Logger.d(TAG, "1");
        MessageManager manager = MessageManager.getMessageManager();
        Logger.d(TAG,"1");
        TextMessage tm = new TextMessage(URL + "get",
                params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Logger.d("result = ", result);
                Log.d("get", "in handle");
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Message msg = new Message();
                if (code.equals("200.0")) {
                    Logger.d("code = ", "200");
                    msg.what = GET_SUCCESSFUL;
                    msg.obj = result;
                    handler.sendMessage(msg);
                    return true;
                }else{
                    msg.what = GET_FAIL;
                    msg.obj = result;
                    handler.sendMessage(msg);
                    return false;
                }

            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = GET_FAIL;
                msg.obj = errorMessage;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

//	public static void insert(final String clinic_plan, final Handler handler){
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("sid", sid));
//		params.add(new BasicNameValuePair("token", token));
//		params.add(new BasicNameValuePair("clinic_plan", clinic_plan));
//		
//		MessageManager manager = MessageManager.getMessageManager();
//		TextMessage tm = new TextMessage(URL + "insert",
//				params, new HttpCallBack() {
//					@Override
//					public boolean handle(String result) {
//						Log.d(TAG, "in handle");
//						String code = JsonUtil.getJsonStringByKey(result,
//								"code").toString();
//						Message msg = new Message();
//						if (code.equals("200.0")) {
//							msg.what = INSERT_SUCCESSFUL;
//							msg.obj = result;
//							handler.sendMessage(msg);
//						}
//
//						return false;
//					}
//				}, new HttpFailedCallBack() {
//
//					@Override
//					public boolean handle(int errorCode, String errorMessage) {
//						Log.d("OnSuccess", "ErrorCode=" + errorCode
//								+ ", ErrorMessage=" + errorMessage + "]");
//						return false;
//					}
//				});
//		manager.sendMessage(tm);
//	}

    public static void update(final String clinic_plan, final Handler handler) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("clinicInfo", clinic_plan));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(URL + "update",
                params, new HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d("update", "in handle");
//						String code = JsonUtil.getJsonStringByKey(result,
//								"code").toString();
                JSONObject jo;
                try {
                    jo = new JSONObject(result);
                    int code = jo.getInt("code");
                    Message msg = new Message();
                    if (code == 200) {
                        Log.d("code", "200");
                        msg.what = UPDATE_SUCCESSFUL;
                        msg.obj = jo.getLong("data");
                        handler.sendMessage(msg);
                    } else {
                        Log.d("data", jo.getString("data"));
//								msg.what = UPDATE_FAIL;
//								msg.obj = result;
//								handler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }, new HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("update fail", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                return false;
            }
        });
        manager.sendMessage(tm);
    }
}
