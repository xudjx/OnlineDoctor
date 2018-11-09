package com.onlinedoctor.activity.mine.homepage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wds on 15/8/13.
 */
public class ModifyDoctorClinic extends Activity {
    private Context mContext = this;
    private EditText clinic_et = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_one_line_edittext);
        TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
        TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("门诊地址");
        TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("");
        clinic_et = (EditText)findViewById(R.id.modify_name_cname_et);
        clinic_et.setHint("请输入您的门诊地址");

        Intent intent = getIntent();
        String clinic = intent.getStringExtra("clinic");
        if(!clinic.isEmpty()){
            clinic_et.setText(clinic);
            clinic_et.setSelection(clinic_et.getText().length());
        }

        backTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getClinic();
            }
        });
    }


    @Override
    public void onBackPressed() {
        getClinic();
        super.onBackPressed();
    }

    private void getClinic(){
        String clinic = clinic_et.getText().toString();
        if(clinic.isEmpty()){
            Toast.makeText(mContext, "门诊地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("clinic", clinic);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
