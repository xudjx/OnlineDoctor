package com.onlinedoctor.net.mine;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.TextHttpsMessage;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wds on 15/10/31.
 */
public class ModifyPasswordNetManager{
    private static final String URL = Common.HTTPS_SERVER;
    public static final int UPDATE_SUCCESSFUL = 1;
    public static final int UPDATE_FAIL = 2;
    public static void update(final String oldPassword, final String newPassword, final Handler handler){
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String cellPhone = spManager.getOne("keyPhone");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cellphone", cellPhone));
        params.add(new BasicNameValuePair("oldPassword", oldPassword));
        params.add(new BasicNameValuePair("newPassword", newPassword));

        MessageManager manager = MessageManager.getMessageManager();
        TextHttpsMessage tm = new TextHttpsMessage(URL + "chgPassword",
                params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d("update", "in handle");
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Message msg = new Message();
                if (code.equals("200.0")) {
                    msg.what = UPDATE_SUCCESSFUL;
                    msg.obj = result;
                    handler.sendMessage(msg);

                }else{
                    msg.what = UPDATE_FAIL;
                    msg.obj = result;
                    handler.sendMessage(msg);
                }
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.obj = errorCode;
                msg.what = UPDATE_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

    public static void update(final String cellphone, final String verifyCode, final String newPassword, final Handler handler){

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cellphone", cellphone));
        params.add(new BasicNameValuePair("verifyCode", verifyCode));
        params.add(new BasicNameValuePair("newPassword", newPassword));

        MessageManager manager = MessageManager.getMessageManager();
        TextHttpsMessage tm = new TextHttpsMessage(URL + "chgPassword",
                params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                Log.d("update", "in handle");
                String code = JsonUtil.getJsonStringByKey(result,
                        "code").toString();
                Message msg = new Message();
                if (code.equals("200.0")) {
                    msg.what = UPDATE_SUCCESSFUL;
                    msg.obj = result;
                    handler.sendMessage(msg);

                }else{
                    msg.what = UPDATE_FAIL;
                    msg.obj = result;
                    handler.sendMessage(msg);
                }
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {

            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Log.d("OnSuccess", "ErrorCode=" + errorCode
                        + ", ErrorMessage=" + errorMessage + "]");
                Message msg = new Message();
                msg.obj = errorCode;
                msg.what = UPDATE_FAIL;
                handler.sendMessage(msg);
                return false;
            }
        });
        manager.sendMessage(tm);
    }

}
