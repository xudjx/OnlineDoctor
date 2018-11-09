package com.onlinedoctor.activity.chats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.onlinedoctor.activity.R;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.util.VoiceUtil;
import com.onlinedoctor.view.AudioRecorder;
import com.onlinedoctor.view.AudioRecorderIMpl;
import com.onlinedoctor.view.RecordButton;
import com.umeng.analytics.MobclickAgent;

public class MsgMutilChatActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private RunDataContainer dataContainer = RunDataContainer.getContainer();
    private List<Patient> patientList;
    private String doctorId = SharedpreferenceManager.getInstance().getOne("keySid");
    private List<PatientMessage> messages = new ArrayList<>();

    private int LIMIT_COUNT = 5;
    private static long CURRENT_TIME = 0;
    private static final int CD_TIME = 15000;

    private String sendMsg = "";

    private TextView desTextView, nameTextView;
    private Button btnVoice, btnSend, btnKeyboard;
    private EditText msgEditText;
    private View lineView;
    private RecordButton recordButton;
    private LinearLayout msgLayout;
    private RelativeLayout editLayout;

    private static final int MSG_SEND_SUCCESS = 100;
    private static final int MSG_SEND_FAILED = 101;

    private ProgressDialog progress;

    private Handler mutilChatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_SUCCESS: {
                    progress.dismiss();
                    Toast.makeText(context,"消息发送成功", Toast.LENGTH_LONG).show();
                    PatientMessage message = (PatientMessage) msg.obj;
                    finish();
                    //showMessage(message);
                }
                break;
                case MSG_SEND_FAILED: {
                    progress.dismiss();
                    Toast.makeText(context,"消息发送失败，请稍后再试", Toast.LENGTH_LONG).show();
                    PatientMessage message = (PatientMessage) msg.obj;
                    finish();
                    //showMessage(message);
                }
                break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_mutil_chat);
        context = this;

        TextView add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        add_tv.setText("");
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText("群发");

        initData();
    }

    private void initData() {

        btnVoice = (Button) findViewById(R.id.btn_voice);
        btnKeyboard = (Button) findViewById(R.id.btn_keyboard);
        btnSend = (Button) findViewById(R.id.btn_send);
        lineView = (View) findViewById(R.id.line_view);
        msgEditText = (EditText) findViewById(R.id.et_sendmessage);
        editLayout = (RelativeLayout) findViewById(R.id.edit_layout);
        msgLayout = (LinearLayout) findViewById(R.id.message_layout);

        btnSend.setOnClickListener(this);
        btnVoice.setOnClickListener(this);
        btnKeyboard.setOnClickListener(this);

        recordButton = (RecordButton) findViewById(R.id.btn_record);
        final AudioRecorder mAudioRecorder = new AudioRecorderIMpl();
        recordButton.setAudioRecord(mAudioRecorder);
        recordButton.setRecordListener(new RecordButton.RecordListener() {
            // 录音成功的回调函数
            public void recordEnd() {
                if(LIMIT_COUNT >= 0){
                    long time = System.currentTimeMillis();
                    if((time - CURRENT_TIME) > CD_TIME){
                        LIMIT_COUNT--;
                        PatientMessage patientMessage = new PatientMessage(UUID.randomUUID().toString(), mAudioRecorder
                                .getFilePath(), Common.MESSAGE_TYPE_voice, doctorId, null, 1, 1, 0, System.currentTimeMillis());
                        send(patientMessage);
                    }else{
                        Toast.makeText(context, "消息发送太频繁，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(context, "消息发送太频繁，请稍后再试", Toast.LENGTH_LONG).show();
                }
            }
        });

        patientList = new ArrayList<Patient>();
        Bundle bundle = getIntent().getExtras();
        patientList = (List<Patient>) bundle.get("patientList");
        int Count = patientList.size();
        desTextView = (TextView) findViewById(R.id.des);
        nameTextView = (TextView) findViewById(R.id.name);
        String des = String.format("您将发消息给%d位朋友", Count);
        desTextView.setText(des);
        StringBuilder builder = new StringBuilder();
        for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext(); ) {
            Patient model = iterator.next();
            builder.append(model.getName());
            builder.append("、");
        }
        builder.deleteCharAt(builder.length() - 1);
        nameTextView.setText(builder.toString());

        msgEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    lineView.setBackgroundColor(getResources().getColor(R.color.title_blue));
                } else {
                    lineView.setBackgroundColor(getResources().getColor(R.color.chat_background));
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_voice:
                editLayout.setVisibility(View.INVISIBLE);
                recordButton.setVisibility(View.VISIBLE);
                btnVoice.setVisibility(View.INVISIBLE);
                btnKeyboard.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_keyboard:
                editLayout.setVisibility(View.VISIBLE);
                recordButton.setVisibility(View.INVISIBLE);
                btnVoice.setVisibility(View.VISIBLE);
                btnKeyboard.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_send:
                if(LIMIT_COUNT >= 0){
                    long time = System.currentTimeMillis();
                    if((time - CURRENT_TIME) > CD_TIME){
                        LIMIT_COUNT--;
                        sendMsg = msgEditText.getText().toString();
                        PatientMessage message = new PatientMessage(UUID.randomUUID().toString(), sendMsg, Common.MESSAGE_TYPE_text, doctorId, null, 1, 1, 0, System.currentTimeMillis());
                        send(message);
                    }else{
                        Toast.makeText(context, "消息发送太频繁，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                }else{
                   Toast.makeText(context, "消息发送太频繁，请稍后再试", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // 发送消息，发送完之后将 editText 设为空
    private void send(PatientMessage msg) {
        progress = ProgressDialog.show(context, null, "正在发送中", true, true);
        final PatientMessage pMsg = msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 记录发送的时间
                CURRENT_TIME = System.currentTimeMillis();
                for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext(); ) {
                    Patient patient = iterator.next();
                    PatientMessage message = new PatientMessage(pMsg);
                    message.setGuid(UUID.randomUUID().toString());
                    message.setToID(patient.getPatientId());
                    // add DataContainer
                    if(MyApplication.chatClient != null){
                        dataContainer.sendMessage(message, message.getToID(), false);
                    }else{
                        while(true) {
                            if(MyApplication.chatClient != null){
                                break;
                            }
                            try{
                                Thread.sleep(1000);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                                continue;
                            }
                        }
                    }
                    try{
                        Thread.sleep(200);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                        continue;
                    }
                }
                Message message = new Message();
                message.obj = pMsg;
                if (MyApplication.chatClient != null) {
                    message.what = MSG_SEND_SUCCESS;
                } else {
                    message.what = MSG_SEND_FAILED;
                }
                mutilChatHandler.sendMessage(message);
            }
        }).start();
    }

    private void showMessage(final PatientMessage message) {
        msgEditText.setText("");
        messages.add(message);
        Logger.d("MutilMessages ", "Count = " + messages.size());

        if(message.getContentType().equals(Common.MESSAGE_TYPE_text)){
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            lp.setMargins(0, 20, 0, 0);
            TextView tv = new TextView(context);
            tv.setLayoutParams(lp);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(20);
            tv.setTextColor(getResources().getColor(R.color.font));
            tv.setBackground(getResources().getDrawable(R.drawable.border_textview));
            tv.setText(message.getContent());
            msgLayout.addView(tv);
        }else if(message.getContentType().equals(Common.MESSAGE_TYPE_voice)){
            long duration = VoiceUtil.getAmrDuration(message.getContent());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) judgeVoiceLength(duration), LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            lp.setMargins(0, 20, 0, 0);
            ImageView imageView = new ImageView(context);
            imageView.setBackground(getResources().getDrawable(R.drawable.rect));
            imageView.setImageResource(R.drawable.voice_pup_left);
            imageView.setScaleType(ImageView.ScaleType.FIT_START);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (VoiceUtil.exists(message.getContent())) {
                        VoiceUtil.playMusic(message.getContent());
                    } else {
                        Toast.makeText(context, "不可播放，文件已被删除", Toast.LENGTH_LONG).show();
                    }
                }
            });
            msgLayout.addView(imageView);
        }
    }

    // 计算Voice消息的显示长度
    @SuppressWarnings("unused")
    private double judgeVoiceLength(long duration) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        Logger.d("Msg Voice", String.valueOf(width));
        double milli = duration / 1000.0;
        Logger.d("Msg Voice", "duration=" + milli);
        if (milli < 1.0) {
            return width / 8;
        } else if (milli < 1.5) {
            return width / 4;
        } else if (milli < 2.0) {
            return width / 3;
        } else if (milli < 3.0) {
            return width / 2.5;
        } else {
            return width / 2;
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
