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

public class ModifyDoctorRank extends Activity {
    private Context mContext = this;
    private EditText rank_et = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_one_line_edittext);
        TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
        TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("职称");
        TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("");
        rank_et = (EditText)findViewById(R.id.modify_name_cname_et);
        rank_et.setHint("请输入您的职称信息");

        Intent intent = getIntent();
        String rank = intent.getStringExtra("rank");
        if(!rank.isEmpty()){
            rank_et.setText(rank);
            rank_et.setSelection(rank_et.getText().length());
        }

        backTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getRank();
            }
        });
    }

    @Override
    public void onBackPressed() {
        getRank();
        super.onBackPressed();
    }

    private void getRank(){
        String rank = rank_et.getText().toString();
        if(rank.isEmpty()){
            Toast.makeText(mContext, "职称信息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("rank", rank);
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
