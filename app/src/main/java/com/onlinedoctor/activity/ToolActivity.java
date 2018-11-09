package com.onlinedoctor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.onlinedoctor.activity.tools.prescription.PrescriptionManageActivity;
import com.onlinedoctor.activity.tools.survey.DiseaseSurveyActivity;
import com.onlinedoctor.activity.tools.response.FastResponseActivity;
import com.onlinedoctor.activity.tools.clinic.OfficeHoursActivity;
import com.onlinedoctor.adapter.tools.ToolsListAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.umeng.analytics.MobclickAgent;

public class ToolActivity extends Activity {

    private static final String TAG = "ToolActivity";
    private ListView toolsView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate");
        setContentView(R.layout.activity_tool);

        MyApplication.getInstance().addActivity(this);
        context = ToolActivity.this;
        init();
    }

    private void init() {
        toolsView = (ListView) findViewById(R.id.lv_menu);
        ToolsListAdapter listAdapter = new ToolsListAdapter(context, R.array.tool_name, R.array.tool_icons);
        toolsView.setAdapter(listAdapter);
        listListener();
    }

    private void listListener() {
        toolsView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                Intent intent = new Intent();
                switch (position) {
                    // 快捷回复
                    case 0:
                        intent.setClass(context, FastResponseActivity.class);
                        break;
                    // 门诊信息
                    case 1:
                        intent.setClass(context, OfficeHoursActivity.class);
                        break;
                    // 群发助手
                    case 2:
                        intent.setClass(context, PatientActivity.class);
                        //intent.putExtra("ActivityType", "MsgMutilChatActivity");
                        Bundle bundle = new Bundle();
                        bundle.putString("mode", "groupsend");
                        intent.putExtras(bundle);
                        break;
                    // 调查问卷
                    case 3:
                        intent.setClass(context, DiseaseSurveyActivity.class);
                        break;
                    // 处方管理
                    case 4:
                        intent.setClass(context, PrescriptionManageActivity.class);
                        break;
                    default:
                        break;
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.i(TAG, "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i(TAG, "onStop");
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
}
