package com.onlinedoctor.activity.tools.response;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.R;

public class FastResponseAdd extends Activity {

    private TextView save_tv = null;
    private EditText edit_et = null;
    private TextView backTextView = null;
    private Intent intentFrom = null;
    private Intent intentTo = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_multi_lines_edittext);

        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("编辑快捷回复");
        save_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("");
        edit_et = (EditText) findViewById(R.id.modify_name_cname_et);
        edit_et.setHint("输入快捷回复");

        backTextView = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);

        backTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        edit();
        //save();
    }

    private void edit() {

        intentFrom = getIntent();
        String msg = intentFrom.getStringExtra("msg");
        edit_et.setText(msg);
        edit_et.setSelection(msg.length());
    }

    @Override
    public void onBackPressed() {
        save();
        super.onBackPressed();
    }


    private void save() {
        String value = edit_et.getText().toString();

        if (!value.isEmpty()) {
            intentTo.putExtra("msg", value);
            setResult(RESULT_OK, intentTo);
            finish();
        } else {
            Toast.makeText(FastResponseAdd.this, "不能为空", Toast.LENGTH_SHORT).show();
        }
    }
}

//    private void save(){
//    	save_tv.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				String value = edit_et.getText().toString();
//
//				if(!value.isEmpty()){
//					intentTo.putExtra("msg", value);
//					setResult(RESULT_OK,intentTo);
//					finish();
//				}else{
//					Toast.makeText(FastResponseAdd.this, "不能为空", Toast.LENGTH_SHORT).show();
//				}
//			}
//		});
//    }


