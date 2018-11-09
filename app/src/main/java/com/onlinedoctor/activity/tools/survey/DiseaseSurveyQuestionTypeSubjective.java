package com.onlinedoctor.activity.tools.survey;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.pojo.tools.survey.IMG;
import com.onlinedoctor.pojo.tools.survey.SubjectiveDTO;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class DiseaseSurveyQuestionTypeSubjective extends Activity {
    private Switch switchView = null;
    private EditText question_et = null;
    private Context mContext = this;
    private Button okBtn = null;
    private int type = Common.TYPE_SUBJECTIVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disease_survey_question_type_subjective);
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText(getResources().getString(R.string.survey_subjective));
        TextView right_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        right_tv.setText("");

        okBtn = (Button) findViewById(R.id.bt_ok);
        question_et = (EditText) findViewById(R.id.et_title);
        switchView = (Switch) findViewById(R.id.switch_view);
//        switchView.setTextOff(getResources().getString(R.string.no));
//        switchView.setTextOn(getResources().getString(R.string.yes));
        update();
        switchViewCheck();
        ok();
    }


    private void switchViewCheck() {
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    type = Common.TYPE_IMAGE;
                } else {
                    type = Common.TYPE_SUBJECTIVE;
                }
            }
        });
    }

    private void update() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            BaseQuestionDTO bq = (BaseQuestionDTO) bundle.getSerializable("SubjectiveQuestion");
            question_et.setText(bq.getTitle());
            checkAllowUploadImage(bq.getType());
            type = bq.getType();
        }
    }

    private void checkAllowUploadImage(int type) {
        switch (type) {
            case Common.TYPE_IMAGE:
                switchView.setChecked(true);
                break;
            case Common.TYPE_SUBJECTIVE:
                switchView.setChecked(false);
                break;
            default:
                break;
        }
    }

    private void ok() {

        okBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String question = question_et.getText().toString();
                Log.d("question = ", question);
                if (question.isEmpty()) {
                    Toast.makeText(mContext, "题目不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (type == -1) {
                    Toast.makeText(mContext, "请确认患者是否可以上传图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                BaseQuestionDTO q = (type == Common.TYPE_SUBJECTIVE) ? new SubjectiveDTO() : new IMG();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                q.setTitle(question);
                bundle.putSerializable("SubjectiveQuestion", q);
                bundle.putInt("position", getIntent().getIntExtra("position", -1));
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
