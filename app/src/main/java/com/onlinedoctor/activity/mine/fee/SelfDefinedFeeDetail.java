package com.onlinedoctor.activity.mine.fee;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.mine.SelfDefinedFeeNetManager;
import com.onlinedoctor.pojo.mine.SelfDefinedFee;
import com.onlinedoctor.sqlite.dao.SelfDefinedFeesImpl;
import com.onlinedoctor.util.EditTextUtil;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SelfDefinedFeeDetail extends Activity {
    private TextView back_tv = null;
    private TextView save_tv = null;
    private EditText title_et = null;
    private EditText desc_et = null;
    private EditText fee_et = null;
    private final Context mContext = this;
    private SelfDefinedFeesImpl sdfImpl = null;
    private int flag = -1;
    private int id = -1;
    private long global_id = -1;
    private static final String TAG = "SelfDefinedFeeDetail";
    private FrameLayout progressBarHolder;
    private TextView title_tv;

    private String title, desc, fee;

    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SelfDefinedFeeNetManager.DELETE_SUCCESSFUL:
                    break;

                case SelfDefinedFeeNetManager.UPDATE_SUCCESSFUL: {
                    Bundle bundle = (Bundle) msg.obj;
                    long globalId = bundle.getLong("globalId");
                    long update_time = bundle.getLong("update_time");
                    SelfDefinedFee sdf = sdfImpl.queryGlobalId(globalId);
                    sdf.setUpdate_time(update_time);
                    sdfImpl.update(sdf);
                    break;
                }
                case SelfDefinedFeeNetManager.INSERT_SUCCESSFUL: {
                    Bundle bundle = (Bundle) msg.obj;
                    int id = bundle.getInt("id");
                    long globalId = bundle.getLong("globalId");
                    Log.d("in handler, globalId = ", Long.toString(globalId));
                    long update_time = bundle.getLong("update_time");
                    SelfDefinedFee sdf = sdfImpl.queryId(id);
                    sdf.setGlobal_id(globalId);
                    sdf.setUpdate_time(update_time);
                    sdfImpl.update(sdf);
                    break;
                }
                default:

                    break;
            }
            progressBarHolder.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_defined_fee_detail);
        title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("编辑收费明细");
        save_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("保存");
        back_tv = (TextView) findViewById(R.id.actionbar_common_backable_left_tv);
        //back_tv.setText("返回");
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);

        title_et = (EditText) findViewById(R.id.title_et);
        desc_et = (EditText) findViewById(R.id.desc_et);
        fee_et = (EditText) findViewById(R.id.fee_et);

        //dotReservedTwo();
        EditTextUtil.valueAfterDot(fee_et, 2);
        init();

        save_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String title = title_et.getText().toString();
                String desc = desc_et.getText().toString();
                String fee = fee_et.getText().toString(); //对fee需要做字符检查，保证fee能够正确转化为数字

                if (title.isEmpty() || desc.isEmpty() || fee.isEmpty()) {
                    Toast.makeText(mContext, "不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    save();
                    finish();
                }
            }
        });
        back_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

    }

    private void init() {
        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", -1);
        switch (flag) {
            case 1: // update
                title_tv.setText("编辑收费明细");
                title_et.setText(intent.getStringExtra("title"));
                desc_et.setText(intent.getStringExtra("description"));
                String fee = intent.getStringExtra("fee");
                fee_et.setText(fee);
                id = intent.getIntExtra("id", -1);
                global_id = intent.getLongExtra("global_id", -1);
                break;
            case 2: // add
                title_tv.setText("编辑收费明细");
                break;
            case 3: // 点击聊天界面里收费按钮图片实现跳转,(由于仅限于查看，因此不能编辑)
                title_tv.setText("查看收费明细");
                title_et.setText(intent.getStringExtra("title"));
                desc_et.setText(intent.getStringExtra("description"));
                fee_et.setText(intent.getStringExtra("fee"));
                title_et.setEnabled(false);
                desc_et.setEnabled(false);
                fee_et.setEnabled(false);
                title_et.setBackgroundColor(getResources().getColor(R.color.gray_light_light));
                desc_et.setBackgroundColor(getResources().getColor(R.color.gray_light_light));
                fee_et.setBackgroundColor(getResources().getColor(R.color.gray_light_light));
                title_et.setTextColor(getResources().getColor(R.color.gray));
                desc_et.setTextColor(getResources().getColor(R.color.gray));
                fee_et.setTextColor(getResources().getColor(R.color.gray));
                save_tv.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        title = title_et.getText().toString();
        desc = desc_et.getText().toString();
        fee = fee_et.getText().toString();
    }

    private void save() {
        String title = title_et.getText().toString();
        String desc = desc_et.getText().toString();
        String fee = fee_et.getText().toString(); //对fee需要做字符检查，保证fee能够正确转化为数字

        if (title.isEmpty() || desc.isEmpty() || fee.isEmpty()) {
            Toast.makeText(mContext, "不能为空", Toast.LENGTH_SHORT).show();
        } else {
            sdfImpl = new SelfDefinedFeesImpl(mContext, null);
            SelfDefinedFee sdf = null;
            if (flag == 1) {
                //update
                sdf = new SelfDefinedFee(id, title, desc, fee);
                sdf.setGlobal_id(global_id);
                sdfImpl.update(sdf);
                SelfDefinedFeeNetManager.update(sdf, handler);
            } else if (flag == 2) {
                //add
                sdf = new SelfDefinedFee(title, desc, fee);
                sdfImpl.add(sdf);
                SelfDefinedFeeNetManager.insert(sdf, handler);
            }
            progressBarHolder.setVisibility(View.VISIBLE);
        }
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


    @Override
    public void onBackPressed() {
        this.back();
        //super.onBackPressed();
    }


    private void back() {
        Log.d("come", " here");
        if (!(title_et.getText().toString().equals(title) &&
                desc_et.getText().toString().equals(desc) &&
                fee_et.getText().toString().equals(fee))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("是否保存编辑?");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String title = title_et.getText().toString();
                    String desc = desc_et.getText().toString();
                    String fee = fee_et.getText().toString(); //对fee需要做字符检查，保证fee能够正确转化为数字

                    if (title.isEmpty() || desc.isEmpty() || fee.isEmpty()) {
                        Toast.makeText(mContext, "不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        save();
                        finish();
                    }
                }

            });
            builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });
            builder.create().show();
        } else {
            finish();
        }
    }



}


