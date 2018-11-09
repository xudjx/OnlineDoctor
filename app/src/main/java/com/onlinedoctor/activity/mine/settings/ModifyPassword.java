package com.onlinedoctor.activity.mine.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.mine.ModifyPasswordNetManager;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wds on 15/8/5.
 */
public class ModifyPassword extends Activity {

    private EditText old_passwd_et = null;
    private EditText new_passwd_et = null;
    private EditText confirm_passwd_et = null;

    private TextView save_tv = null;

    private Context mContext = this;
    private static final String TAG = "ModifyPassword";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ModifyPasswordNetManager.UPDATE_SUCCESSFUL:
                    Log.d("result", msg.obj.toString());
                    Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ModifyPasswordNetManager.UPDATE_FAIL:
                    Toast.makeText(mContext, "原密码错误，修改失败", Toast.LENGTH_SHORT).show();
                    Log.d("result", msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_password);

        TextView title_tv = (TextView)findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("修改密码");
        save_tv = (TextView)findViewById(R.id.actionbar_common_backable_right_tv);
        save_tv.setText("保存");

        old_passwd_et = (EditText)findViewById(R.id.old_passwd);
        new_passwd_et = (EditText)findViewById(R.id.new_passwd);
        confirm_passwd_et = (EditText)findViewById(R.id.confirm_passwd);


        //checkOldPasswd();
        checkNewPasswd();
        checkConfirmPasswd();
        save();
    }

   /* private void checkOldPasswd(){
        old_passwd_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if ((!b) && (!isOldPasswd(old_passwd_et.getText().toString())))
                    Toast.makeText(mContext, "原密码不对, 请重新输入!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void checkNewPasswd(){
        new_passwd_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if((!b) && (!isLegalPasswd(new_passwd_et.getText().toString()))){
                     Toast.makeText(mContext, "新密码只能包含字母和数字，请重新输入!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkConfirmPasswd(){
        confirm_passwd_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String newpwd = new_passwd_et.getText().toString();
                String cfpwd = confirm_passwd_et.getText().toString();
                if((!b) && (!isConfirm(newpwd, cfpwd))){
                    Toast.makeText(mContext, "两次输入密码不一致,请重新输入!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void save(){
        save_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldpasswd = old_passwd_et.getText().toString();
                String newpasswd = new_passwd_et.getText().toString();
                String confirmpasswd = confirm_passwd_et.getText().toString();

                boolean flag = true;
                /*
                //判断oldPassword是否正确
                if(!isOldPasswd(oldpasswd)){
                    Toast.makeText(mContext, "原密码不对, 请重新输入!", Toast.LENGTH_SHORT).show();
                    flag = false;
                }*/
                //判断新密码是否符合格式
                if (!isLegalPasswd(newpasswd)) {
                    Toast.makeText(mContext, "新密码只能包含字母和数字，请重新输入!",Toast.LENGTH_SHORT).show();
                    flag = false;
                }
                //判断新密码和确认密码是否一致
                if(!isConfirm(newpasswd, confirmpasswd)){
                    Toast.makeText(mContext, "两次输入密码不一致,请重新输入!",Toast.LENGTH_SHORT).show();
                    flag = false;
                }
                if(flag){
                    ModifyPasswordNetManager.update(oldpasswd, newpasswd, handler);
                }
            }
        });
    }

    private boolean isOldPasswd(String pwd){
        SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
        String localpwd = spManager.getOne("keyPassword");
        if(pwd.equals(localpwd))
            return true;
        else
            return false;
    }
    private boolean isLegalPasswd(String pwd){
        boolean flag = false;
        if(!pwd.isEmpty()){
            for(int i = 0; i < pwd.length(); i++){
                char varchar = pwd.charAt(i);
                if(!Character.isLetterOrDigit(varchar))
                    return flag;
            }
            flag = true;
        }
        return flag;
    }

    private boolean isConfirm(String pwd, String cfpwd){
        if(pwd.equals(cfpwd))
            return true;
        else
            return false;
    }
}

