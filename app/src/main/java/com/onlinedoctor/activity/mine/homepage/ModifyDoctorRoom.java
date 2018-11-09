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
public class ModifyDoctorRoom extends Activity {
    private Context mContext = this;
    private EditText room_et = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_one_line_edittext);
        TextView backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
        TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("科室");
        TextView save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("");
        room_et = (EditText)findViewById(R.id.modify_name_cname_et);
        room_et.setHint("请输入您的科室信息");

        Intent intent = getIntent();
        String room = intent.getStringExtra("room");
        if(!room.isEmpty()){
            room_et.setText(room);
            room_et.setSelection(room_et.getText().length());
        }

        backTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getRoom();
            }
        });
    }

    @Override
    public void onBackPressed() {
        getRoom();
        super.onBackPressed();
    }

    private void getRoom(){
        String room = room_et.getText().toString();
        if(room.isEmpty()){
            Toast.makeText(mContext, "科室信息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("room", room);
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
