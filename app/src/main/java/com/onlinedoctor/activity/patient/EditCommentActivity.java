package com.onlinedoctor.activity.patient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.sqlite.service.PatientInfoService;
import com.onlinedoctor.view.CommonActionbarCancelRelativeLayout;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2015/8/24.
 */
public class EditCommentActivity extends Activity {
    private Context mContext;
    private EditText commentContent;
    private TextView rightView;
    private CommonActionbarCancelRelativeLayout cacrl = null;
    private String updateId;
    private String preComment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_content);
        mContext = getApplicationContext();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Patient patient = (Patient) bundle.getSerializable("patient");
        updateId = patient.getPatientId();
        preComment = patient.getComment();
        getView();
        setListener();
    }

    private void getView(){
        commentContent = (EditText) this.findViewById(R.id.input_content);
        commentContent.setHint("输入备注");
        commentContent.setText(preComment);
        cacrl = (CommonActionbarCancelRelativeLayout) this.findViewById(R.id.edit_content_title);
        cacrl.setTitle(this.getResources().getString(R.string.edit_coment));
        cacrl.setRight(this.getResources().getString(R.string.finish_add_label));
        rightView = (TextView) cacrl.getRightView();
    }

    private void setListener(){
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientInfoService ps = new PatientInfoServiceImpl(mContext, null);
                ps.updatePatientComment(updateId, commentContent.getText().toString());
                finish();
            }
        });
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
