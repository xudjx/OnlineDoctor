package com.onlinedoctor.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.TextMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.umeng.analytics.MobclickAgent;

import android.os.Handler;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.*;

/**
 * Created by Song on 2015/8/8.
 */


public class FillBasicInfoActivity extends Activity implements TextWatcher{

    private final static int _msg_success = 0;
    private final static int _msg_fail = -1;
    private static SharedpreferenceManager spManager;
    private Context mContext = this;
    Spinner spinner;
    Button btn_complete;
    EditText et_room,et_clinic,et_name;
    FrameLayout progressBarHolder;
    String name = "";
    String clinic = "";
    String room = "";
    String rank = "";

    private  Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == _msg_success) {
//                SharedPreferences sPref = MyApplication.context.getSharedPreferences(MinePageActivity.MINE_PAGE_DATA, MODE_PRIVATE);
//                SharedPreferences.Editor spfEditor = sPref.edit();
//                spfEditor.putString("name",name);
//                spfEditor.putString("clinic", clinic);
//                spfEditor.putString("room", room);
//                spfEditor.putString("rank", rank);
//                spfEditor.commit();
                MobclickAgent.onEvent(mContext,"doctor_fill_in_info");
                DoctorInfo dInfo = new DoctorInfo();
                dInfo.setName(name);
                dInfo.setClinic(clinic);
                dInfo.setRoom(room);
                dInfo.setRank(rank);
                DoctorInfoServiceImpl dInfoImpl = new DoctorInfoServiceImpl(mContext, null);
                int id = dInfoImpl.insert(dInfo);
//                SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
//                spManager.setOne("keyDoctorInfo", Integer.toString(id));
                progressBarHolder.setVisibility(GONE);
                Intent intent = new Intent(FillBasicInfoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == _msg_fail) {
                Toast.makeText(FillBasicInfoActivity.this,"提交失败，请重新提交",Toast.LENGTH_SHORT).show();
            }
        }

    };

    @Override
    protected void onResume(){
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    protected  void onPause(){
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_basic_info);
        btn_complete = (Button)findViewById(R.id.btn_complete);
        et_clinic = (EditText)findViewById(R.id.et_clinic);
        et_name= (EditText)findViewById(R.id.et_name);
        et_room = (EditText)findViewById(R.id.et_room);
        progressBarHolder = (FrameLayout)findViewById(R.id.progressBarHolder);

        btn_complete.setEnabled(false);
        spinner = (Spinner)findViewById(R.id.spinner);
        et_clinic.addTextChangedListener(this);
        et_name.addTextChangedListener(this);
        spManager = SharedpreferenceManager.getInstance();
        btn_complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                clinic = et_clinic.getText().toString();
                room  = et_room.getText().toString();
                rank = spinner.getSelectedItem().toString();
                if(room.equals("")){
                 room = "中医科";
                }
                progressBarHolder.setVisibility(View.VISIBLE);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("sid", spManager.getOne("keySid")));
                params.add(new BasicNameValuePair("token", spManager.getOne("keyToken")));
                params.add(new BasicNameValuePair("name",name));
                params.add(new BasicNameValuePair("clinic",clinic));
                params.add(new BasicNameValuePair("rank",rank));
                params.add(new BasicNameValuePair("room",room));
                MessageManager manager = MessageManager.getMessageManager();
                TextMessage tm = new TextMessage(Common.AddDoctorInfoUrl, params, new SimpleMessage.HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        // 处理返回结果
                        try {
                            JSONObject jsonResult = new JSONObject(result);
                            int code = jsonResult .getInt("code");

                            if (code == 200) {
                                mHandler.sendEmptyMessage(_msg_success);
                            }else {
                                mHandler.sendEmptyMessage(_msg_fail);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }, new SimpleMessage.HttpFailedCallBack() {

                    @Override
                    public boolean handle(int errorCode, String errorMessage) {

                        return false;
                    }

                });
                manager.sendMessage(tm);
            }

        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(et_name.getText().toString().equals("")||et_clinic.getText().toString().equals("")){
            btn_complete.setEnabled(false);
            btn_complete.setBackgroundResource(R.drawable.smssdk_btn_disenable);
        }else{
            btn_complete.setEnabled(true);
            btn_complete.setBackgroundResource(R.drawable.smssdk_btn_enable);
        }
    }
}
