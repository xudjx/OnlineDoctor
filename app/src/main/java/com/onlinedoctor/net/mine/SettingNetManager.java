package com.onlinedoctor.net.mine;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wds on 15/8/4.
 */
public class SettingNetManager {
    public static final int CHG_PASSWD_SUCCESSFUL = 1;
    public static final int CHG_PASSWD_FAIL = 2;
    public static final int LOGOUT_SUCCESSFUL = 3;
    public static final int LOGOUT_FAIL = 4;
    public static final int _msg_version = 5;
    public static final int _msg_check_version = 6;
    private static final String URL = Common.Url;
    private static final String versionUrl = Common.URL_VERSION;
    private static SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
    //    private static String sid = spManager.getOne("keySid");
//    private static String token = spManager.getOne("keyToken");
    private static final String TAG = "SettingNetManager";

    public static void changePasswd(final String newPassword, final Handler handler) {
        String cellphone = spManager.getOne("keyPhone");
        String oldPassword = spManager.getOne("keyPassword");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cellphone", cellphone));
        params.add(new BasicNameValuePair("oldPassword", oldPassword));
        params.add(new BasicNameValuePair("newPassword", newPassword));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(URL + "chgPassword",
                params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d("chgPassword", "in handle");
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Message msg = new Message();
                if (code.equals("200.0")) {
                    msg.what = CHG_PASSWD_SUCCESSFUL;
                    //msg.obj = result;
                } else {
                    msg.what = CHG_PASSWD_FAIL;
                }
                handler.sendMessage(msg);
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = CHG_PASSWD_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

    public static void logout(final Handler handler) {
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("sid", sid));
        params.add(new BasicNameValuePair("token", token));

        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(URL + "logout",
                params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d("logout", "in handle");
                Log.d("logout", URL + "logout");
                Log.d("result", result);
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Message msg = new Message();
                if (code.equals("200.0")) {
                    msg.what = LOGOUT_SUCCESSFUL;
                } else {
                    msg.what = LOGOUT_FAIL;
                }
                handler.sendMessage(msg);
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.what = LOGOUT_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }


    public static void getVersion(final Handler handler) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(versionUrl,
                params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Message msg = new Message();
                msg.what = _msg_version;
                msg.obj = result;
                handler.sendMessage(msg);
                spManager.setOne("keyVersion", result);
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                return false;
            }
        });
        manager.sendMessage(tm);
    }


    public static void checkVersion(final Handler handler) {
        if (!spManager.getOne("keyVersion").equals("")) {
            Message msg = new Message();
            msg.what = _msg_check_version;
            msg.obj = spManager.getOne("keyVersion");
            handler.sendMessage(msg);
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        MessageManager manager = MessageManager.getMessageManager();
        TextMessage tm = new TextMessage(versionUrl,
                params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                //Message msg = new Message();
                //msg.what = _msg_check_version;
                //msg.obj = result;
                // handler.sendMessage(msg);
                spManager.setOne("keyVersion", result);
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                return false;
            }
        });
        manager.sendMessage(tm);
    }

}
