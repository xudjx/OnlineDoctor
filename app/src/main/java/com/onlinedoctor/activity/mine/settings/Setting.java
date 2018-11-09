package com.onlinedoctor.activity.mine.settings;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.activity.login.LoginActivity;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.net.VersionUpdateManager;
import com.onlinedoctor.net.mine.SettingNetManager;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.util.UpdateManager;
import com.onlinedoctor.view.BadgeView;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class Setting extends Activity {
    private LinearLayout quiet_lo = null;
    private LinearLayout about_lo = null;
    private LinearLayout chg_passwd_lo = null;
    private LinearLayout ll_version;
    private TextView tv_version;
    private Button logout_lo = null;
    private Switch quiet_switch = null;
    PopupWindow popupWindow;
    UpdateManager updateManager = null;
    private static final String TAG = "Setting";
    private Context mContext = this;
    private String versionName = "1.0.0";
    BadgeView badge_new_version;
    int isDislogExist = 0;
    RunDataContainer rdc = RunDataContainer.getContainer();
    private final Handler handler = new Handler() {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SettingNetManager.CHG_PASSWD_SUCCESSFUL:
                    Log.d(TAG, "CHG_PASSWD_SUCCESSFUL");
                    break;
                case SettingNetManager.CHG_PASSWD_FAIL:
                    Log.d(TAG, "CHG_PASSWD_FAIL");
                    break;
                case SettingNetManager.LOGOUT_SUCCESSFUL:
                    Log.d(TAG, "LOGOUT_SUCCESSFUL");
                case SettingNetManager.LOGOUT_FAIL:
                    Log.d(TAG, "LOGOUT_FAIL");
                    // Logout处理
                    MyApplication.getInstance().logout();

                    Intent intent = new Intent();
                    intent.setClass(mContext, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case SettingNetManager._msg_version:
                    if (isDislogExist == 1) return;
                    try {
                        JSONObject jo = new JSONObject(msg.obj.toString());
                        String serverVersion = jo.getString("version");
                        final String path = jo.getString("path");
                        String serverMsg = jo.getString("msg");
                        if (versionName.equals(serverVersion)) {
                            Toast.makeText(mContext, "已是最新版本", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        new AlertDialog.Builder(Setting.this).setTitle("升级提示").setMessage(serverMsg).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                isDislogExist = 0;
                            }
                        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                isDislogExist = 0;
                                Toast.makeText(Setting.this, "下载中...", Toast.LENGTH_SHORT).show();
                                if (updateManager == null)
                                    updateManager = new UpdateManager(Setting.this);
                                updateManager.downloadUrl = path;
                                updateManager.showDownloadDialog();
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                isDislogExist = 0;
                            }
                        }).show();
                        isDislogExist = 1;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SettingNetManager._msg_check_version:
                    try {
                        JSONObject jo = new JSONObject(msg.obj.toString());
                        String serverVersion = jo.getString("version");
                        if (versionName.equals(serverVersion))
                            badge_new_version.setVisibility(View.INVISIBLE);
                        else badge_new_version.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        TextView right_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        right_tv.setText("");
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("设置");

        quiet_lo = (LinearLayout) findViewById(R.id.setting_quiet_view);
        about_lo = (LinearLayout) findViewById(R.id.setting_about_view);
        chg_passwd_lo = (LinearLayout) findViewById(R.id.setting_password_modify_view);
        logout_lo = (Button) findViewById(R.id.setting_login_out_btn);
        ll_version = (LinearLayout) findViewById(R.id.setting_version_view);
        tv_version = (TextView) findViewById(R.id.tv_version);
        badge_new_version = (BadgeView) findViewById(R.id.badge_new_version);
        quiet_switch = (Switch) findViewById(R.id.is_quiet_switch);
        if (rdc.getQuiet()) {
            quiet_switch.setChecked(true);
        } else {
            quiet_switch.setChecked(false);
        }
        clickListener();
        try {
            versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_version.setText(versionName);
        SettingNetManager.checkVersion(handler);
    }

    private void clickListener() {
        quiet_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rdc.setQuiet(isChecked);
            }
        });

        about_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, AboutUs.class);
                startActivity(intent);
                //finish();
            }
        });

        chg_passwd_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, ModifyPassword.class);
                startActivity(intent);
            }
        });

        logout_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingNetManager.logout(handler);
            }
        });

        ll_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SettingNetManager.getVersion(handler);
                VersionUpdateManager updateManager = new VersionUpdateManager(mContext);
                updateManager.checkUpdate();
            }
        });

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


}
