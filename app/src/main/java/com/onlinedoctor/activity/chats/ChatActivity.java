package com.onlinedoctor.activity.chats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.mine.SelfDefinedFeeNetManager;
import com.onlinedoctor.adapter.chats.ChatMsgRecyclerAdapter;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.pojo.message_content.LinkPatientMsgDO;
import com.onlinedoctor.pojo.mine.SelfDefinedFee;
import com.onlinedoctor.pojo.tools.prescription.PrescriptionDO;
import com.onlinedoctor.service.FloatBallService;
import com.onlinedoctor.service.NotifyManager;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.sqlite.service.DoctorInfoService;
import com.onlinedoctor.util.BitmapUtil;
import com.onlinedoctor.face.FaceConversionUtil;
import com.onlinedoctor.util.DisplayUtil;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.util.VoiceUtil;
import com.onlinedoctor.view.AudioRecorder;
import com.onlinedoctor.view.AudioRecorderIMpl;
import com.onlinedoctor.view.chat.ChatViewFactory;
import com.onlinedoctor.view.chat.FacePopupWindow;
import com.onlinedoctor.view.RecordButton;
import com.onlinedoctor.view.RecordButton.RecordListener;
import com.onlinedoctor.view.chat.MoreToolPopupWindow;
import com.umeng.analytics.MobclickAgent;

public class ChatActivity extends AppCompatActivity implements OnClickListener {

    public final String TAG = "ChatActivity";

    private Context context;
    private ViewStub chatMoreStub, faceViewStub;
    private Button sendBtn, moreBtn, expressionBtn, voiceBtn, keyBoardBtn;
    private RelativeLayout editLayout;
    private View lineView;
    private RecordButton recordBtn;
    private EditText messageEditText;
    private RecyclerView msgRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View chatView;

    private ChatMsgRecyclerAdapter msgAdapter;
    // msgList的所有访问需要加锁
    private List<PatientMessage> msgList;
    private Patient currentPatient;
    private String userId;
    private String doctorId;
    private String sendMsg = null;

    private DoctorInfo doctorInfo = null;

    // PopupWindow faceView and Toolmore
    private FacePopupWindow facePopupWindow;
    private MoreToolPopupWindow moreToolPopupWindow;

    private boolean IsFacePopupVisible = false;
    private boolean IsMoreToolPopupVisible = false;
    private boolean IsInputMethodVisible = false;
    private boolean IsEditTextTouched = false;

    private static int FacePopExchange = 1;
    private static int MoreToolPopExchange = 1;

    // 虚拟键盘的高度
    private int mVisibleHeight = 0;
    // 整个界面的可见高度
    private int chatViewHeight = 0;

    // 消息计时器
    //private MessageTimer messageTimer = MessageTimer.getInstance();

    private int draft = 0;
    private String draftString = "";

    private String ActivityType = "";

    private SharedpreferenceManager preManager = null;
    private RunDataContainer dataContainer = RunDataContainer.getContainer();

    // 图片暂存路径
    public static String TempPicPath = Common.sdPicSave + File.separator + "temp.png";

    // Activity Result返回区分
    public static final int TAKE_PICTURE = 10000;
    public static final int SHOW_CAMERA = 10001;
    public static final int SHOW_CAMERA_RESULT = 10002;
    public static final int PHOTO_CROP = 10005;
    public static final int FAST_RESPONSE = 10006;
    public static final int QUESTIONNAIRE_SELECTION = 10007;
    public static final int MINE_CHARGING = 10008;
    public static final int PRESCRIPTION_RESULT = 10009;

    // Handler message what
    public static final int SHOW_FACE_POPUP = 20001;
    public static final int SHOW_TOOL_POPUP = 20002;

    public static final String MIME_JPEG = "image/png";
    public static final String JPG = ".png";

    // 对msgList加锁
    private Lock listLock = new ReentrantLock();

    // 快捷回复结果返回
    private String fastResponseStr = null;


    @SuppressLint("HandlerLeak")
    private final Handler chatHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Common.MSG_WHAT_CHAT_MESSAGE_RECEIVE: // 接收到的患者消息
                {
                    PatientMessage pm = (PatientMessage) msg.obj;
                    refreshMessageList(pm, "receive");
                    break;
                }
                case Common.MSG_WHAT_CHAT_MESSAGE_SERVER_FEEDBACK: // server feedback
                {
                    Logger.d("MSG_FEEDBACK", "MSG_WHAT_CHAT_MESSAGE_SERVER_FEEDBACK");
                    String guid = (String) msg.obj;
                    refreshMessageList(guid, "feedback");
                    break;
                }
                case Common.MSG_WAHT_AUDIO_MESSAGE: // 语音消息
                {
                    PatientMessage pm = (PatientMessage) msg.obj;
                    refreshMessageList(pm, "send");
                    dataContainer.sendMessage(pm, userId, false);
                    break;
                }
                case Common.MSG_WAHT_IMAGE_MESSAGE: // 图片消息
                {
                    PatientMessage message = (PatientMessage) msg.obj;
                    refreshMessageList(message, "send");
                    dataContainer.sendMessage(message, userId, false);
                    break;
                }
                case Common.MSG_WHAT_OPEN_JUHUA: {
                    PatientMessage pm = (PatientMessage) msg.obj;
                    String guid = pm.getGuid();
                    listLock.lock();
                    int position = msgList.size() - 1;
                    for (; position >= 0; position--) {
                        if (msgList.get(position).getGuid().equals(guid)) {
                            break;
                        }
                    }
                    listLock.unlock();
                    break;
                }
                case Common.MSG_WHAT_CLOSE_JUHUA: {
                    PatientMessage pm = (PatientMessage) msg.obj;
                    String guid = pm.getGuid();
                    listLock.lock();
                    int position = msgList.size() - 1;
                    for (; position >= 0; position--) {
                        if (msgList.get(position).getGuid().equals(guid)) {
                            break;
                        }
                    }
                    listLock.unlock();
                    break;
                }
                case Common.MSG_WHAT_MESSAGE_DELETE: // 删除消息
                {
                    PatientMessage message = (PatientMessage) msg.obj;
                    dataContainer.deletePatientMessage(message, userId);
                    String guid = message.getGuid();
                    listLock.lock();
                    int position = msgList.size() - 1;
                    for (; position >= 0; position--) {
                        if (msgList.get(position).getGuid().equals(guid)) {
                            msgList.remove(position);
                            break;
                        }
                    }
                    listLock.unlock();
                    msgAdapter.notifyDataSetChanged();
                    break;
                }
                case Common.MSG_WHAT_SEND_FAILED: // 发送失败处理
                {
                    String guid = (String) msg.obj;
                    listLock.lock();
                    int position = msgList.size() - 1;
                    for (; position >= 0; position--) {
                        if (msgList.get(position).getGuid().equals(guid)) {
                            msgList.get(position).setSendStatus(-1);
                            PatientMessage message = new PatientMessage(msgList.get(position));
                            dataContainer.updatePatientMessage(message, guid);
                            break;
                        }
                    }
                    listLock.unlock();
                    msgAdapter.notifyDataSetChanged();
                    break;
                }
                case Common.MSG_WAHT_SEND_AGAIN: {
                    PatientMessage message = (PatientMessage) msg.obj;
                    dataContainer.sendMessage(message, userId, true);
                    msgAdapter.notifyDataSetChanged();
                    break;
                }
                case Common.MSG_WHAT_CLINIC_PLAN: // 门诊时间
                {
                    String text = (String) msg.obj;
                    PatientMessage pMessage = new PatientMessage(UUID.randomUUID().toString(), text, Common.MESSAGE_TYPE_text,
                            doctorId, userId, 0, 0, 0, System.currentTimeMillis());
                    refreshMessageList(pMessage, "send");
                    dataContainer.sendMessage(pMessage, userId, false);
                    break;
                }
                case Common.MSG_WHAT_QRCODE: // 链接信息
                {
                    String QrCode = (String) msg.obj;
                    LinkPatientMsgDO messageContent = new LinkPatientMsgDO(Common.HTTP_SERVER + "DoctorCard?doctorId=" + doctorId, doctorInfo.getName() + "医生的二维码名片", getResources().getString(R.string.qrcode_description), QrCode);
                    PatientMessage message = new PatientMessage(UUID.randomUUID().toString(), JsonUtil.objectToJson(messageContent), Common.MESSAGE_TYPE_link,
                            doctorId, userId, 0, 0, 0, System.currentTimeMillis());
                    refreshMessageList(message, "send");
                    dataContainer.sendMessage(message, userId, false);
                    break;
                }
                case Common.MSG_WHAT_HIDE: // 滑动时触发，掩藏相应信息
                {
                    hideInputMethod();
                    closeFacePopupWindow();
                    closeMorePopupWindow();
                    break;
                }
                case Common.MSG_WHAT_MINE_CHARGING: // 自定义收费信息
                {

                    SelfDefinedFee sdf = (SelfDefinedFee) msg.obj;
                    MobclickAgent.onEventValue(context, "doctor_send_pay_request", null, (int) (Float.parseFloat(sdf.getFee()) * 100));
                    Logger.d("FEE = ", sdf.getFee());
                    Logger.d("TITLE = ", sdf.getTitle());
                    Logger.d("DESC = ", sdf.getDescription());
                    Map<String, String> map = new HashMap<>();
                    map.put("title", sdf.getTitle());
                    map.put("description", sdf.getDescription());
                    map.put("fee", sdf.getFee());
                    PatientMessage message = new PatientMessage(UUID.randomUUID().toString(), JsonUtil.objectToJson(map), Common.MESSAGE_TYPE_paymentRequest,
                            doctorId, userId, 0, 0, 0, System.currentTimeMillis());
                    refreshMessageList(message, "send");
                    dataContainer.sendMessage(message, userId, false);
                    break;
                }

                case SelfDefinedFeeNetManager.REQUEST_FAIL: {
                    Toast.makeText(context, "自定义收费请求失败", Toast.LENGTH_SHORT).show();
                    break;
                }
                case SHOW_FACE_POPUP: {
                    if (facePopupWindow == null) {
                        facePopupWindow = new FacePopupWindow(ChatActivity.this, new FacePopupWindow.FaceItemClickListener(messageEditText), mVisibleHeight);
                    }
                    facePopupWindow.showAtLocation(chatView, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                    IsFacePopupVisible = true;
                    break;
                }
                case SHOW_TOOL_POPUP: {
                    if (moreToolPopupWindow == null) {
                        moreToolPopupWindow = new MoreToolPopupWindow(ChatActivity.this, mVisibleHeight, currentPatient);
                    }
                    moreToolPopupWindow.showAtLocation(chatView, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
                    IsMoreToolPopupVisible = true;
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = ChatActivity.this;
        preManager = SharedpreferenceManager.getInstance();

        DoctorInfoService doctorInfoService = new DoctorInfoServiceImpl(context, null);
        doctorInfo = doctorInfoService.get(1);

        // 获取表情数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                FaceConversionUtil.getInstace().getFileText(getApplication());
            }
        }).start();

        HandlerManager.getManager().setChatHandler(chatHandler);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        currentPatient = (Patient) bundle.getSerializable("patient");
        ActivityType = bundle.getString("FromActivity", "");
        userId = currentPatient.getPatientId();
        draft = bundle.getInt("draft", 0);
        draftString = bundle.getString("draft_message");
        Logger.i("ChatActivity userId", userId);
        dataContainer.setChatUserId(userId);
        doctorId = preManager.getOne("keySid");
        Logger.i("ChatActivity doctorID", doctorId);

        editLayout = (RelativeLayout) findViewById(R.id.edit_layout);
        lineView = (View) findViewById(R.id.line_view);
        messageEditText = (EditText) findViewById(R.id.et_sendmessage);
        sendBtn = (Button) findViewById(R.id.btn_send);
        moreBtn = (Button) findViewById(R.id.btn_more);
        expressionBtn = (Button) findViewById(R.id.btn_expression);
        keyBoardBtn = (Button) findViewById(R.id.btn_keyboard);
        voiceBtn = (Button) findViewById(R.id.btn_voice);
        msgRecyclerView = (RecyclerView) findViewById(R.id.msgListview);
        msgRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        chatView = (View) findViewById(R.id.chat_layout);
        TextView add_tv = (TextView) findViewById(R.id.actionbar_common_backable_right_tv);
        add_tv.setText("");
        TextView title_tv = (TextView) findViewById(R.id.actionbar_common_backable_title_tv);
        title_tv.setText(currentPatient.getName());
        findViewById(R.id.actionbar_common_backable_left_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFinish();
            }
        });

        messageEditText.addTextChangedListener(textWatcher);
        // 如果是草稿数据，则显示
        if (draft == 1) {
            SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context,
                    draftString, "normal");
            messageEditText.setText(spannableString);
        }
        sendBtn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        expressionBtn.setOnClickListener(this);
        voiceBtn.setOnClickListener(this);
        keyBoardBtn.setOnClickListener(this);

        recordBtn = (RecordButton) findViewById(R.id.btn_record);
        final AudioRecorder mAudioRecorder = new AudioRecorderIMpl();
        recordBtn.setAudioRecord(mAudioRecorder);
        recordBtn.setRecordListener(new RecordListener() {
            // 录音成功的回调函数
            public void recordEnd() {
                Message message = new Message();
                message.what = Common.MSG_WAHT_AUDIO_MESSAGE;
                PatientMessage patientMessage = new PatientMessage(UUID.randomUUID().toString(), mAudioRecorder
                        .getFilePath(), "voice", doctorId, userId, 0, 0, 0, System.currentTimeMillis());
                message.obj = patientMessage;
                chatHandler.sendMessage(message);
            }
        });

        // set the init date to msgList
        initChatMessageList();
        msgAdapter = new ChatMsgRecyclerAdapter(context, currentPatient, msgList);
        msgRecyclerView.setAdapter(msgAdapter);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.theme_accent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<String, Integer, String>() {

                    int addCount = 0;

                    @Override
                    protected void onPreExecute() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        super.onPreExecute();
                    }

                    @Override
                    protected String doInBackground(String... arg0) {
                        List<PatientMessage> list = dataContainer.getPatientMessageContinue(userId, msgList.get(0)
                                .getGuid());
                        addCount = list.size();
                        list.addAll(msgList);
                        msgList.clear();
                        msgList.addAll(list);
                        Logger.i("下拉加载", "msgList size=" + msgList.size());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        msgAdapter.notifyItemRangeInserted(0, addCount);
                        msgRecyclerView.scrollToPosition(addCount - 1);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }.execute(null, null, null);
            }
        });

        msgRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Logger.d("OnScrollListener", "OnScrollListener");
                Message message = new Message();
                message.what = Common.MSG_WHAT_HIDE;
                chatHandler.sendMessage(message);
            }
        });

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    lineView.setBackgroundColor(getResources().getColor(R.color.title_blue));
                } else {
                    lineView.setBackgroundColor(getResources().getColor(R.color.chat_background));
                }
            }
        });

        messageEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Logger.d("EditText", "ACTION_UP");
                    IsEditTextTouched = true;
                    IsInputMethodVisible = true;
                    closeMorePopupWindow();
                    closeFacePopupWindow();
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                }
                return false;
            }
        });

        chatView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                int height = chatView.getHeight();
                if (chatViewHeight == 0) {
                    // chatViewHeight的值设定为ChatView的初始高度
                    chatViewHeight = chatView.getHeight();
                }
                if (height < chatViewHeight) {
                    Rect r = new Rect();
                    chatView.getWindowVisibleDisplayFrame(r);
                    int visible = r.bottom - r.top;
                    mVisibleHeight = chatViewHeight - visible;

                    // recycleView 上滑
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                } else if (height == chatViewHeight) {
                    // 输入法掩藏了，popupwindow也要跟着掩藏
                    closeFacePopupWindow();
                    closeMorePopupWindow();
                    IsInputMethodVisible = false;
                    IsFacePopupVisible = false;
                    IsMoreToolPopupVisible = false;
                    FacePopExchange = 1;
                    MoreToolPopExchange = 1;
                }

            }
        });
    }

    /**
     * Init ChatMsgListAdapter \n 1、如果内存缓存中有当前userID 聊天信息，直接加载
     * 2、如果没有，则从数据库中读取，并更新内存缓存
     */
    private void initChatMessageList() {
        msgList = dataContainer.getPatientMessage(userId);
        if (msgList == null) {
            msgList = new ArrayList<>();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send: {
                send();
                break;
            }
            case R.id.et_sendmessage: {
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                closeFacePopupWindow();
                closeMorePopupWindow();
                break;
            }
            case R.id.btn_more: {
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                FacePopExchange = 1;
                openInputMethod();
                closeFacePopupWindow();
                switch (MoreToolPopExchange) {
                    case 1:
                        showMorePopupWindow();
                        MoreToolPopExchange = 2;
                        break;
                    case 2:
                        closeMorePopupWindow();
                        MoreToolPopExchange = 1;
                        break;
                }
                recordBtn.setVisibility(View.GONE);
                editLayout.setVisibility(View.VISIBLE);
                voiceBtn.setVisibility(View.VISIBLE);
                keyBoardBtn.setVisibility(View.GONE);
                break;
            }
            case R.id.btn_expression: {
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                MoreToolPopExchange = 1;
                openInputMethod();
                closeMorePopupWindow();
                switch (FacePopExchange) {
                    case 1:
                        showFacePopupWindow();
                        FacePopExchange = 2;
                        break;
                    case 2:
                        closeFacePopupWindow();
                        FacePopExchange = 1;
                        break;
                }
                break;
            }
            case R.id.btn_voice: {
                hideInputMethod();
                closeMorePopupWindow();
                closeFacePopupWindow();
                editLayout.setVisibility(View.GONE);
                recordBtn.setVisibility(View.VISIBLE);
                voiceBtn.setVisibility(View.GONE);
                keyBoardBtn.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.btn_keyboard: {
                hideInputMethod();
                recordBtn.setVisibility(View.GONE);
                editLayout.setVisibility(View.VISIBLE);
                voiceBtn.setVisibility(View.VISIBLE);
                keyBoardBtn.setVisibility(View.GONE);
                if (IsEditTextTouched) {
                    messageEditText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(messageEditText, InputMethodManager.SHOW_FORCED);
                    IsInputMethodVisible = true;
                }
                break;
            }
            default:
                break;
        }
    }

    // 发送消息，发送完之后将 editText 设为空
    private void send() {
        sendMsg = messageEditText.getText().toString();
        if (sendMsg.length() > 0) {
            PatientMessage message = new PatientMessage();
            message.setContent(sendMsg);
            message.setContentType("text");
            message.setFromID(doctorId);
            message.setToID(userId);
            message.setGuid(UUID.randomUUID().toString());
            message.setTimestamp(System.currentTimeMillis());
            messageEditText.setText("");
            refreshMessageList(message, "send");
            // add DataContainer
            dataContainer.sendMessage(message, userId, false);
        }
    }

    private void refreshMessageList(Object message, String flag) {
        Logger.i("Message Handler", flag);
        PatientMessage entity = new PatientMessage();
        if ("send".equals(flag)) {
            entity = (PatientMessage) message;
            listLock.lock();
            msgList.add(entity);
            listLock.unlock();
        } else if ("receive".equals(flag)) {
            entity = (PatientMessage) message;
            listLock.lock();
            msgList.add(entity);
            listLock.unlock();
        } else if ("feedback".equals(flag)) {
            String guid = (String) message;
            listLock.lock();
            int position = msgList.size() - 1;
            for (; position >= 0; position--) {
                if (msgList.get(position).getGuid().equals(guid)) {
                    msgList.get(position).setSendStatus(1);
                    break;
                }
            }
            listLock.unlock();
        }
        Logger.d("MsgList size", String.valueOf(msgList.size()));
        msgAdapter.notifyDataSetChanged();
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
    }

    // 关闭输入法
    private boolean hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
        IsInputMethodVisible = false;
//        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(ChatActivity.this.getCurrentFocus().getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);
        return true;
    }

    // 打开输入法
    private void openInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(messageEditText, InputMethodManager.SHOW_FORCED);
        IsInputMethodVisible = true;
    }

    private void showFacePopupWindow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mVisibleHeight != 0) {
                        Message message = new Message();
                        message.what = SHOW_FACE_POPUP;
                        chatHandler.sendMessage(message);
                        break;
                    }
                }
            }
        }).start();
    }

    private void closeFacePopupWindow() {
        if (facePopupWindow != null) {
            facePopupWindow.dismiss();
            IsFacePopupVisible = false;
            if (facePopupWindow == null) {
                Logger.d("", "PopupWindow destory after dismiss");
            }
            // 不可见之后，重置为1
            FacePopExchange = 1;
        }
    }

    private void showMorePopupWindow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mVisibleHeight != 0) {
                        Message message = new Message();
                        message.what = SHOW_TOOL_POPUP;
                        chatHandler.sendMessage(message);
                        break;
                    }
                }
            }
        }).start();
    }

    private void closeMorePopupWindow() {
        if (moreToolPopupWindow != null) {
            moreToolPopupWindow.dismiss();
            IsMoreToolPopupVisible = false;
            // 不可见之后，重置为1
            MoreToolPopExchange = 1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // float ball
        //startService(new Intent(this, FloatBallService.class));
        dataContainer.setInChatActivity(true, userId);
        refreshUnReadNotice(userId);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void refreshUnReadNotice(String patientId) {
        // 清除通知栏信息
        NotifyManager.getManager().cancelById(patientId);
        List<BriefMessagePojo> messagePojos = dataContainer.getBriefMessage();
        for (Iterator<BriefMessagePojo> iterator = messagePojos.iterator(); iterator.hasNext(); ) {
            BriefMessagePojo pojo = iterator.next();
            if (pojo.getUserId().equals(patientId)) {
                BriefMessagePojo messagePojo = new BriefMessagePojo(pojo);
                messagePojo.setCount(0);
                dataContainer.updateBriefMessage(messagePojo);
                break;
            }
        }
        Message message = new Message();
        message.what = Common.MSG_WHAT_BRIEFCHANGE_MESSAGE;
        if (HandlerManager.getManager().getMainHandler() != null) {
            HandlerManager.getManager().getMainHandler().sendMessage(message);
        }
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, FloatBallService.class));
        Logger.i(TAG, "ChatActivity onStop");
        dataContainer.setInChatActivity(false, userId);
        VoiceUtil.stopPlay();
        hideInputMethod();
        closeFacePopupWindow();
        closeMorePopupWindow();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.i(TAG, "ChatActivity onDestroy");
        super.onDestroy();
        ChatViewFactory.getInstance().clearCache();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleFinish();
        }
        return false;
    }

    private void handleFinish() {
        if (IsFacePopupVisible || IsMoreToolPopupVisible || IsInputMethodVisible) {
            hideInputMethod();
            closeFacePopupWindow();
            closeMorePopupWindow();
        } else {
            if ((draft == 1) && ("".equals(messageEditText.getText().toString()))) {
                dataContainer.setBriefMessageDraft(userId, false, "", "");
            }

            if (!("".equals(messageEditText.getText().toString()))) {
                dataContainer.setBriefMessageDraft(userId, true, messageEditText.getText().toString(), Common.MESSAGE_TYPE_text);
            }
            if (ActivityType.equals("PatientInfoActivity")) {
                finish();
                return;
            }
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * ********************* 拍照 相册选择等的回调方法 *********************
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(ChatActivity.this, "请插入SD卡", Toast.LENGTH_LONG).show();
                return;
            }

            Bitmap bitmap;
            String filename = getCurrentDate() + ".png";
            bitmap = BitmapUtil.matrixRotate(TempPicPath);
            File file = new File(Common.sdPicSave);
            if (!file.exists()) {
                file.mkdirs();
            }
            String filePath = file.getPath() + File.separator + filename;
            DisplayMetrics metrics = DisplayUtil.getDisplaymetrics(context);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            try{
                // 压缩宽高可以修改
                BitmapUtil.ratioAndGenThumb(bitmap, filePath, width/2, height/2);
            }catch (Exception e){
                e.printStackTrace();
            }
            Intent intent = new Intent(ChatActivity.this, CameraActivity.class);
            intent.putExtra("camera", filePath);
            ChatActivity.this.startActivityForResult(intent, SHOW_CAMERA);

            /*
            String photoPath = TempPicPath;
            if (TextUtils.isEmpty(photoPath)) {
                Toast.makeText(context, com.netease.nim.uikit.R.string.picker_image_error, Toast.LENGTH_LONG).show();
                return;
            }
            File imageFile = new File(photoPath);
            File scaledImageFile = ImageUtil.getScaledImageFileWithMD5(imageFile, MIME_JPEG);
            // 删除拍照生成的临时文件
            AttachmentStore.delete(photoPath);
            if (scaledImageFile == null) {
                Toast.makeText(context, com.netease.nim.uikit.R.string.picker_image_error, Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(ChatActivity.this, CameraActivity.class);
            intent.putExtra("camera", scaledImageFile.getAbsolutePath());
            ChatActivity.this.startActivityForResult(intent, SHOW_CAMERA);
            */
        } else if (requestCode == SHOW_CAMERA && resultCode == SHOW_CAMERA_RESULT) {
            Bundle bundle = data.getExtras();
            Object imageUrl = bundle.get("imgUrl");
            Logger.d("TAG", "Camera Image: " + imageUrl.toString());
            if (imageUrl.toString().length() > 0) {
                String imageContent = handlerImageSave(imageUrl.toString());
                Message message = new Message();
                message.what = Common.MSG_WAHT_IMAGE_MESSAGE;
                PatientMessage pMessage = new PatientMessage(UUID.randomUUID().toString(), imageContent, "image",
                        doctorId, userId, 0, 0, 0, System.currentTimeMillis());
                message.obj = pMessage;
                chatHandler.sendMessage(message);
            }
        } else if (requestCode == PHOTO_CROP && resultCode == RESULT_OK) {
            Logger.i(TAG, "PHOTO_CROP");
            Uri imageUrl = data.getData();
            String type = data.getType();
            int flag = data.getFlags();
            Logger.i("PHOTO_CROP data", data.toString());
            if (null == imageUrl) {
                Toast.makeText(context, "图片选择失败，请重新选择", Toast.LENGTH_LONG).show();
            } else {
                String imagePath = BitmapUtil.getImageAbsolutePath(ChatActivity.this, imageUrl);
                String filename = getCurrentDate() + ".png";
                String filePath = Common.sdPicSave + File.separator + filename;
                DisplayMetrics metrics = DisplayUtil.getDisplaymetrics(context);
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                try{
                    // 压缩宽高可以修改
                    BitmapUtil.ratioAndGenThumb(imagePath, filePath, width/2, height/2,false);
                }catch (Exception e){
                    e.printStackTrace();
                }
                Intent intent = new Intent(ChatActivity.this, CameraActivity.class);
                intent.putExtra("camera", filePath);
                ChatActivity.this.startActivityForResult(intent, SHOW_CAMERA);
            }
        } else if (requestCode == FAST_RESPONSE && resultCode == RESULT_OK) {
            Logger.i(TAG, "FAST_RESPONSE");
            fastResponseStr = data.getStringExtra("msg");
            if (fastResponseStr.isEmpty()) {
                Logger.e(TAG, "快捷回复返回为空");
            } else {
                Logger.d("fastResponseStr", fastResponseStr);
                messageEditText.setText(fastResponseStr);
            }
        } else if (requestCode == QUESTIONNAIRE_SELECTION && resultCode == RESULT_OK) {
            PatientMessage pMessage = new PatientMessage(UUID.randomUUID().toString(), data.getIntExtra("globalId", -1) + "", Common.MESSAGE_TYPE_survey,
                    doctorId, userId, 0, 0, 0, System.currentTimeMillis());
            refreshMessageList(pMessage, "send");
            dataContainer.sendMessage(pMessage, userId, false);
        }else if(requestCode == PRESCRIPTION_RESULT && resultCode == RESULT_OK){
            PrescriptionDO prescriptionDO = (PrescriptionDO)data.getSerializableExtra("prescription");
            PatientMessage pMessage = new PatientMessage(UUID.randomUUID().toString(), JsonUtil.objectToJson(prescriptionDO), Common.MESSAGE_TYPE_prescription,
                    doctorId, userId, 0,0,0,System.currentTimeMillis());
            refreshMessageList(pMessage, "send");
            dataContainer.sendMessage(pMessage, userId,false);
        }
    }

    /**
     * 传入原始图片的路径，创建缩略图 返回ImageContent
     */
    private String handlerImageSave(String srcPath) {
        String imageContent = null;
        String fileName = srcPath.substring(srcPath.lastIndexOf("/") + 1);
        String desPath = Common.sdPicSave + File.separator + "thumbnail_" + fileName;
        try {
            BitmapUtil.storeImage(srcPath, desPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        // 将图片的绝对路径转换成 SD card 路径
        imageContent = JsonUtil.objectToJson(new Bean("file://" + desPath, "file://" + srcPath));
        return imageContent;
    }

    /**
     * 发送信息输入框的消息监听
     */
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!messageEditText.getText().toString().equals("")) {
                moreBtn.setVisibility(View.INVISIBLE);
                sendBtn.setVisibility(View.VISIBLE);
            } else {
                moreBtn.setVisibility(View.VISIBLE);
                sendBtn.setVisibility(View.INVISIBLE);
            }
        }
    };

    // 以当前时间作为文件名
    @SuppressLint("SimpleDateFormat")
    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        return str;
    }

    public class Bean {
        String thumbnailPath;
        String avaterPath;

        public Bean(String thumbnailPath, String avaterPath) {
            super();
            this.thumbnailPath = thumbnailPath;
            this.avaterPath = avaterPath;
        }
    }
}
