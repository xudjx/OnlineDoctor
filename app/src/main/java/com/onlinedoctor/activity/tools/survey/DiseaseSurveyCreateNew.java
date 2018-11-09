package com.onlinedoctor.activity.tools.survey;

import com.onlinedoctor.activity.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DiseaseSurveyCreateNew extends Activity {
    private String TAG = "DiseaseSurveyCreateNew";
    private EditText title_et = null;
    private EditText abstractStr_et = null;
    private TextView save_tv = null;
    private Button createBtn = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disease_survey_create_new);
        mContext = DiseaseSurveyCreateNew.this;
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("创建问卷");
        save_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("");
        title_et = (EditText) findViewById(R.id.et_survey_title);
        //abstractStr_et = (EditText) findViewById(R.id.et_survey_abstract);
        createBtn = (Button) findViewById(R.id.bt_survey_create);

        //创建模板标题和摘要
        Log.i(TAG, "before createSurvey()");
        createSurvey();
    }

    private void createSurvey() {
        createBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                String strTitle = title_et.getText().toString();
                //String strAbstract = abstractStr_et.getText().toString();
                if (strTitle.isEmpty()) {
                    Log.d(TAG, "标题为空");
                    Toast.makeText(DiseaseSurveyCreateNew.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = null;
                    //Log.d(TAG,"将新创建的空的调查问卷保存到db");
                    //DiseaseSurveyImpl dsImpl = new DiseaseSurveyImpl(mContext, null);
                    //DiseaseSurvey ds = new DiseaseSurvey(strTitle, strAbstract);
                    //dsImpl.addDiseaseSurvey(ds);

//					Log.d(TAG,"将新添加的调查问卷返回给上一个activity");
//					intent = new Intent();
//					intent.putExtra("title", strTitle);
//					intent.putExtra("abstractStr", strAbstract);
//					setResult(RESULT_OK, intent);

                    Log.d(TAG, "准备跳转到下一个页面，为刚刚才创建的空的调查文件添加题目");
                    intent = new Intent(mContext, DiseaseSurveyQuestionsActivity.class);
                    intent.putExtra("title", strTitle);
                    intent.putExtra("title_id", "");
                    intent.putExtra("fromActivity", "DiseaseSurveyCreateNew");
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

}
