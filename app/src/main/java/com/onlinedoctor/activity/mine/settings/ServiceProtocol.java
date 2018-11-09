package com.onlinedoctor.activity.mine.settings;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.onlinedoctor.activity.R;
import com.umeng.analytics.MobclickAgent;

import org.w3c.dom.Text;

/**
 * Created by wds on 15/9/14.
 */
public class ServiceProtocol extends Activity{

    private WebView webView = null;
    private String url = null;
    private String flag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_protocol);
        ((TextView)findViewById(R.id.actionbar_common_backable_right_tv)).setText("");
        ((TextView)findViewById(R.id.actionbar_common_backable_title_tv)).setText("");
        webView = (WebView) findViewById(R.id.service_protocol_wv);

        flag = getIntent().getStringExtra("flag");
        if(flag != null) {
            if (flag.equals("AboutUs")) {
                webView.loadUrl("file:///android_asset/service_protocol/service_protocol.html");
            } else if (flag.equals("LinkMessage")) {
                url = getIntent().getStringExtra("LinkUrl");
                if ((url != null) && !url.equals("")) {
                    webView.loadUrl(url);
                }
            }
        }else{
            webView.loadUrl("file:///android_asset/service_protocol/service_protocol.html");
        }
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
