package com.onlinedoctor.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.NetworkProber;
import com.onlinedoctor.net.VersionUpdateManager;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.net.mine.MineNetManager;
import com.onlinedoctor.activity.mine.homepage.MinePageActivity;
import com.onlinedoctor.net.mine.SettingNetManager;
import com.onlinedoctor.activity.tools.clinic.OfficeHoursActivity;
import com.onlinedoctor.activity.tools.clinic.OfficeHoursNetManager;
import com.onlinedoctor.net.tools.SurveyNetManager;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.pojo.mine.PackDoctorInfo;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.pojo.tools.survey.Questionnaire;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.QuestionnaireImpl;
import com.onlinedoctor.util.BadgeViewUtil;
import com.onlinedoctor.face.FaceConversionUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.util.UpdateManager;
import com.onlinedoctor.view.BadgeView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class MainActivity extends ActivityGroup {

    private static final String TAG = "MainActivity";
    private Context mContext = this;
    private int curretActivity = 1;
    private ImageView messageButton, contactButton, mineButton, toolButton;
    private RelativeLayout msgLayout, contactLayout, mineLayout, toolLayout, msgBigLayout, contactBigLayout, mineBigLayout, toolBigLayout;
    private LinearLayout containerLayout;

    // BadgeView for Notify
    private BadgeView messageBadge;
    private BadgeView contactBadge;

    private BadgeView mineBadge;

    private RunDataContainer container = RunDataContainer.getContainer();
    private SharedpreferenceManager preManager = SharedpreferenceManager.getInstance();
    PopupWindow popupWindow;
    UpdateManager updateManager;
    private final static int _msg_show_window = 1;

    QuestionnaireImpl dsImpl = new QuestionnaireImpl(this, null);

    private Handler mainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Common.MSG_WHAT_BRIEFCHANGE_MESSAGE:
                    refreshView();
                    break;
                case Common.MSG_WHAT_PATIENTCOUNTCHANGE_MESSAGE:
                    refreshPatientView();
                    break;
                case _msg_show_window:
                    popupWindow.showAtLocation(MainActivity.this.getWindow().getDecorView().findViewById(android.R.id.content).getRootView(), Gravity.CENTER, 0, 0);

                case SurveyNetManager._msg_get_questionnaire:
                    try {
                        JSONObject jo = new JSONObject(msg.obj.toString());
                        int isNew = jo.getInt("isNew");
                        if (isNew == 0) {
                            JSONArray ja = jo.getJSONArray("synObject");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject tmp = ja.getJSONObject(i);
                                if (tmp.getInt("isDelete") == 1)
                                    dsImpl.deleteQuestionnaireByGlobalId(tmp.getInt("id"));
                                else {
                                    Questionnaire qa = Questionnaire.fromJO(tmp);
                                    qa.localId = -1;
                                    dsImpl.updateQuestionnaire(qa);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(mContext, "无法解析服务器数据", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case MineNetManager.DOWNLOAD_SUCCESSFUL: {
                    Logger.d(TAG, "download successful");
                    String data = msg.obj.toString();
                    Logger.d("data", data);
                    PackDoctorInfo pdInfo = new Gson().fromJson(data, PackDoctorInfo.class);

                    //将从服务器获得信息保存到本地DB
                    DoctorInfo serverDoInfo = new DoctorInfo();
                    serverDoInfo.setTime(pdInfo.getLastTime());
                    serverDoInfo.setName(pdInfo.getName());

                    Logger.d("~~~come here , name = ", serverDoInfo.getName());
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

                    DoctorInfoServiceImpl dInfoImpl = new DoctorInfoServiceImpl(mContext, null);
                    if (dInfoImpl.isEmpty()) {
                        Logger.d("本地表", "为空");
                        int id = dInfoImpl.insert(serverDoInfo);
                        serverDoInfo.setId(id);
                    } else {
                        Logger.d("本地表", "不为空");
                        serverDoInfo.setId(MinePageActivity.LOCAL_DOCTOR_TB_ITEM_ID);
                        dInfoImpl.update(serverDoInfo);
                    }
                    break;
                }

                case OfficeHoursNetManager.GET_SUCCESSFUL: {
                    String result = msg.obj.toString();
                    try {
                        JSONObject rootNode = new JSONObject(result);
                        JSONObject jo = null;
                        if (rootNode.getJSONObject("data") != null) {
                            jo = rootNode.getJSONObject("data");
                        } else {
                            break;
                        }
                        String data = jo.toString();
                        Logger.d("data", data);
                        if (!data.equals("null")) {
                            long time = jo.getLong("time");
                            Logger.d("time", Long.toString(time));
                            String clinic_info = jo.getString("clinicInfo");
                            Logger.d("clinic_info", clinic_info);
                            SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
                            spManager.setOne(OfficeHoursActivity.KEY_CLINIC_JSON, clinic_info);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case Common.MSG_WHAT_PAYMENT_SUCCESSFUL: {
                    Logger.d("in MainHandler case: ", "MSG_WHAT_PAYMENT_SUCCESSFUL");
                    int count;
                    String unReadPayItemNumStr = preManager.getOne("keyUnReadPayItemNum");
                    if (unReadPayItemNumStr.equals("")) {
                        count = 0;
                    } else {
                        count = Integer.parseInt(unReadPayItemNumStr);
                    }

                    count = count + 1;
                    preManager.setOne("keyUnReadPayItemNum", Integer.toString(count));
                    Logger.d("keyUnReadPayItemNum = ", count + "");
                    BadgeViewUtil.commonSetBadgeView(mineBadge, count);
                    break;
                }
                default:
                    break;
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        MyApplication.getInstance().addActivity(this);

        // 获取表情数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                FaceConversionUtil.getInstace().getFileText(getApplication());
            }
        }).start();

        HandlerManager.getManager().setMainHandler(mainHandler);
        containerLayout = (LinearLayout) findViewById(R.id.content);
        messageButton = (ImageView) findViewById(R.id.messageBtn);
        contactButton = (ImageView) findViewById(R.id.contactBtn);
        toolButton = (ImageView) findViewById(R.id.toolsBtn);
        mineButton = (ImageView) findViewById(R.id.mineBtn);

        msgLayout = (RelativeLayout) findViewById(R.id.message_layout);
        contactLayout = (RelativeLayout) findViewById(R.id.contact_layout);
        toolLayout = (RelativeLayout) findViewById(R.id.tools_layout);
        mineLayout = (RelativeLayout) findViewById(R.id.mine_layout);

        msgBigLayout = (RelativeLayout) findViewById(R.id.message_big_layout);
        contactBigLayout = (RelativeLayout) findViewById(R.id.contact_big_layout);
        toolBigLayout = (RelativeLayout) findViewById(R.id.tools_big_layout);
        mineBigLayout = (RelativeLayout) findViewById(R.id.mine_big_layout);

        containerLayout.removeAllViews();
        containerLayout.addView(getLocalActivityManager().startActivity("Module1",
                new Intent(MainActivity.this, MessageActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .getDecorView());
        showButton(curretActivity);
        View.OnClickListener clickListener = new View.OnClickListener() {

            public void onClick(View v) {
                switch (v.getId()) {
                    //case R.id.messageBtn:
                    case R.id.message_big_layout:
                        curretActivity = 1;
                        containerLayout.removeAllViews();
                        containerLayout.addView(getLocalActivityManager().startActivity(
                                "Module1",
                                new Intent(MainActivity.this, MessageActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView());
                        break;
                    //case R.id.contactBtn:
                    case R.id.contact_big_layout:
                        curretActivity = 2;
                        containerLayout.removeAllViews();
                        Intent intent = new Intent(MainActivity.this, PatientActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("mode", "list");
                        intent.putExtras(bundle);
                        containerLayout.addView(getLocalActivityManager().startActivity(
                                "Module2", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView());
                        break;
                    //case R.id.toolsBtn:
                    case R.id.tools_big_layout:
                        curretActivity = 3;
                        containerLayout.removeAllViews();
                        containerLayout.addView(getLocalActivityManager().startActivity("Module3",
                                new Intent(MainActivity.this, ToolActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                .getDecorView());
                        break;
                    //case R.id.mineBtn:
                    case R.id.mine_big_layout:
                        curretActivity = 4;
                        containerLayout.removeAllViews();
                        containerLayout.addView(getLocalActivityManager().startActivity("Module4",
                                new Intent(MainActivity.this, MineActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                .getDecorView());
                        //mineBadge.hide();
                        break;
                    default:
                        break;
                }
                showButton(curretActivity);
            }
        };

        msgBigLayout.setOnClickListener(clickListener);
        contactBigLayout.setOnClickListener(clickListener);
        toolBigLayout.setOnClickListener(clickListener);
        mineBigLayout.setOnClickListener(clickListener);
        initBadge();
        SettingNetManager.checkVersion(mainHandler);
        if (!preManager.getOne("keyVersion").equals("")) {
            try {
                JSONObject jo = new JSONObject(preManager.getOne("keyVersion"));
                String serverVersion = jo.getString("version");
                String localVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
                if (versionCompar(serverVersion, localVersion)) {
                    final String path = jo.getString("path");
                    String serverMsg = jo.getString("msg");
                    new AlertDialog.Builder(MainActivity.this).setTitle("升级提示").setMessage(serverMsg).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            MyApplication.getInstance().exit();
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "下载中...", Toast.LENGTH_SHORT);
                            if (updateManager == null)
                                updateManager = new UpdateManager(MainActivity.this);
                            updateManager.downloadUrl = path;
                            updateManager.showDownloadDialog();
                        }
                    }).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //同步病情调查
        syncDiseaseSurvey();

        //同步我的信息
        syncDoctorInfo();

        //同步门诊信息
        syncClinicPlan();

        //检测权威认证状态变化
        DoctorInfoServiceImpl docInfoImpl = new DoctorInfoServiceImpl(mContext, null);
        DoctorInfo docInfo = docInfoImpl.get(MinePageActivity.LOCAL_DOCTOR_TB_ITEM_ID);
        if (docInfo.getAuth_status() != Common.AUTH_PASS) {//权威认证还没有通过
            MineNetManager.authStatusCheck(mContext, docInfo.getAuth_status());
        }

        // 获取七牛云上传的token
        getQiniuToken();

        //（从七牛）检测版本更新情况
        if (NetworkProber.isWiFi(mContext)) {
            VersionUpdateManager updateManager = new VersionUpdateManager(mContext);
            updateManager.checkUpdate();
        }
    }

    private void showButton(int curretActivity) {
        switch (curretActivity) {
            case 1:
                messageButton.setImageResource(R.drawable.ic_tab_message_highlight);
                contactButton.setImageResource(R.drawable.ic_tab_contact_normal);
                toolButton.setImageResource(R.drawable.ic_tab_tool_normal);
                mineButton.setImageResource(R.drawable.ic_tab_my_normal);
                break;
            case 2:
                messageButton.setImageResource(R.drawable.ic_tab_message_normal);
                contactButton.setImageResource(R.drawable.ic_tab_contact_highlight);
                toolButton.setImageResource(R.drawable.ic_tab_tool_normal);
                mineButton.setImageResource(R.drawable.ic_tab_my_normal);
                break;
            case 3:
                messageButton.setImageResource(R.drawable.ic_tab_message_normal);
                contactButton.setImageResource(R.drawable.ic_tab_contact_normal);
                toolButton.setImageResource(R.drawable.ic_tab_tool_highlight);
                mineButton.setImageResource(R.drawable.ic_tab_my_normal);
                break;
            case 4:
                messageButton.setImageResource(R.drawable.ic_tab_message_normal);
                contactButton.setImageResource(R.drawable.ic_tab_contact_normal);
                toolButton.setImageResource(R.drawable.ic_tab_tool_normal);
                mineButton.setImageResource(R.drawable.ic_tab_my_highlight);
                break;
        }
    }

    /**
     * 初次显示，检索数据库，判断未读的消息 点击之后进行刷新
     */
    private void initBadge() {
        messageBadge = new BadgeView(MainActivity.this, msgLayout);
        contactBadge = new BadgeView(MainActivity.this, contactLayout);
        mineBadge = new BadgeView(MainActivity.this, mineLayout);

        int count = 0;
        List<BriefMessagePojo> list = container.getBriefMessage();
        for (int i = 0; i < list.size(); i++) {
            count += list.get(i).getCount();
        }
        messageBadge.setText(String.valueOf(count));
        messageBadge.setTextSize(12);
        messageBadge.setTextColor(Color.WHITE);
        if (count > 0 && count < 100) {
            messageBadge.show();
        } else if (count >= 100) {
            messageBadge.setText("99+");
            messageBadge.show();
        } else {
            messageBadge.hide();
        }

        count = container.getNewPatientNum();
        contactBadge.setTextColor(Color.WHITE);
        contactBadge.setTextSize(12);
        if (count == 0) {
            contactBadge.hide();
        } else {
            contactBadge.setText(String.valueOf(count));
            contactBadge.show();
        }
    }

    /**
     * 接收到通知消息进行刷新
     */
    private void refreshView() {
        int count = 0;
        List<BriefMessagePojo> list = container.getBriefMessage();
        for (int i = 0; i < list.size(); i++) {
            count += list.get(i).getCount();
        }
        messageBadge.setText(String.valueOf(count));
        messageBadge.setTextSize(12);
        messageBadge.setTextColor(Color.WHITE);
        if (count > 0 && count < 100) {
            messageBadge.show();
        } else if (count >= 100) {
            messageBadge.setText("99+");
            messageBadge.show();
        } else {
            messageBadge.hide();
        }
    }

    private void refreshPatientView() {
        int count = container.getNewPatientNum();
        if (count == 0) {
            contactBadge.hide();
        } else {
            contactBadge.setText(String.valueOf(count));
            contactBadge.show();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.i(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i(TAG, "onResume");
        refreshView();
        BadgeViewUtil.paymentSetBadgeView(mineBadge);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i(TAG, "onStop");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder alert = new Builder(MainActivity.this);
            alert.setMessage("确认退出吗");
            alert.setTitle("提示");
            alert.setPositiveButton("确定", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    MainActivity.this.finish();
                }
            });
            alert.setNegativeButton("取消", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.create().show();
        }
        return false;
    }

    static boolean versionCompar(String v1, String v2) {
        String[] v1s = v1.split("\\.");
        String[] v2s = v2.split("\\.");
        return Integer.parseInt(v1s[0]) > Integer.parseInt(v2s[0]) || (Integer.parseInt(v1s[0]) == Integer.parseInt(v2s[0]) && Integer.parseInt(v1s[1]) > Integer.parseInt(v2s[1]));
    }


    //同步病情调查信息
    void syncDiseaseSurvey() {
        long time = dsImpl.getLastUpdate();
        SurveyNetManager.get(time, mainHandler);
    }

    //同步我的个人信息
    void syncDoctorInfo() {
        MineNetManager.download(mainHandler);
    }

    //同步门诊信息
    void syncClinicPlan() {
        OfficeHoursNetManager.get(mainHandler);
    }

    private void getQiniuToken() {
        final MessageManager messageManager = MessageManager.getMessageManager();
        // 获取上传的token
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("sid", preManager.getOne("keySid")));
        params.add(new BasicNameValuePair("token", preManager.getOne("keyToken")));
        TextMessage textMessage = new TextMessage(Common.ImageHttpUrl, params, new SimpleMessage.HttpCallBack() {
            @Override
            public boolean handle(String result) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    int code = jsonResult.getInt("code");
                    Logger.i("QiniuTokenImg", jsonResult.toString());
                    if (code == 200) {
                        String token = jsonResult.getString("data");
                        Logger.i("QiniuTokenImg", token);
                        preManager.setOne("keyQiniuTokenImg", token);

                        TextMessage textMessage2 = new TextMessage(Common.VoiceHttpUrl, params, new SimpleMessage.HttpCallBack() {
                            @Override
                            public boolean handle(String result) {
                                try {
                                    JSONObject jsonResult = new JSONObject(result);
                                    int code = jsonResult.getInt("code");
                                    Logger.i("QiniuTokenVoice", jsonResult.toString());
                                    if (code == 200) {
                                        String token = jsonResult.getString("data");
                                        Logger.i("QiniuTokenVoice", token);
                                        preManager.setOne("keyQiniuTokenVoice", token);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        }, new SimpleMessage.HttpFailedCallBack() {
                            @Override
                            public boolean handle(int errorCode, String errorMessage) {
                                Logger.e("QiniuTokenVoice", "<MainActivity got QiniuToken voice failed>");
                                return false;
                            }
                        });
                        messageManager.sendMessage(textMessage2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }, new SimpleMessage.HttpFailedCallBack() {
            @Override
            public boolean handle(int errorCode, String errorMessage) {
                Logger.e("QiniuTokenImg", "<MainActivity got QiniuToken image failed>");
                preManager.setOne("keyQiniuTokenImg", "C3ylKmpJSjn6epM2M23lNgJqOoLl8HSEjZSnFavE:Of__xc60ry2P4nklCu_ZMWD72DE=:eyJzY29wZSI6InlpeGluZ3poZS1pbWciLCJkZWFkbGluZSI6MTQ1MTI5MDg3NH0=");
                return false;
            }
        });
        messageManager.sendMessage(textMessage);
    }
}
