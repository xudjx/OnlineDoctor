package com.onlinedoctor.activity.mine.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wds on 15/8/30.
 */
public class AboutUs extends Activity{

    private TextView title_tv;
    private TextView save_tv;
    private TextView service_protocol_tv;
    private Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("关于我们");
        save_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("");

        service_protocol_tv = (TextView) findViewById(R.id.service_protocol);

        service_protocol_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, ServiceProtocol.class);
                intent.putExtra("flag","AboutUs");
                startActivity(intent);
            }
        });

        ((TextView)findViewById(R.id.version)).setText("Version " + getVersionName());
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

    private String getVersionName() {
        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            int versionCode = this.getPackageManager()
                    .getPackageInfo(pkName, 0).versionCode;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }
}
