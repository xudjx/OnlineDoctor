package com.onlinedoctor.net.tools;

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
import com.onlinedoctor.net.NetworkManager;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.SharedpreferenceManager;

public class SurveyNetManager {
    private final static String InsertQuestionnaireUrl = Common.URL_INSERT_QUESTIONNAIRE;
    private final static String UpdateQuestionnaireUrl = Common.URL_UPDATE_QUESTIONNAIRE;
    private final static String DeleteQuestionnaireUrl = Common.URL_DELETE_QUESTIONNAIRE;
    private final static String GetQuestionnaireUrl = Common.URL_GET_QUESTIONNAIRE;

    public final static int _msg_insert_questionnaire = 1;
    public final static int _msg_delete_questionnaire = 2;
    public final static int _msg_update_questionnaire = 3;
    public final static int _msg_get_questionnaire = 4;
    public final static int _msg_fail = -1;
    public final static int _msg_unknown_exception = -2;

    public static void insert(final long localId, String name, String questionJson, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("name", name));
        param.add(new BasicNameValuePair("questionJson", questionJson));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(InsertQuestionnaireUrl, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Log.d("synSurvey", result);
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_insert_questionnaire : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONObject("data").toString();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    msg.arg1 = (int) localId;
                    mhandler.sendMessage(msg);
                    return false;
                }
            }, new HttpFailedCallBack() {

                @Override
                public boolean handle(int errorCode, String errorMessage) {
                    // TODO Auto-generated method stub
                    Message msg = new Message();
                    msg.what = _msg_fail;
                    msg.obj = errorCode;
                    mhandler.sendMessage(msg);
                    return false;
                }
            }));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Message msg = new Message();
            msg.what = _msg_unknown_exception;
            msg.obj = null;
            mhandler.sendMessage(msg);
        }

    }

    public static void delete(long globalId, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("globalId", globalId + ""));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(DeleteQuestionnaireUrl, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    // TODO Auto-generated method stub
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_delete_questionnaire : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONObject("data").toString();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mhandler.sendMessage(msg);
                    return false;
                }
            }, new HttpFailedCallBack() {

                @Override
                public boolean handle(int errorCode, String errorMessage) {
                    // TODO Auto-generated method stub
                    Message msg = new Message();
                    msg.what = _msg_fail;
                    msg.obj = errorCode;
                    mhandler.sendMessage(msg);
                    return false;
                }
            }));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Message msg = new Message();
            msg.what = _msg_unknown_exception;
            msg.obj = null;
            mhandler.sendMessage(msg);
        }

    }

    public static void update(final int localId, String title, String questionJson, long globalId, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("globalId", globalId + ""));
        param.add(new BasicNameValuePair("name", title));
        param.add(new BasicNameValuePair("questionJson", questionJson));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(UpdateQuestionnaireUrl, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    // TODO Auto-generated method stub
                    Message msg = new Message();
                    msg.arg1 = localId;
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_update_questionnaire : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONObject("data").toString();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mhandler.sendMessage(msg);
                    return false;
                }
            }, new HttpFailedCallBack() {

                @Override
                public boolean handle(int errorCode, String errorMessage) {
                    // TODO Auto-generated method stub
                    Message msg = new Message();
                    msg.what = _msg_fail;
                    msg.obj = errorCode;
                    mhandler.sendMessage(msg);
                    return false;
                }
            }));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Message msg = new Message();
            msg.what = _msg_unknown_exception;
            msg.obj = null;
            mhandler.sendMessage(msg);
        }
    }

    public static void get(long time, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("time", time + ""));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(GetQuestionnaireUrl, param, new HttpCallBack() {
                @Override
                public boolean handle(String result) {
                    Logger.d("result", result);
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_get_questionnaire : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONObject("data").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mhandler.sendMessage(msg);
                    return false;
                }
            }, new HttpFailedCallBack() {
                @Override
                public boolean handle(int errorCode, String errorMessage) {
                    Logger.d("errorMessage", errorMessage);
                    Message msg = new Message();
                    msg.what = _msg_fail;
                    msg.obj = errorCode;
                    mhandler.sendMessage(msg);
                    return false;
                }
            }));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Message msg = new Message();
            msg.what = _msg_unknown_exception;
            msg.obj = null;
            mhandler.sendMessage(msg);
        }
    }


}
