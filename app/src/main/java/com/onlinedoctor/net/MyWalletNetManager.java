package com.onlinedoctor.net;

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
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.SharedpreferenceManager;

public class MyWalletNetManager {
    public final static int _msg_get_money = 0;
    public final static int _msg_get_withdraw_type = 1;
    public final static int _msg_update_mobile = 2;
    public final static int _msg_update_alipay = 3;
    public final static int _msg_update_bank = 4;
    public final static int _msg_get_withdraw_list = 5;
    public final static int _msg_withdraw = 6;
    public final static int _msg_get_order_list = 7;

    public final static int _msg_fail = -1;
    public final static int _msg_unknown_exception = -2;
    public final static int _msg_money_not_enough = -3;
    public final static int _msg_invaild_bankcard = -4;
    public final static int _msg_invaild_cellphone = -5;
    public final static int _msg_invaild_alipay = -6;

    private final static String url_get_money = Common.URL_GET_MONEY;
    private final static String url_get_withdraw_type = Common.URL_GET_WITHDRAW_TYPE;
    private final static String url_update_mobile = Common.URL_UPDATE_MOBILE;
    private final static String url_update_alipay = Common.URL_UPDATE_ALIPAY;
    private final static String url_update_bank = Common.URL_UPDATE_BANK;
    private final static String url_get_withdraw_list = Common.URL_GET_WITHDRAW_LIST;
    private final static String url_withdraw = Common.URL_WITHDRAW_REQUEST;
    private final static String url_get_order_list = Common.URL_GET_ORDER_LIST;

    public static void getMoney(final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        Logger.d("token = ", token);
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_get_money, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_get_money : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONObject("data").toString();
                        Logger.d("data = ", msg.obj.toString());
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
                    Logger.d("data = ", msg.obj.toString());
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
            Logger.d("data = ", msg.obj.toString());
            mhandler.sendMessage(msg);
        }
    }


    public static void getWithdrawType(final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_get_withdraw_type, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_get_withdraw_type : _msg_fail;
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


    public static void updateMobile(String mobile, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("cellphone", mobile));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_update_mobile, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_update_mobile : _msg_invaild_cellphone;
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

    public static void updateAlipay(String alipay, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("alipay", alipay));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_update_alipay, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_update_alipay : _msg_invaild_alipay;
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

    public static void updateBank(String bankCard, String bankName, String bankCardOwner, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("bankcard", bankCard));
        param.add(new BasicNameValuePair("bankname", bankName));
        param.add(new BasicNameValuePair("bankcardOwner", bankCardOwner));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_update_bank, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        msg.what = (new JSONObject(result).getInt("code") == 200) ? _msg_update_bank : _msg_invaild_bankcard;
                        msg.obj = new JSONObject(result).getJSONObject("data").toString();
                        Logger.d("data", (String) msg.obj);
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

    public static void getWithdrawList(int pageSize, int page, long startTime, long endTime, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("page", page + ""));
        param.add(new BasicNameValuePair("pageSize", pageSize + ""));
        param.add(new BasicNameValuePair("startTime", startTime + ""));
        param.add(new BasicNameValuePair("endTime", endTime + ""));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_get_withdraw_list, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        Log.d("withdrawList", result);
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_get_withdraw_list : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONArray("data").toString();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        msg.obj = "[]";
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

    public static void withdraw(String amount, String guid, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        int amountInt = (int)(((float)Float.valueOf(amount))*100);
        param.add(new BasicNameValuePair("amount", Integer.toString(amountInt)));
        param.add(new BasicNameValuePair("guid", guid));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_withdraw, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_withdraw : new JSONObject(result).getInt("code") == -10037 ? _msg_money_not_enough : _msg_fail;
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


    public static void getOrderList(int pageSize, int page, long startTime, long endTime, final Handler mhandler) {
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String sid = spManager.getOne("keySid");
        String token = spManager.getOne("keyToken");
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("sid", sid));
        param.add(new BasicNameValuePair("token", token));
        param.add(new BasicNameValuePair("page", page + ""));
        param.add(new BasicNameValuePair("pageSize", pageSize + ""));
        param.add(new BasicNameValuePair("startTime", startTime + ""));
        param.add(new BasicNameValuePair("endTime", endTime + ""));
        try {
            NetworkManager.getNetworkManager().send(new TextMessage(url_get_order_list, param, new HttpCallBack() {

                @Override
                public boolean handle(String result) {
                    Log.d("orderList", result);
                    Message msg = new Message();
                    try {
                        msg.what = new JSONObject(result).getInt("code") == 200 ? _msg_get_order_list : _msg_fail;
                        msg.obj = new JSONObject(result).getJSONArray("data").toString();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        msg.obj = "[]";
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


}
