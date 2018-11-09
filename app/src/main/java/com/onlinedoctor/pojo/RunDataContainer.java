package com.onlinedoctor.pojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.onlinedoctor.adapter.DataAdapter;
import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.HandlerManager;
import com.onlinedoctor.net.QiniuUtil;
import com.onlinedoctor.net.message.ImageMessage;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.MessageTimer;
import com.onlinedoctor.net.MyChatClient;
import com.onlinedoctor.net.message.SimpleMessage;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.net.message.UploadFileToQiniuMessage;
import com.onlinedoctor.net.message.VoiceMessage;
import com.onlinedoctor.pojo.chats.BriefMessagePojo;
import com.onlinedoctor.pojo.chats.PatientMessageDTO;
import com.onlinedoctor.pojo.chats.SynAtnMessageDTO;
import com.onlinedoctor.pojo.chats.SynMessageDTO;
import com.onlinedoctor.pojo.patient.Label;
import com.onlinedoctor.pojo.patient.NewPatient;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientDoctorRel;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.patient.Record;
import com.onlinedoctor.service.NotifyManager;
import com.onlinedoctor.sqlite.dao.CurrentChatUserServiceImpl;
import com.onlinedoctor.sqlite.dao.LabelServiceImpl;
import com.onlinedoctor.sqlite.dao.NewPatientInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientDocRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientLabelRelationServiceImpl;
import com.onlinedoctor.sqlite.dao.PatientMessageInfoServiceImpl;
import com.onlinedoctor.sqlite.dao.RecordServiceImpl;
import com.onlinedoctor.sqlite.service.CurrentChatUserService;
import com.onlinedoctor.sqlite.service.LabelService;
import com.onlinedoctor.sqlite.service.NewPatientInfoService;
import com.onlinedoctor.sqlite.service.PatientDocRelationService;
import com.onlinedoctor.sqlite.service.PatientInfoService;
import com.onlinedoctor.sqlite.service.PatientLabelRelationService;
import com.onlinedoctor.sqlite.service.PatientMessageInfoService;
import com.onlinedoctor.sqlite.service.RecordService;
import com.onlinedoctor.util.BitmapUtil;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.onlinedoctor.util.VoiceUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * @author Song
 */

public class RunDataContainer {

    private String chatActivityUserId = "";
    private boolean isInMessageActivity = false;
    private boolean isInChatActivity = false;
    private String curPatientId = "";
    private boolean isInPatientActivity = false;
    private int newPatientNum = 0;
    private static final int sendTryCount = 30;
    private boolean isQuiet = false;

    private static RunDataContainer instance;
    private HandlerManager handlerManager = HandlerManager.getManager();

    private ConcurrentHashMap<String, List<PatientMessage>> patientMessageMap = new ConcurrentHashMap<String, List<PatientMessage>>();
    private HashMap<String, Integer> briefMessageIndex = new HashMap<String, Integer>();
    private List<BriefMessagePojo> briefMessageList = new ArrayList<BriefMessagePojo>();
    private ConcurrentHashMap<String, PatientMessage> patientMessageSent = new ConcurrentHashMap<String, PatientMessage>();
    private ConcurrentHashMap<String, Boolean> atnPatientIdMap = new ConcurrentHashMap<String, Boolean>();// 暂时用Concurrent
    private ConcurrentHashMap<Long, Label> labelIdMap = new ConcurrentHashMap<Long, Label>();                                                                                                        // map替代Concurren
    private HashSet<Integer> allLabelColors = new HashSet<Integer>();
    // set的功能，

    // lock
    private ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<String, ReentrantReadWriteLock>();
    private ReentrantReadWriteLock briefMessageLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock chatActivityUserIdLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock isMessageActivityLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock isChatActivityLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock isPatientActivityLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock reInitLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock timeStampLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock atnTimeStampLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock newPatientNumLock = new ReentrantReadWriteLock(true);
    private ReentrantReadWriteLock patientAtnLock = new ReentrantReadWriteLock(true);

    private PatientMessageInfoService patientMessageInfoService = new PatientMessageInfoServiceImpl(
            MyApplication.context, null);
    private CurrentChatUserService currentChatUserService = new CurrentChatUserServiceImpl(MyApplication.context, null);
    private PatientInfoService patientInfoService = new PatientInfoServiceImpl(MyApplication.context, null);
    private NewPatientInfoService newPatientInfoService = new NewPatientInfoServiceImpl(MyApplication.context, null);
    private PatientDocRelationService patientDocRelationService = new PatientDocRelationServiceImpl(
            MyApplication.context, null);
    private PatientLabelRelationService patientLabelRelationService = new PatientLabelRelationServiceImpl(
            MyApplication.context, null);
    private LabelService labelService = new LabelServiceImpl(MyApplication.context, null);
    private RecordService recordService = new RecordServiceImpl(MyApplication.context, null);

    private MyChatClient chatClient;

    private SharedpreferenceManager spManager;

    private RunDataContainer() {
        init();
    }

    public static RunDataContainer getContainer() {
        if (instance == null) {
            synchronized (RunDataContainer.class) {
                if (instance == null) {
                    instance = new RunDataContainer();
                    Log.d("RDCDebug", "RunDataContainer new");
                }
            }
        }
        return instance;
    }

    private int init() {
        this.briefMessageList = currentChatUserService.getAllChatUsersInTimeOrder();
        // for debug
        BriefMessagePojo briefMessagePojo1 = new BriefMessagePojo(10, "p_10", "测试用户", "测试消息提示", "http://image.dev.dajiazhongyi.com/studio/image.jpg",
                System.currentTimeMillis(), false, Common.MESSAGE_TYPE_text, "", 1, "", 0);
        BriefMessagePojo briefMessagePojo2 = new BriefMessagePojo(10, "p_10", "测试用户", "测试消息提示", "http://image.dev.dajiazhongyi.com/studio/image.jpg",
                System.currentTimeMillis(), false, Common.MESSAGE_TYPE_text, "", 1, "", 0);
        BriefMessagePojo briefMessagePojo3 = new BriefMessagePojo(10, "p_10", "测试用户", "测试消息提示", "http://image.dev.dajiazhongyi.com/studio/image.jpg",
                System.currentTimeMillis(), false, Common.MESSAGE_TYPE_text, "", 1, "", 0);
        this.briefMessageList.add(briefMessagePojo1);
        this.briefMessageList.add(briefMessagePojo2);
        this.briefMessageList.add(briefMessagePojo3);

        int index = 0;
        for (Iterator<BriefMessagePojo> iter = briefMessageList.iterator(); iter.hasNext(); index++) {
            BriefMessagePojo bm = iter.next();
            briefMessageIndex.put(bm.getUserId(), index);
            List<PatientMessage> pmList = patientMessageInfoService.linkedListLastKPatientMessage(bm.getUserId(),
                    Common.PATIENT_MESSAGE_NUM);
            patientMessageMap.put(bm.getUserId(), pmList);
            lockMap.put(bm.getUserId(), new ReentrantReadWriteLock(true));
        }
        chatClient = MyApplication.chatClient;
        spManager = SharedpreferenceManager.getInstance();
        atnPatientIdMap = patientDocRelationService.getPatientIdMapByDoctroId(spManager.getOne("keySid"));
        List<Label> labels = labelService.listLabels();
        for (Iterator<Label> iter = labels.iterator(); iter.hasNext(); ) {
            Label l = iter.next();
            labelIdMap.put(l.getId(), l);
            allLabelColors.add(l.getLabelColor());
        }
        this.setNewPatientNum();
        Log.d("RDCDebug", "<init> briefMessage #:" + briefMessageList.size() + ", PatientMessageList #:"
                + patientMessageMap.size());
        String quietTmp = spManager.getOne("keyQuiet");
        if (quietTmp.isEmpty() || quietTmp.equals("0")) {
            this.setQuiet(false);
        } else {
            this.setQuiet(true);
        }
        List<PatientMessage> resendPaymentList = patientMessageInfoService.listPaymentUnsuccess();
        for (Iterator<PatientMessage> iter = resendPaymentList.iterator(); iter.hasNext(); ) {
            PatientMessage pm = iter.next();
            this.sendMessage(pm, pm.getToID(), true);
        }
        return 0;
    }

    public void destory() {
        Logger.d("RDC destory", "destoried");
        instance = null;
        System.gc();
        //test to do
        spManager.setOne("keyLatestTime", "0");
        spManager.setOne("keyAtnLatestTime", "0");
    }

    // patient message相关cache中信息重新init
    private int reInitPatientMessage(String userId) {
        if (userId.isEmpty()) {
            return -1;
        }
        reInitLock.writeLock().lock();
        Log.d("RDCDebug", "<reInitPatientMessage> userId:" + userId);
        if (lockMap.containsKey(userId) || patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug", "<reInitPatientMessage> reInit FAIL! Already exist!");
            return -1;
        }
        // reinit lock
        List<PatientMessage> pmList = patientMessageInfoService.linkedListLastKPatientMessage(userId,
                Common.PATIENT_MESSAGE_NUM);
        patientMessageMap.put(userId, pmList);
        lockMap.put(userId, new ReentrantReadWriteLock(true));
        reInitLock.writeLock().unlock();

        Log.d("RDCDebug", "<reInitPatientMessage> reInit userId:" + userId + "PatientMessage #:" + pmList.size());
        return 0;
    }

    public int sendMessage(final PatientMessage pm, final String userId, final boolean isResend) {
        if (pm == null) {
            Log.d("RDCDebug", "<sendMessage> empty PatientMessage");
            return -1;
        }
        Log.d("RDCDebug", "<sendMessage> userId:" + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID()
                + " content:" + pm.getContent());
        if (userId.isEmpty() || pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<sendMessage> empty PatientMessage or userId value");
            return -1;
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                sendMessageThread(pm, userId, isResend);
            }
        });

        t.start();
        Log.d("RDCDebug", "<sendMessage> send message thread start");
        MobclickAgent.onEvent(MyApplication.context, "doctor_send_message");
        MobclickAgent.onEvent(MyApplication.context, "doctor_send_message_" + pm.getContentType());
        return 0;
    }

    private int sendMessageThread(final PatientMessage pm, final String userId, final boolean isResend) {
        Log.d("RDCDebug", "<sendMessageThread> userId:" + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID()
                + " content:" + pm.getContent());
        if (userId.isEmpty() || pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<sendMessageThread> empty PatientMessage or userId value");
            return -1;
        }

        if (pm.getContentType().equals("image")) {
            final String imagePaths = pm.getContent();
            if (imagePaths.contains("\"thumbnailPath\":\"http://") || imagePaths.contains("\"avaterPath\":\"http://")) {
                sendMessageSocket(pm, userId, imagePaths, isResend);
            } else {
                MessageManager manager = MessageManager.getMessageManager();
                String thumbnailPath = JsonUtil.getJsonStringByKey(imagePaths, Common.KEY_IMAGE_thumbnailPath).toString(), avatarPath = JsonUtil
                        .getJsonStringByKey(imagePaths, Common.KEY_IMAGE_avaterPath).toString();

                if (Common.IsUploadToQiniu) {
                    String imgToken = spManager.getOne("keyQiniuTokenImg");
                    List<File> fileList = new ArrayList<>();
                    fileList.add(BitmapUtil.getFile(avatarPath));
                    // upload avatar to qiniu cloud
                    UploadFileToQiniuMessage qiniuMessage = new UploadFileToQiniuMessage(null, new SimpleMessage.HttpCallBack() {
                        @Override
                        public boolean handle(String result) {
                            Logger.i("RDCDebug", "<sendMessageThread ImageBack> qiniu result" + result);
                            List<String> qiniuKeys = new ArrayList<>();
                            qiniuKeys = (List<String>) JsonUtil.jsonToList(result);
                            HashMap<String, String> map = new HashMap<>();
                            for (String key : qiniuKeys) {
                                map.put(Common.KEY_IMAGE_thumbnailPath, QiniuUtil.getThumbnailUrl(key, QiniuUtil.Type_Image, QiniuUtil.ThumbnailMode200x200));
                                map.put(Common.KEY_IMAGE_avaterPath, QiniuUtil.getDownloadUrl(key, QiniuUtil.Type_Image));
                                break;
                            }
                            String pathJson = JsonUtil.objectToJson(map);
                            Log.d("RDCDebug", "<sendMessageThread ImageBack> qiniu data:" + JsonUtil.objectToJson(map));
                            pm.setContent(pathJson); // 将获取的ImageUrl存到PatientMessage
                            sendMessageSocket(pm, userId, imagePaths, isResend);
                            return true;
                        }
                    }, new SimpleMessage.HttpFailedCallBack() {
                        @Override
                        public boolean handle(int errorCode, String errorMessage) {
                            Log.d("RDCDebug", "<sendMessageThread imageBack> qiniu OnFailed ErrorCode=" + errorCode
                                    + ", ErrorMessage=" + errorMessage + "]");
                            saveSendMessage(pm, userId, false, false);
                            return false;
                        }
                    }, fileList, null, imgToken);
                    manager.sendMessage(qiniuMessage);
                } else {
                    // upload avatar
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap avatar = BitmapUtil.getBitmap(avatarPath);
                    avatar.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    ImageMessage message = new ImageMessage(Common.ImageHttpUrl, byteArray, new HttpCallBack() {
                        @Override
                        public boolean handle(String result) {
                            Log.d("RDCDebug", "<sendMessageThread ImageBack> result" + result);
                            Gson gson = new Gson();
                            Type type = new TypeToken<Map<String, Object>>() {
                            }.getType();
                            Map<String, Object> map = gson.fromJson(result, type);
                            int code = (int) Double.parseDouble(String.valueOf(map.get(Common.code)));
                            if (code == 200) {
                                Map<String, Object> dataMap = (Map<String, Object>) map.get(Common.data);
                                String pathJson = JsonUtil.objectToJson(dataMap);
                                Log.d("RDCDebug", "<sendMessageThread ImageBack> data:" + JsonUtil.objectToJson(dataMap));
                                pm.setContent(pathJson); // 将获取的ImageUrl存到PatientMessage
                                sendMessageSocket(pm, userId, imagePaths, isResend);
                            } else {
                                saveSendMessage(pm, userId, false, isResend);
                            }
                            return true;
                        }
                    }, new HttpFailedCallBack() {
                        @Override
                        public boolean handle(int errorCode, String errorMessage) {
                            Log.d("RDCDebug", "<sendMessageThread imageBack> OnFailed ErrorCode=" + errorCode
                                    + ", ErrorMessage=" + errorMessage + "]");
                            // to do send fail
                            saveSendMessage(pm, userId, false, false);
                            return false;
                        }
                    });
                    manager.sendMessage(message);

                }
            }
        } else if (pm.getContentType().equals("voice")) {
            MessageManager manager = MessageManager.getMessageManager();
            String voicePath = pm.getContent();
            if(Common.IsUploadToQiniu){
                String voiceToken = spManager.getOne("keyQiniuTokenVoice");
                List<File> fileList = new ArrayList<>();
                fileList.add(new File(voicePath));
                // upload avatar to qiniu cloud
                UploadFileToQiniuMessage qiniuMessage = new UploadFileToQiniuMessage(null, new SimpleMessage.HttpCallBack() {
                    @Override
                    public boolean handle(String result) {
                        Logger.i("RDCDebug", "<sendMessageThread voiceBack> qiniu result" + result);
                        List<String> qiniuKeys = new ArrayList<>();
                        qiniuKeys = (List<String>) JsonUtil.jsonToList(result);
                        String audioPath = "";
                        for (String key : qiniuKeys) {
                            audioPath = QiniuUtil.getDownloadUrl(key, QiniuUtil.Type_Voice);
                            break;
                        }
                        Log.d("RDCDebug", "<sendMessageThread voiceBack> voicePath:" + audioPath);
                        String localDir = pm.getContent();
                        pm.setContent(audioPath);
                        sendMessageSocket(pm, userId, localDir, isResend);
                        return true;
                    }
                }, new SimpleMessage.HttpFailedCallBack() {
                    @Override
                    public boolean handle(int errorCode, String errorMessage) {
                        Log.d("RDCDebug", "<sendMessageThread voiceBack> qiniu OnFailed ErrorCode=" + errorCode
                                + ", ErrorMessage=" + errorMessage + "]");
                        saveSendMessage(pm, userId, false, false);
                        return false;
                    }
                }, fileList, null, voiceToken);
                manager.sendMessage(qiniuMessage);
            }else {
                byte[] voiceByte = null;
                try {
                    voiceByte = VoiceUtil.readAudio(voicePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                VoiceMessage message = new VoiceMessage(Common.VoiceHttpUrl, voiceByte, new HttpCallBack() {

                    @Override
                    public boolean handle(String result) {
                        Log.d("RDCDebug", "<sendMessageThread voiceBack> result" + result);
                        Gson gson = new Gson();
                        Type type = new TypeToken<Map<String, Object>>() {
                        }.getType();
                        Map<String, Object> map = gson.fromJson(result, type);
                        int code = (int) Double.parseDouble(String.valueOf(map.get(Common.code)));
                        if (code == 200) {
                            Map<String, Object> dataMap = (Map<String, Object>) map.get(Common.data);
                            String audioPath = (String) dataMap.get(Common.KEY_VOICE_voicePath);
                            Log.d("RDCDebug", "<sendMessageThread voiceBack> voicePath:" + audioPath);
                            String localDir = pm.getContent();
                            pm.setContent(audioPath);
                            sendMessageSocket(pm, userId, localDir, isResend);
                        } else {
                            saveSendMessage(pm, userId, false, isResend);
                        }
                        return true;
                    }
                }, new HttpFailedCallBack() {

                    @Override
                    public boolean handle(int errorCode, String errorMessage) {
                        Log.d("RDCDebug", "<sendMessageThread> OnFailed ErrorCode=" + errorCode + ", ErrorMessage="
                                + errorMessage + "]");
                        saveSendMessage(pm, userId, false, false);
                        return false;
                    }
                });
                manager.sendMessage(message);
            }
        } else {
            sendMessageSocket(pm, userId, null, isResend);
        }
        return 0;
    }

    private int sendMessageSocket(PatientMessage pm, String userId, String localDir, boolean isResend) {
        Log.d("RDCDebug", "<sendMessageSocket> userId:" + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID()
                + " content:" + pm.getContent());
        if (userId.isEmpty() || pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<sendMessageSocket> empty PatientMessage or userId value");
            return -1;
        }
        if (pm.getContentType().equals("voice") && localDir.isEmpty()) {
            Log.d("RDCDebug", "<sendMessageSocket> empty localDir for voice message");
            return -1;
        }

        saveSendMessage(pm, userId, true, isResend);

        int tryCount = this.sendTryCount;
        boolean isSuccess = false;
        while (tryCount > 0) {
            if (!pm.getContentType().equals(Common.MESSAGE_TYPE_paymentRequest)) {
                tryCount--;
            }
            chatClient = MyApplication.chatClient;
            if (chatClient == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.d("RDCDebug", "<sendMessageSocket> chatClient is not ready, remain " + tryCount + " try");
            } else {
                //isSuccess = true;
                break;
            }
        }
		/*
		if(!isSuccess){
			saveSendMessage(pm, userId, false, isResend);
			Log.d("RDCDebug", "<sendMessageSocket> send FAIL! chatClient is not ready");
			return -1;
		}
		isSuccess = false;*/
        Map<Object, Object> jsonMap = new HashMap<Object, Object>();
        PatientMessageDTO patientMessageDTO = DataAdapter.patientMessageAdapter(pm);
        jsonMap.put("type", Common.type_chatmessage);
        jsonMap.put("data", patientMessageDTO);
        while (tryCount > 0) {
            tryCount--;
            if (chatClient.sendMessage(JsonUtil.objectToJson(jsonMap)) != 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.d("RDCDebug", "<sendMessageSocket> send FAIL, remain " + tryCount + " try");
            } else {
                if (pm.getContentType().equals("voice") || pm.getContentType().equals("image")) {
                    pm.setContent(localDir); // 如果是voice或者图片消息，设置为本地路径
                }
                //saveSendMessage(pm, userId, true, isResend);
                MessageTimer mt = MessageTimer.getInstance();
                final PatientMessage final_pm = pm;
                final String final_userId = userId;
                //final boolean final_isResend = isResend;
                mt.begin(final_pm.getGuid(), 30, new MessageTimer.RTOHandler() {
                    @Override
                    public void handle() {
                        if (patientMessageSent.contains(final_pm.getGuid())) {
                            patientMessageSent.remove(final_pm.getGuid());
                        } else {
                            Log.d("RDCDebug", "<sendMessageSocket> patientMessageSent contains no key");
                        }
                        saveSendMessage(final_pm, final_userId, false, true);
                    }
                });
                isSuccess = true;
                break;
            }
        }
        if (!isSuccess) {
            saveSendMessage(pm, userId, false, true);
            Log.d("RDCDebug", "<sendMessageSocket> send FAIL! chatClient is not ready");
            return -1;
        }
        return 0;
    }

    //if success, send status = 0, if fail, send status = -1
    private int saveSendMessage(PatientMessage pm, String userId, boolean isSuccess, boolean isResend) {
        Log.d("RDCDebug", "<saveSendMessage> userId:" + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID()
                + " content:" + pm.getContent() + "isSuccess:" + Boolean.toString(isSuccess));
        if (userId.isEmpty() || pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<saveSendMessage> empty PatientMessage or userId value");
            return -1;
        }

        if (!patientMessageMap.containsKey(userId)) {
            this.reInitPatientMessage(userId);
        }

        int sendStatus = 0;
        if (!isSuccess) {
            sendStatus = -1; // 不更改的话默认为0
        }

        if (isResend) { // 重发
            PatientMessage newPm = new PatientMessage(pm);
            newPm.setSendStatus(sendStatus);
            if (isSuccess) {
                patientMessageSent.put(newPm.getGuid(), newPm);
            }
            this.updatePatientMessage(newPm, userId);
            BriefMessagePojo newBm = new BriefMessagePojo(this.getBriefMessageOne(userId));
            if (newBm.getpGuid().equals(pm.getGuid())) {
                newBm.setSendStatus(sendStatus);
                this.updateBriefMessage(newBm);
            }
        } else {
            pm.setSendStatus(sendStatus);
            if (isSuccess) {
                patientMessageSent.put(pm.getGuid(), pm);
            }
            checkToUpdateNewMessageTimeStamp(pm, userId);
            savePatientMessage(pm, userId);
            saveBriefMessage(pm, userId, true);
            Log.d("RDCDebug",
                    "<saveSendMessage> add message to patientMessageSent, userId:" + userId + ", from:"
                            + pm.getFromID() + ", to:" + pm.getToID() + " content:" + pm.getContent());
        }
        if (handlerManager.getMainHandler() != null) {
            Message msg = new Message();
            msg.what = Common.MSG_WHAT_BRIEFCHANGE_MESSAGE;
            handlerManager.getMainHandler().sendMessage(msg);
            Log.d("RDCDebug", "<notifyBriefMessage> notify MSG_WHAT_BRIEFCHANGE_MESSAGE MainHandler");
        } else {
            Log.d("RDCDebug", "<notifyBriefMessage> handleManager.getMainHandler() == null");
            return -1;
            // to do
        }
        if (this.isInMessageActivity()) {
            notifyBriefMessageChange();
        }
        if (sendStatus == -1 && this.isInChatActivity(userId)) {
            if (handlerManager.getChatHandler() != null) {
                Message msg = new Message();
                msg.what = Common.MSG_WHAT_SEND_FAILED;
                msg.obj = pm.getGuid();
                handlerManager.getChatHandler().sendMessage(msg);
                Log.d("RDCDebug",
                        "<receiveMessage> notify MSG_WHAT_CHAT_MESSAGE_RECEIVE message"
                                + ", from:" + pm.getFromID() + ", to:" + pm.getToID() + ", content:"
                                + pm.getContent());
            } else {
                Log.d("RDCDebug", "<receiveMessage> handleManager.getChatHandler() == null");
                // to do
            }
        }
        return 0;
    }

    public int sendSynMessage() {
        Log.d("RDCDebug", "<sendSynMessage> ready to send syn message");
        SynMessageDTO synMessageDTO = new SynMessageDTO(spManager.getOne("keyLatestTime"));
        Log.d("RDCDebug", "<sendSynMessage> timestamp:" + spManager.getOne("keyLatestTime"));
        chatClient = MyApplication.chatClient;
        if (chatClient != null) {
            Map<Object, Object> jsonMap = new HashMap<Object, Object>();
            jsonMap.put("type", Common.type_synmessage);
            jsonMap.put("data", synMessageDTO);
            chatClient.sendMessage(JsonUtil.objectToJson(jsonMap));
            Log.d("RDCDebug", "<sendSynMessage> send syn message");
        } else {
            // to do
            Log.d("RDCDebug", "<sendSynMessage> send syn message FAIL!");
            return -1;
        }
        return 0;
    }

    public int sendSynAtnMessage() {
        Log.d("RDCDebug", "<sendSynAtnMessage> ready to send syn attention message");
        SynAtnMessageDTO synAtnMessageDTO = new SynAtnMessageDTO(spManager.getOne("keyAtnLatestTime"));
        Log.d("RDCDebug", "<sendSynAtnMessage> timestamp:" + spManager.getOne("keyAtnLatestTime"));
        chatClient = MyApplication.chatClient;
        if (chatClient != null) {
            Map<Object, Object> jsonMap = new HashMap<Object, Object>();
            jsonMap.put("type", Common.type_synatnmessage);
            jsonMap.put("data", synAtnMessageDTO);
            chatClient.sendMessage(JsonUtil.objectToJson(jsonMap));
            Log.d("RDCDebug", "<sendSynAtnMessage> send syn atn message: " + JsonUtil.objectToJson(jsonMap));
        } else {
            // to do
            Log.d("RDCDebug", "<sendSynAtnMessage> send syn atn message FAIL!");
        }
        return 0;
    }

    public int receiveMessage(PatientMessage pm) {
        Log.d("RDCDebug",
                "<receiveMessage> from:" + pm.getFromID() + " to:" + pm.getToID() + " content:" + pm.getContent());
        if (pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<receiveMessage> empty PatientMessage value");
            return -1;
        }
        String curUserId = this.getUserId();
        if (curUserId == null) {
            curUserId = "";
        }

        // feedback message
        if (patientMessageSent.containsKey(pm.getGuid())) {
            Log.d("RDCDebug", "<receiveMessage> feedback message");
            if (handlerManager.getChatHandler() != null) {
                MessageTimer mt = MessageTimer.getInstance();
                mt.stop(pm.getGuid());
                PatientMessage updatePm = new PatientMessage(patientMessageSent.get(pm.getGuid()));
                updatePm.setSendStatus(1);
                patientMessageSent.remove(pm.getGuid()); // 异常处理见else
                Message msg = new Message();
                msg.what = Common.MSG_WHAT_CHAT_MESSAGE_SERVER_FEEDBACK;
                msg.obj = pm.getGuid();
                handlerManager.getChatHandler().sendMessage(msg);
                Log.d("RDCDebug", "<receiveMessage> notify MSG_WHAT_CHAT_MESSAGE_SERVER_FEEDBACK message");
                String userId = DataAdapter.getUserId(updatePm);
                Log.d("RDCDebug", "<receiveMessage> feedback message content: " + updatePm.getContent());
                this.updatePatientMessage(updatePm, userId);
                briefMessageLock.writeLock().lock();
                if (briefMessageIndex.containsKey(userId)) {
                    int index = briefMessageIndex.get(userId);
                    if (briefMessageList.get(index).getpGuid().equals(pm.getGuid())) {
                        BriefMessagePojo updateBm = new BriefMessagePojo(briefMessageList.get(index));
                        updateBm.setSendStatus(1);
                        this.updateBriefMessage(updateBm);
                    }
                }
                briefMessageLock.writeLock().unlock();

                timeStampLock.writeLock().lock();
                long latestTimeStamp = Long.valueOf(spManager.getOne("keyLatestTime"));
                if (latestTimeStamp > pm.getTimestamp()) {
                    Log.d("RDCDebug",
                            "<receiveMessage> update lastestTimeStamp exception: lastestTimeStamp is smaller than patientMessage Time");
                } else {
                    latestTimeStamp = pm.getTimestamp(); // 只用服务器的时间来更新
                    spManager.setOne("keyLatestTime", Long.toString(latestTimeStamp));
                }
                timeStampLock.writeLock().unlock();
            } else {
                // to do
                Log.d("RDCDebug", "<notifyBriefMessage> handleManager.getChatHandler() == null");
                // deal with exception
            }
            return 0;
        }

        // 判断患者是否关注
        String msgUserId = DataAdapter.getUserId(pm);
        if (checkUpdatePatientAttention(msgUserId) != 0) {
            Log.d("RDCDebug", "<receiveMessage> no such Patient");
            return -1;
        }

        timeStampLock.writeLock().lock();
        long latestTimeStamp = Long.valueOf(spManager.getOne("keyLatestTime"));
        if (latestTimeStamp > pm.getTimestamp()) {
            Log.d("RDCDebug",
                    "<receiveMessage> update lastestTimeStamp exception: lastestTimeStamp is smaller than patientMessage Time");
        } else {
            latestTimeStamp = pm.getTimestamp(); // 只用服务器的时间来更新
            spManager.setOne("keyLatestTime", Long.toString(latestTimeStamp));
        }
        timeStampLock.writeLock().unlock();

        // receive message
        pm.setSendStatus(1); // 设置发送状态:已发送
        if (!patientMessageMap.containsKey(msgUserId)) {
            this.reInitPatientMessage(msgUserId);
        }

        if (pm.getContentType().equals(Common.MESSAGE_TYPE_image)) {
            String imagePaths = pm.getContent();
            String thumbnailPath = JsonUtil.getJsonStringByKey(imagePaths, "thumbnailPath").toString();
            if (ImageLoader.getInstance().loadImageSync(thumbnailPath) == null) {
                Log.d("RDCDebug", "<receiveMessage> save thumbnail FAIL!");
            }
            Log.d("RDCDebug", "<receiveMessage> save thumbnail success");
            pm.setContent(imagePaths); // save url
        } else if (pm.getContentType().equals(Common.MESSAGE_TYPE_voice)) {
            String localDir = VoiceUtil.saveAudioByUrl(pm.getContent());
            pm.setContent(localDir); // save local directory
            Log.d("RDCDebug", "<receiveMessage> save audio to :" + localDir);
        } else if (pm.getContentType().equals(Common.MESSAGE_TYPE_paymentSuccess)) {
//			if(handlerManager.getPaymentHandler() != null){
//				Log.d("RDCDebug", "<receiveMessage> send to patient handler");
//				Message msg = new Message();
//				msg.what = Common.MSG_WHAT_PAYMENT_SUCCESSFUL;
//				msg.obj = null;
//				handlerManager.getPaymentHandler().sendMessage(msg);
//			}
            if (handlerManager.getMainHandler() != null) {
                Log.d("RDCDebug", "<receiveMessage> send to mainHandler");
                Message msg = new Message();
                msg.what = Common.MSG_WHAT_PAYMENT_SUCCESSFUL;
                msg.obj = null;
                handlerManager.getMainHandler().sendMessage(msg);
                try {
                    MobclickAgent.onEventValue(MyApplication.context, "doctor_payment_success", null, new JSONObject(pm.getContent()).getInt("fee"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        checkToUpdateNewMessageTimeStamp(pm, msgUserId);
        savePatientMessage(pm, msgUserId);
        BriefMessagePojo bm = saveBriefMessage(pm, msgUserId, false);

        // Notify
        if (HandlerManager.getInstance().getMainHandler() != null) {
            Message msg = new Message();
            msg.what = Common.MSG_WHAT_BRIEFCHANGE_MESSAGE;
            handlerManager.getMainHandler().sendMessage(msg);
            Log.d("RDCDebug", "<notifyBriefMessage> notify MSG_WHAT_BRIEFCHANGE_MESSAGE messasge");
        } else {
            Log.d("RDCDebug", "<notifyBriefMessage> handleManager.getMainHandler() == null");
            return -1;
            // to do
        }
        if (this.isInMessageActivity()) {
            Log.d("RDCDebug", "<receiveMessage> current activity: MessageActivity");
            notifyBriefMessageChange();
        } else if (this.isInChatActivity(msgUserId)) {
            Log.d("RDCDebug", "<receiveMessage> current activity: ChatActivity");
            if (curUserId.equals(msgUserId)) {
                if (handlerManager.getChatHandler() != null) {
                    Message msg = new Message();
                    msg.what = Common.MSG_WHAT_CHAT_MESSAGE_RECEIVE;
                    msg.obj = pm;
                    handlerManager.getChatHandler().sendMessage(msg);
                    Log.d("RDCDebug",
                            "<receiveMessage> notify MSG_WHAT_CHAT_MESSAGE_RECEIVE message, userId:" + msgUserId
                                    + ", from:" + pm.getFromID() + ", to:" + pm.getToID() + ", content:"
                                    + pm.getContent());
                } else {
                    Log.d("RDCDebug", "<receiveMessage> handleManager.getChatHandler() == null");
                    // to do
                }
            } else {
                // notify to bar
                if (!isQuiet) {
                    Log.d("RDCDebug", "<receiveMessage> curUserId != msgUserId");
                    NotifyManager.getManager().notifyMessage(bm);
                    Log.d("RDCDebug", "<receiveMessage> notify briefMessage to bar");
                }
            }
        } else {
            // notify to bar
            if (!isQuiet) {
                Log.d("RDCDebug", "<receiveMessage> current activity: not MessageActivity and not ChatActivity");
                NotifyManager.getManager().notifyMessage(bm);
                Log.d("RDCDebug", "<receiveMessage> notify briefMessage to bar");
            }
        }

        return 0;
    }

    public int receiveAtnMessage(final Patient p, final NewPatient np, final int status) {
        Log.d("RDCDebug", "<receiveAtnMessage> in receiveAtnMessage");
        if (p.getPatientId().isEmpty() || np.getPatientId().isEmpty()) {
            Log.d("RDCDebug", "<receiveAtnMessage> empty patient, empty newpatient");
        }

        if (atnPatientIdMap.containsKey(p.getPatientId())) {
            Log.d("RDCDebug", "<receiveAtnMessage> already exists");
            return -1;
        }

        // atnTimeStampLock.writeLock().lock();
        long atnLatestTimeStamp = Long.valueOf(spManager.getOne("keyAtnLatestTime"));
        if (atnLatestTimeStamp > np.getFollowTime()) {
            Log.d("RDCDebug",
                    "<receiveAtnMessage> update atnLastestTimeStamp exception: atnLastestTimeStamp is larger than newPatient Time");
        } else {
            atnLatestTimeStamp = np.getFollowTime();// 只用服务器的时间来更新
            spManager.setOne("keyAtnLatestTime", Long.toString(atnLatestTimeStamp));
        }
        // atnTimeStampLock.writeLock().unlock();

        // patientAtnLock.writeLock().lock();
        // 回滚策略，数据库异常处理
        if (status == 1) {
            patientInfoService.addPatientInfoOne(p);
            NewPatient retNp = newPatientInfoService.viewNewPatientInfoById(p.getPatientId());
            if (retNp == null) {
                newPatientInfoService.addNewPatientInfoOne(np);
            } else {
                newPatientInfoService.updateStatusNewPatientInfoOne(p.getPatientId(), 1);
            }
            PatientDoctorRel retPdr = patientDocRelationService.viewRelationInfo(spManager.getOne("keySid"),
                    p.getPatientId());
            if (retPdr == null) {
                PatientDoctorRel pdr = new PatientDoctorRel();
                pdr.doctorId = spManager.getOne("keySid");
                pdr.patientId = p.getPatientId();
                pdr.status = 1;
                pdr.time = np.getFollowTime();
                patientDocRelationService.addRelationInfoOne(pdr);
            } else {
                patientDocRelationService.updateStatus(spManager.getOne("keySid"), p.getPatientId(), 1);
            }
            plusNewPatientNum(1);
            atnPatientIdMap.put(p.getPatientId(), true);
        } else if (status == 0) {
            if (!patientDocRelationService.updateStatus(spManager.getOne("keySid"), p.getPatientId(), 0)) {
                Log.d("RDCDebug", "<receiveAtnMessage> delete FAIL : has no patient doctor relation");
            }
            if (!newPatientInfoService.updateStatusNewPatientInfoOne(p.getPatientId(), 0)) {
                Log.d("RDCDebug", "<receiveAtnMessage> update newPatientInfo status FAIL");
            }
			/*
			if(!patientInfoService.deletePatientInfo(p.getPatientId())){
				Log.d("RDCDebug", "<receiveAtnMessage> delete patientInfo status FAIL");
			}
			if(!recordService.deleteRecord(p.getPatientId())){
				Log.d("RDCDebug", "<receiveAtnMessage> delete record FAIL");
			}
			if(!patientLabelRelationService.deleteRelationIfoByPatientId(p.getPatientId())) {
				Log.d("RDCDebug", "<receiveAtnMessage> delete patientLabelRelation FAIL");
			}
			//to do, delete messages
			int index = briefMessageIndex.get(p.getPatientId());
			BriefMessagePojo bm = briefMessageList.get(index);
			*/

            minusNewPatientNum(1);
            atnPatientIdMap.remove(p.getPatientId());
            // to do 回滚策略
        }
        // patientAtnLock.writeLock().unlock();
        Message msg = new Message();
        msg.obj = null;
        msg.what = Common.MSG_WHAT_PATIENTCOUNTCHANGE_MESSAGE;
        if (handlerManager.getMainHandler() != null) {
            handlerManager.getMainHandler().sendMessage(msg);
        }

        Message msg2 = new Message();
        msg2.obj = null;
        msg2.what = Common.MSG_WHAT_PATIENTCOUNTCHANGE_MESSAGE;
        if (handlerManager.getPatientHandler() != null) {
            handlerManager.getPatientHandler().sendMessage(msg2);
        }
        return 0;
    }

    private int checkUpdatePatientAttention(String patientId) {
        Log.d("RDCDebug", "<checkUpdatePatientAttention> patientId:" + patientId);
        if (patientId.isEmpty()) {
            Log.d("RDCDebug", "<checkUpdatePatientAttention> empty patientId");
            return -1;
        }
		/*
		 * int count = 5; while(!atnPatientIdMap.containsKey(patientId) && count
		 * >= 0){ try { Thread.sleep(4000); count--; } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } }
		 */
        if (atnPatientIdMap.containsKey(patientId)) {
            Log.d("RDCDebug", "<checkUpdatePatientAttention> patient exists");
            return 0;
        } else {
            Log.d("RDCDebug", "<checkUpdatePatientAttention> not exist!");
            return -1;
        }
    }

    private int checkToUpdateNewMessageTimeStamp(PatientMessage pm, String userId) {
        if (userId.isEmpty() || pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<checkNewMessageTimeStamp> empty userId or emtpy pm");
        }
        briefMessageLock.readLock().lock();
        if (!briefMessageIndex.containsKey(userId)) {
            Log.d("RDCDebug", "<checkNewMessageTimeStamp> briefMessageIndex has no key");
            briefMessageLock.readLock().unlock();
            return 0;
        }
        int index = briefMessageIndex.get(userId);
        BriefMessagePojo bm = briefMessageList.get(index);
        if (pm.getTimestamp() < bm.getTime()) {
            pm.setTimestamp(bm.getTime());
        }
        briefMessageLock.readLock().unlock();
        return 0;
    }

    private int savePatientMessage(PatientMessage pm, String userId) {
        if (pm.getGuid().isEmpty() || userId.isEmpty() || !lockMap.containsKey(userId)) {
            Log.d("RDCDebug", "<savePatientMessage> empty PatientMessage or userId value or lockMap has no key");
            return -1;
        }
        ReentrantReadWriteLock lock = lockMap.get(userId);
        if (lock == null) {
            Log.d("RDCDebug", "<savePatientMessage> emtpy lock");
            return -1;
        }
        PatientMessageTask pmt = new PatientMessageTask("add", pm, userId, lock);
        Thread t = new Thread(pmt);
        t.start();
        Log.d("RDCDebug",
                "<savePatientMessage> start PatientMessageTask single add, userId:" + userId + ", from:"
                        + pm.getFromID() + ", to:" + pm.getToID() + " content:" + pm.getContent());
        return 0;
    }

    private BriefMessagePojo saveBriefMessage(PatientMessage pm, String msgUserId, boolean isSend) {
        if (pm.getGuid().isEmpty() || msgUserId.isEmpty()) {
            Log.d("RDCDebug", "<saveBriefMessage> empty PatientMessage or userId value");
            return null;
        }
        String key = msgUserId;
        BriefMessagePojo bm;
        int status; // 1: update, 2: add

        // save to cache
        briefMessageLock.writeLock().lock();
        if (briefMessageIndex.containsKey(key)) {
            int index = briefMessageIndex.get(key);
            bm = briefMessageList.get(index);
            DataAdapter.updateBriefMessageByNewPatientMessage(bm, pm, isSend);
            if (this.isInChatActivity(key)) {
                bm.setCount(0); // 如果在chatActivity里面，则来信息不增加count值
            }
            Log.d("RDCDebug", "<saveBriefMessage> update, set message to " + bm.getMessage());
            status = 1;
        } else {
            Patient p = patientInfoService.viewPatientInfoByPatientId(msgUserId);
            List<Long> labelIds = patientLabelRelationService.listLabelByPatientId(msgUserId);
            String labelBmString = "";
            for (Iterator<Long> iter = labelIds.iterator(); iter.hasNext(); ) {
                long labelId = iter.next();
                //Label label = labelService.getLabelById(labelId);
                //labelBmString += label.getLabelName() + "&";
                labelBmString += labelId + "&";
            }
            String cutLabelBmString = labelBmString;
            if (!labelBmString.isEmpty()) {
                cutLabelBmString = labelBmString.substring(0, labelBmString.length() - 1);
            }
            boolean ifNoDisturbing = true; // 初始状态不设置免打扰
            int isDraft = 0; // 初始状态不设置草稿
            bm = DataAdapter.briefMsgAdapter(pm, p.getThumbnail(), ifNoDisturbing, p.getName(), cutLabelBmString,
                    isDraft);
            briefMessageList.add(bm);
            briefMessageIndex.put(key, briefMessageList.size() - 1);
            Log.d("RDCDebug", "<saveBriefMessage> add message, userId:" + msgUserId + ", from:" + pm.getFromID()
                    + ", to:" + pm.getToID() + " content:" + pm.getContent());
            status = 2;
        }

        // 修改cache
        int index = briefMessageIndex.get(key);
        Log.d("RDCDebug", "<saveBriefMessage> before move briefMessage location:" + Integer.toString(index));
        BriefMessagePojo p = bm, pre;
        for (int i = index - 1; i >= 0; i--) {
            pre = briefMessageList.get(i);
            if (pre.getTime() <= bm.getTime()) {
                briefMessageIndex.put(pre.getUserId(), index);
                briefMessageList.set(index, pre);
                index = i;
            }
        }
        briefMessageIndex.put(key, index);
        briefMessageList.set(index, bm);

        briefMessageLock.writeLock().unlock();
        // save to db
        BriefMessageTask bmt;
        if (status == 1) {
            bmt = new BriefMessageTask("update", bm);
            Log.d("RDCDebug", "<savePatientMessage> start BriefMessageTask single update, userId:" + bm.getUserId()
                    + ", userName:" + bm.getUserName() + ", content:" + bm.getMessage());
        } else {
            bmt = new BriefMessageTask("add", bm);
            Log.d("RDCDebug", "<savePatientMessage> start BriefMessageTask single add, userId:" + bm.getUserId()
                    + ", userName:" + bm.getUserName() + ", content:" + bm.getMessage());
        }
        Thread t = new Thread(bmt);
        t.start();

        return bm;
    }

    /*
     * PatientMessage
     */
    // 调用，以获得PatientMessageList
    public List<PatientMessage> getPatientMessage(String userId) {
        if (userId.isEmpty() || !lockMap.containsKey(userId) || !patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug",
                    "<getPatientMessage> userId==null or lockMap contains no key or patientMessageMap contains no key");
            return null;
        }
        ReentrantReadWriteLock lock = lockMap.get(userId);
        lock.readLock().lock();
        List<PatientMessage> list = new ArrayList<PatientMessage>(patientMessageMap.get(userId));
        lock.readLock().unlock();
        Log.d("RDCDebug", "<getPatientMessage> userId:" + userId + ", # of items:" + list.size());
        return list;
    }

    // 调用，以再得到若干个PatientMessage
    public List<PatientMessage> getPatientMessageContinue(String userId, String guid) {
        if (userId.isEmpty() || !lockMap.containsKey(userId)) {
            Log.d("RDCDebug", "<getPatientMessageContinue> userId==null or lockMap contains no key");
            return null;
        }
        ReentrantReadWriteLock lock = lockMap.get(userId);
        lock.readLock().lock();
        List<PatientMessage> list = patientMessageInfoService.listLastKPatientMessageBeforeId(userId,
                Common.PATIENT_MESSAGE_NUM, guid);
        lock.readLock().unlock();
        // Log.d("RDCDebug", "<getPatientMessageContinue> userId:" + userId +
        // ", timestamp:" + timestamp + ", # of items:" + list.size());
        Log.d("RDCDebug",
                "<getPatientMessageContinue> userId:" + userId + ", guid:" + guid + ", # of items:" + list.size());
        return list;
    }

    // 调用，以更新PatientMessage
    public int updatePatientMessage(PatientMessage pm, String userId) {
        if (pm.getGuid().isEmpty() || userId.isEmpty() || !lockMap.containsKey(userId)) {
            Log.d("RDCDebug",
                    "<updatePatientMessage> empty PatientMessage guid or empty userId or lockMap contains no key");
            return -1;
        }
        ReentrantReadWriteLock lock = lockMap.get(userId);
        if (lock == null) {
            Log.d("RDCDebug", "<updatePatinetMessage> empty lock");
            return -1;
        }
        PatientMessageTask pmt = new PatientMessageTask("update", pm, userId, lock);
        Thread t = new Thread(pmt);
        t.start();
        Log.d("RDCDebug", "<updatePatientMessage> start PatientMessageTask single update, userId:" + userId + ", from:"
                + pm.getFromID() + ", to:" + pm.getToID() + " content:" + pm.getContent());
        return 0;
    }

    // 调用，以删除PatientMessage
    public int deletePatientMessage(PatientMessage pm, String userId) {
        if (pm.getGuid().isEmpty() || userId.isEmpty() || !lockMap.containsKey(userId)) {
            Log.d("RDCDebug",
                    "<deletePatientMessage> empty PatientMessage guid or empty userId or lockMap contains no key");
            return -1;
        }
        ReentrantReadWriteLock lock = lockMap.get(userId);
        if (lock == null) {
            Log.d("RDCDebug", "<deletePatientMessage> empty lock");
            return -1;
        }
        PatientMessageTask pmt = new PatientMessageTask("delete", pm, userId, lock);
        Thread t = new Thread(pmt);
        t.start();
        Log.d("RDCDebug", "<deletePatientMessage> start PatientMessageTask single delete, userId:" + userId + ", from:"
                + pm.getFromID() + ", to:" + pm.getToID() + " content:" + pm.getContent());
        return 0;
    }

    private int addPatientMessageList(String userId, PatientMessage pm) {
        Log.d("RDCDebug", "<addPatientMessageList> add message, userId:" + userId + ", from:" + pm.getFromID()
                + ", to:" + pm.getToID() + " content:" + pm.getContent());
        if (userId.isEmpty() || pm.getGuid().isEmpty()) {
            Log.d("RDCDebug", "<addPatientMessageList> empty PatientMessage guid or empty userId");
            return -1;
        }
        if (!patientMessageMap.containsKey(userId)) {
            List<PatientMessage> newList = new LinkedList<PatientMessage>();
            patientMessageMap.put(userId, newList);
        }
        List<PatientMessage> list = patientMessageMap.get(userId);
        list.add(pm);
        if (list.size() > Common.PATIENT_MESSAGE_NUM) {
            list.remove(0);
            Log.d("RDCDebug", "<addPatientMessageList> remove message index of 0");
        }
        patientMessageMap.put(userId, list);
        return 0;
    }

    private int addPatientMessageList(String userId, List<PatientMessage> pmList) {
        Log.d("RDCDebug", "<addPatientMessageList> add messagessss, #:" + pmList.size());
        if (userId.isEmpty() || pmList.isEmpty()) {
            Log.d("RDCDebug", "<addPatientMessageList> empty PatientMessageList or empty userId");
            return -1;
        }
        if (!patientMessageMap.containsKey(userId)) {
            List<PatientMessage> newList = new LinkedList<PatientMessage>();
            patientMessageMap.put(userId, newList);
        }
        List<PatientMessage> list = patientMessageMap.get(userId);
        list.addAll(pmList);
        int removeCount = list.size() - Common.PATIENT_MESSAGE_NUM;
        for (int i = 0; i < removeCount; i++) {
            list.remove(0);
        }
        Log.d("RDCDebug", "<addPatientMessageList> remove messagessss, #:" + removeCount);
        patientMessageMap.put(userId, list);
        return 0;
    }

    private int insertHeadPatientMessageList(String userId, PatientMessage pm) {
        Log.d("RDCDebug",
                "<insertHeadPatientMessageList> insert message, userId:" + userId + ", from:" + pm.getFromID()
                        + ", to:" + pm.getToID() + " content:" + pm.getContent());
        if (userId.isEmpty() || pm.getGuid().isEmpty() || !patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug",
                    "<insertHeadPatientMessageList> empty userId or empty PatientMessage or PatientMessageMap has no key");
            return -1;
        }
        List<PatientMessage> list = patientMessageMap.get(userId);
        list.add(0, pm);
        patientMessageMap.put(userId, list);
        return 0;
    }

    // 0:找到 -1:没有找到
    // PatientMessage由于与db一起操作，所以这里不加锁
    private int updatePatientMessageList(String userId, PatientMessage pm) {
        Log.d("RDCDebug", "<updatePatientMessageList> update message, userId:" + userId + ", from:" + pm.getFromID()
                + ", to:" + pm.getToID() + " content:" + pm.getContent());
        if (userId.isEmpty() || !patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug", "<updatePatientMessageList> empty userId or no such key in patientMessageMap");
            return -1;
        }
        List<PatientMessage> oldList = patientMessageMap.get(userId);
        int index = 0;
        for (Iterator<PatientMessage> iter = oldList.iterator(); iter.hasNext(); index++) {
            if (iter.next().getGuid().equals(pm.getGuid())) {
                oldList.set(index, pm);
                return 0;
            }
        }
        Log.d("RDCDebug", "<updatePatientMessageList> NOT FOUND message!");
        return -1;
    }

    // 0:找到 -1:没有找到
    private int updatePatientMessageList(String userId, List<PatientMessage> pmList) {
        Log.d("RDCDebug", "<updatePatientMessageList> update messagessss, userId:" + userId + ", #:" + pmList.size());
        if (userId.isEmpty() || !patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug", "<updatePatientMessageList> empty userId or no such key in patientMessageMap");
            return -1;
        }
        List<PatientMessage> oldList = patientMessageMap.get(userId);
        HashMap<String, PatientMessage> map = new HashMap<String, PatientMessage>();
        for (Iterator<PatientMessage> iter = pmList.iterator(); iter.hasNext(); ) {
            PatientMessage pm = iter.next();
            map.put(pm.getGuid(), pm);
        }

        int index = 0, flag = -1;
        for (Iterator<PatientMessage> iter = oldList.iterator(); iter.hasNext(); index++) {
            PatientMessage pm = iter.next();
            if (map.containsKey(pm.getGuid())) {
                oldList.set(index, map.get(pm.getGuid()));
                flag = 0;
            }
        }
        if (flag == -1) {
            Log.d("RDCDebug", "<updatePatientMessageList> NOT FOUND message!");
        }
        return flag;
    }

    private int deletePatientMessageList(String userId, PatientMessage pm) {
        Log.d("RDCDebug", "<deletePatientMessageList> delete message, userId:" + userId + ", from:" + pm.getFromID()
                + ", to:" + pm.getToID() + " content:" + pm.getContent());
        if (userId.isEmpty() || pm.getGuid().isEmpty() || !patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug",
                    "<deletePatientMessageList> empty userId or empty PatientMessageList or patientMessageMap has no key");
            return -1;
        }
        List<PatientMessage> oldList = patientMessageMap.get(userId);
        int index = 0;
        for (Iterator<PatientMessage> iter = oldList.iterator(); iter.hasNext(); index++) {
            if (iter.next().getGuid().equals(pm.getGuid())) {
                Log.d("RDCDebug", "<deletePatientMessageList> remove index:" + index);
                oldList.remove(index);
                if (index == oldList.size()) {
                    Log.d("RDCDebug", "<deletePatientMessageList> remove the last one");
                    BriefMessagePojo bm = new BriefMessagePojo(this.getBriefMessageOne(userId));
                    if (index != 0) {
                        DataAdapter.updateBriefMessageByPreviousPatientMessage(bm, oldList.get(index - 1));
                    } else {
                        bm.setContentType("text");
                        bm.setCount(0);
                        bm.setFaceImageUrl("");
                        bm.setMessage("");
                        bm.setpGuid("");
                        bm.setSendStatus(1);
                    }
                    this.updateBriefMessage(bm);
                }
                return 0;
            }
        }
        return -1;
    }

    /*
     * BriefMessagePojo
     */
    // 调用，以获得BriefMessageList
    public List<BriefMessagePojo> getBriefMessage() {
        briefMessageLock.readLock().lock();
        List<BriefMessagePojo> list = briefMessageList;
        briefMessageLock.readLock().unlock();
        Log.d("RDCDebug", "<getBriefMessage> get briefMessage, #:" + list.size());
        return list;
    }

    public BriefMessagePojo getBriefMessageOne(String userId) {
        if (userId.isEmpty() || !briefMessageIndex.containsKey(userId)) {
            Log.d("RDCDebug", "<getBriefMessageOne> empty userId or briefMessageIndex has no key");
            return null;
        }
        briefMessageLock.readLock().lock();
        BriefMessagePojo bm = briefMessageList.get(briefMessageIndex.get(userId));
        briefMessageLock.readLock().unlock();
        Log.d("RDCDebug", "<getBriefMessageOne> get briefMessage, userId:" + userId + ", userName:" + bm.getUserName()
                + ", content:" + bm.getMessage());
        return bm;
    }

    // update briefmessage
    // 调用，以修改BriefMessage
    public int updateBriefMessage(BriefMessagePojo bm) {
        Log.d("RDCDebug",
                "<updateBriefMessage> update briefMessage, userId:" + bm.getUserId() + ", userName:" + bm.getUserName()
                        + ", content:" + bm.getMessage());
        if (bm.getUserId().isEmpty()) {
            Log.d("RDCDebug", "<updateBriefMessage> emtpy BriefMessagePojo guid");
            return -1;
        }
        int ret = updateBriefMessagePojo(bm);
        if (ret == -1) {
            return -1;
        }
        if (this.isInMessageActivity()) {
            notifyBriefMessageChange();
        }

        BriefMessageTask bmt = new BriefMessageTask("update", bm);
        Thread t = new Thread(bmt);
        t.start();
        Log.d("RDCDebug", "<updateBriefMessage> start BriefMessageTask single update, userId:" + bm.getUserId()
                + ", userName:" + bm.getUserName() + ", content:" + bm.getMessage());
        return 0;
    }

    // 修改信息的草稿状态
    public int setBriefMessageDraft(String userId, boolean isDraft, String content, String contentType) {
        Log.d("RDCDebug", "<setBriefMessageDraft> userId:" + userId + " isDraft:" + Boolean.toString(isDraft)
                + " content:" + content);
        if (userId.isEmpty() || !briefMessageIndex.containsKey(userId) || (isDraft && content.isEmpty())
                || !patientMessageMap.containsKey(userId)) {
            Log.d("RDCDebug",
                    "<setBriefMessageDraft> empty userId or briefMessageIndex has no key or empty content or patientMessageMap has no key");
            return -1;
        }
        int index = briefMessageIndex.get(userId);
        BriefMessagePojo bm = briefMessageList.get(index);
        BriefMessagePojo newBm = new BriefMessagePojo(bm);
        if (isDraft) {
            newBm.setIsDraft(1);
            newBm.setMessage(content);
            newBm.setContentType(contentType);
            this.updateBriefMessage(newBm);
        } else {
            newBm.setIsDraft(0);
            if (patientMessageMap.containsKey(userId)) {
                ReentrantReadWriteLock lock = lockMap.get(userId);
                lock.readLock().lock();
                List<PatientMessage> pList = patientMessageMap.get(userId);
                if (pList.size() != 0) {
                    PatientMessage pm = pList.get(pList.size() - 1);
                    DataAdapter.updateBriefMessageByNewPatientMessage(newBm, pm, true);
                } else {
                    newBm.setMessage("");
                }
                lock.readLock().unlock();
                this.updateBriefMessage(newBm);
            }
        }
        return 0;
    }

    // 调用，以删除BriefMessage
    public int deleteBriefMessage(BriefMessagePojo bm) {
        Log.d("RDCDebug",
                "<deleteBriefMessage> delete briefMessage, userId:" + bm.getUserId() + ", userName:" + bm.getUserName()
                        + ", content:" + bm.getMessage());
        if (bm.getUserId().isEmpty()) {
            Log.d("RDCDebug", "<deleteBriefMessage> emtpy BriefMessagePojo guid");
            return -1;
        }
        int ret = deleteBriefMessagePojo(bm);
        if (ret == -1) {
            Log.d("RDCDebug", "<deleteBriefMessage> deleteBriefMessagePojo FAIL!");
            return -1;
        }

        // 删除相应的cache中的patient message，但保留数据库中的数据，同时删除lock
        String userId = bm.getUserId();
        if (!lockMap.containsKey(userId)) {
            Log.d("RDCDebug", "<deleteBriefMessage> lockMap has no key, delete patient message FAIL!");
        } else {
            ReentrantReadWriteLock lock = lockMap.get("userId");
            lock.writeLock().lock();
            if (!patientMessageMap.containsKey(userId)) {
                Log.d("RDCDebug", "<deleteBriefMessage> patientMessageMap has no key, delete patient message FAIL!");
            } else {
                patientMessageMap.remove(userId);
            }
            lock.writeLock().unlock();
            lockMap.remove(userId);
        }

        if (this.isInMessageActivity()) {
            notifyBriefMessageChange();
        }

        BriefMessageTask bmt = new BriefMessageTask("delete", bm);
        Thread t = new Thread(bmt);
        t.start();
        Log.d("RDCDebug", "<deleteBriefMessage> start BriefMessageTask single delete, userId:" + bm.getUserId()
                + ", userName:" + bm.getUserName() + ", content:" + bm.getMessage());
        return 0;
    }

    private int notifyBriefMessageChange() {
        if (handlerManager.getMessageHandler() != null) {
            Message msg = new Message();
            msg.what = Common.MSG_WHAT_BRIEFCHANGE_MESSAGE;
            handlerManager.getMessageHandler().sendMessage(msg);
            Log.d("RDCDebug", "<notifyBriefMessage> notify MSG_WHAT_BRIEFCHANGE_MESSAGE MessageHandler");
        } else {
            Log.d("RDCDebug", "<notifyBriefMessage> handleManager.getMessageHandler() == null");
            return -1;
            // to do
        }
        return 0;
    }

    private int updateBriefMessagePojo(BriefMessagePojo messagePojo) {
        Log.d("RDCDebug", "<updateBriefMessagePojo> update briefMessage, userId:" + messagePojo.getUserId()
                + ", userName:" + messagePojo.getUserName() + ", content:" + messagePojo.getMessage());
        if (messagePojo.getUserId().isEmpty()) {
            Log.d("RDCDebug", "<updateBriefMessagePojo> empty messagePojo userId");
            return -1;
        }
        String key = messagePojo.getUserId();
        briefMessageLock.writeLock().lock();
        if (!briefMessageIndex.containsKey(key)) {
            Log.d("RDCDebug", "<updateBriefMessagePojo> briefMessageIndex has no key");
            briefMessageLock.writeLock().unlock();
            return -1;
        }
        int index = briefMessageIndex.get(key);
        BriefMessagePojo oldPojo = briefMessageList.get(index);
        if (oldPojo.getTime() > messagePojo.getTime()) {
            while (index + 1 < briefMessageList.size()
                    && briefMessageList.get(index + 1).getTime() > messagePojo.getTime()) {
                BriefMessagePojo tmp = briefMessageList.get(index + 1);
                briefMessageList.set(index, tmp);
                briefMessageIndex.put(tmp.getUserId(), index);
                index++;
            }
            Log.d("RDCDebug", "<updateBriefMessagePojo> new one move down to index:" + index);
        } else if (oldPojo.getTime() < messagePojo.getTime()) {
            while (index - 1 >= 0 && briefMessageList.get(index - 1).getTime() < messagePojo.getTime()) {
                BriefMessagePojo tmp = briefMessageList.get(index - 1);
                briefMessageList.set(index, tmp);
                briefMessageIndex.put(tmp.getUserId(), index);
                index--;
            }
            Log.d("RDCDebug", "<updateBriefMessagePojo> new one move up to index:" + index);
        }
        briefMessageList.set(index, messagePojo);
        briefMessageIndex.put(messagePojo.getUserId(), index);
        briefMessageLock.writeLock().unlock();
        return 0;
    }

    private int deleteBriefMessagePojo(BriefMessagePojo messagePojo) {
        Log.d("RDCDebug", "<deleteBriefMessagePojo> delete briefMessage, userId:" + messagePojo.getUserId()
                + ", userName:" + messagePojo.getUserName() + ", content:" + messagePojo.getMessage());
        if (messagePojo.getUserId().isEmpty()) {
            Log.d("RDCDebug", "<deleteBriefMessagePojo> messagePojo has no key");
            return -1;
        }
        String key = messagePojo.getUserId();
        briefMessageLock.writeLock().lock();
        if (!briefMessageIndex.containsKey(key)) {
            Log.d("RDCDebug", "<deleteBriefMessagePojo> NOT FOUND briefMessage! key:" + key);
            briefMessageLock.writeLock().unlock();
            return -1;
        }
        int index = briefMessageIndex.get(key);
        briefMessageList.remove(index);
        briefMessageIndex.remove(key);
        Log.d("RDCDebug", "<deleteBriefMessagePojo> remove index:" + index);

        // update hashmap
        index = 0;
        for (Iterator<BriefMessagePojo> iter = briefMessageList.iterator(); iter.hasNext(); index++) {
            BriefMessagePojo bm = iter.next();
            String tmpKey = bm.getUserId();
            briefMessageIndex.put(tmpKey, index);
        }
        briefMessageLock.writeLock().unlock();
        return 0;
    }

	/*Label
	 */
    //no need to set id in label
    //handle color problem

    public int genLabelColor() {
        //TO DO!!!
        float hsv[] = {0, 0, 0};
        int color = 0;
        while (true) {
            Random random = new Random();
            hsv[0] = random.nextFloat() * (Common.H_UB - Common.H_LB) + Common.H_LB;
            hsv[1] = 1;
            hsv[2] = random.nextFloat() * (Common.V_UB - Common.V_LB) + Common.V_LB;
            color = Color.HSVToColor(255, hsv);
            if (!allLabelColors.contains(color)) {
                break;
            }
        }
        Log.d("RDCDebug", "<genLabelColor> generate color" + Integer.toString(color));
        return color;
    }

    public int genLabelColorRGB() {
        //TO DO!!!
        Random random = new Random();
        int r, g, b;
        r = random.nextInt(Common.RED_COLOR_UB - Common.RED_COLOR_LB) + Common.RED_COLOR_LB;
        g = random.nextInt(Common.GREEN_COLOR_UB - Common.GREEN_COLOR_LB) + Common.GREEN_COLOR_LB;
        b = random.nextInt(Common.BLUE_COLOR_UB - Common.BLUE_COLOR_LB) + Common.BLUE_COLOR_LB;
        int color = Color.argb(255, r, g, b);
        Log.d("RDCDebug", "<genLabelColor> generate color" + Integer.toString(color));
        return color;
    }

    public String getLabelNameString(String labelIdString) {
        Logger.d("RDC Label", labelIdString);
        String labelString = "";
        if (labelIdString.equals("")) {
            return labelString;
        }
        String[] labelIds = labelIdString.split("&");
        int len = labelIds.length;
        for (int i = 0; i < len; i++) {
            Label label = labelIdMap.get(Long.valueOf(labelIds[i]));
            labelString += label.getLabelName() + "&";
        }
        labelString = labelString.substring(0, labelString.length() - 1);
        return labelString;
    }

    public List<Integer> getLabelColors(String labelIdString) {
        Logger.d("RDC Label", labelIdString);
        if (labelIdString.isEmpty()) {
            return null;
        }
        List<Integer> colorList = new ArrayList<Integer>();
        String[] labelIds = labelIdString.split("&");
        int len = labelIds.length;
        for (int i = 0; i < len; i++) {
            String id = labelIds[i];
            Label label = null;
            try {
                label = labelIdMap.get(Long.valueOf(id));
            } catch (Exception e) {
                e.printStackTrace();
                return colorList;
            }
            int color = label.getLabelColor();
            colorList.add(color);
        }
        return colorList;
    }

    public int addLabel(Label label) {
        if (label == null || label.getLabelName().isEmpty()) {
            Log.d("RDCDebug", "<addLabel> label or labelName or labelColor is null");
            return -1;
        }
        int ret = labelService.addLabelOne(label);
        if (ret != 0) {
            Log.d("RDCDebug", "<addLabel> add label to db fail");
            return ret;
        }
        int color = genLabelColor();
        this.updateLabelColorById(label.getId(), color);
        return 0;
    }

    //handle color problem
	/*public int updateLabel(Label label, Label oldLabel){
		if(label == null || oldLabel == null){
			Log.d("RDCDebug", "<updateLabel> label or oldLabel is null");
			return -1;
		}
		if(!labelIdMap.containsKey(label.getId())){
			Log.d("RDCDebug", "<updateLabel> label not exist");
			return -2;
		}
		if(!labelService.updateLabel(label.getId(), labelIdMap.get(label.getId()))){
			Log.d("RDCDebug", "<updateLabel> update db fail");
			return -3;
		}
		labelIdMap.put(label.getId(), label);
		allLabelColors.add(label.getLabelColor());
		allLabelColors.remove(oldLabel.getLabelColor());
		return 0;
	}*/

    public int updateLabelNameById(Long updateId, String updateName) {
        int ret = labelService.updateLabelNameById(updateId, updateName);
        if (ret == 0) {
            if (!labelIdMap.containsKey(updateId)) {
                Label label = labelService.getLabelById(updateId);
                labelIdMap.put(label.getId(), label);
            }
            Label label = labelIdMap.get(updateId);
            label.setLabelName(updateName);
            PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(MyApplication.context, null);
            List<String> patientIds = plr.listPatientByLabelId(updateId);
            for (Iterator<String> iter = patientIds.iterator(); iter.hasNext(); ) {
                String id = iter.next();
                this.updateLabelInBM(id);
            }
        }
        return ret;
    }

    public int updateLabelColorById(Long updateId, int color) {
        Label l;
        if (!labelIdMap.containsKey(updateId)) {
            l = labelService.getLabelById(updateId);
            labelIdMap.put(l.getId(), l);
        } else {
            l = labelIdMap.get(updateId);
        }
        int oldColor = l.setLabelColor(color);
        boolean ret = labelService.updateLabel(updateId, l);
        if (ret) {
            allLabelColors.add(l.getLabelColor());
            PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(MyApplication.context, null);
            List<String> patientIds = plr.listPatientByLabelId(updateId);
            for (Iterator<String> iter = patientIds.iterator(); iter.hasNext(); ) {
                String id = iter.next();
                this.updateLabelInBM(id);
            }
        } else {
            l.setLabelColor(oldColor);
        }
        return ret == true ? 0 : -1;
    }

    //handle color problem
    public Label getLabel(long id) {
        if (labelIdMap.contains(id)) {
            return labelIdMap.get(id);
        } else {
            Label label = labelService.getLabelById(id);
            if (label == null) {
                return null;
            } else {
                labelIdMap.put(label.getId(), label);
                return label;
            }
        }
    }

    //handle color problem
    public int delLabel(long id) {
        if (!labelService.deleteLabelById(id)) {
            return -1;
        }
        Label label = labelIdMap.remove(id);
        int color = label.getLabelColor();
        if (allLabelColors.contains(color)) {
            allLabelColors.remove(color);
        }
        PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(MyApplication.context, null);
        List<String> pList = plr.listPatientByLabelId(id);
        if (!plr.deleteRelationInfoByLabelId(id)) {
            return -1;
        }
        for (Iterator<String> iter = pList.iterator(); iter.hasNext(); ) {
            String pId = iter.next();
            updateLabelInBM(pId);
        }
        return 0;
    }

    public int updateLabelInBM(String patientId) {
        PatientLabelRelationService plr = new PatientLabelRelationServiceImpl(MyApplication.context, null);
        BriefMessagePojo oldBm = this.getBriefMessageOne(patientId);
        if (oldBm == null) {
            return 0;
        }
        List<Long> idList = plr.listLabelByPatientId(patientId);
        LabelService ls = new LabelServiceImpl(MyApplication.context, null);
        String labelString = "";
        for (Iterator<Long> iter = idList.iterator(); iter.hasNext(); ) {
            Long lId = iter.next();
            labelString += Long.toString(ls.getLabelById(lId).getId()) + "&";
        }
        if (!labelString.equals("")) {
            labelString = labelString.substring(0, labelString.length() - 1);
        }
        BriefMessagePojo bm = new BriefMessagePojo(oldBm);
        bm.setLabels(labelString);
        this.updateBriefMessage(bm);
        return 0;
    }

    //1: quiet, 0:noise
    public void setQuiet(boolean isQuiet) {
        if (isQuiet) {
            spManager.setOne("keyQuiet", "1");
            this.isQuiet = isQuiet;
        } else {
            spManager.setOne("keyQuiet", "0");
            this.isQuiet = isQuiet;
        }
    }

    public boolean getQuiet() {
        return isQuiet;
    }

    /*
     * Other
     */
    private ComponentName getCurrentActivity() {
        Context context = MyApplication.context;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningTasks(1).get(0).topActivity;
    }

    public void setChatUserId(String userId) {
        chatActivityUserIdLock.writeLock().lock();
        this.chatActivityUserId = userId;
        chatActivityUserIdLock.writeLock().unlock();
    }

    public String getUserId() {
        chatActivityUserIdLock.readLock().lock();
        String ret = this.chatActivityUserId;
        chatActivityUserIdLock.readLock().unlock();
        return ret;
    }

    public boolean isInMessageActivity() {
        isMessageActivityLock.readLock().lock();
        boolean ret = isInMessageActivity;
        isMessageActivityLock.readLock().unlock();
        return ret;
    }

    public void setInMessageActivity(boolean isInMessageActivity) {
        isMessageActivityLock.writeLock().lock();
        this.isInMessageActivity = isInMessageActivity;
        isMessageActivityLock.writeLock().unlock();
    }

    public boolean isInChatActivity(String curPatientId) {
        isChatActivityLock.readLock().lock();
        boolean ret = false;
        if (this.curPatientId != null && this.curPatientId.equals(curPatientId) && this.isInChatActivity) {
            ret = true;
        }
        isChatActivityLock.readLock().unlock();
        return ret;
    }

    public void setInChatActivity(boolean isInChatActivity, String curPatientId) {
        isChatActivityLock.writeLock().lock();
        this.isInChatActivity = isInChatActivity;
        this.curPatientId = curPatientId;
        isChatActivityLock.writeLock().unlock();
    }

    public boolean isInPatientActivity() {
        isPatientActivityLock.readLock().lock();
        boolean ret = isInPatientActivity;
        isPatientActivityLock.readLock().unlock();
        return ret;
    }

    public void setInPatientActivity(boolean isInPatientActivity) {
        isPatientActivityLock.writeLock().lock();
        this.isInPatientActivity = isInPatientActivity;
        isPatientActivityLock.writeLock().unlock();
    }

    public int getNewPatientNum() {
        newPatientNumLock.readLock().lock();
        int ret = newPatientNum;
        newPatientNumLock.readLock().unlock();
        return ret;
    }

    public void setNewPatientNum() {
        newPatientNumLock.writeLock().lock();
        this.newPatientNum = newPatientInfoService.getUnchechedCount();
        newPatientNumLock.writeLock().unlock();
    }

    public void plusNewPatientNum(int k) {
        newPatientNumLock.writeLock().lock();
        newPatientNum += k;
        newPatientNumLock.writeLock().unlock();
    }

    public void minusNewPatientNum(int k) {
        newPatientNumLock.writeLock().lock();
        newPatientNum -= k;
        if (newPatientNum < 0) {
            newPatientNum = 0;
        }
        newPatientNumLock.writeLock().unlock();
    }

    public void newPatientAllChecked() {
        newPatientNumLock.writeLock().lock();
        if (this.newPatientNum == 0) {
            return;
        }
        if (!newPatientInfoService.allCheck()) {
            return;
        }
        this.newPatientNum = 0;
        newPatientNumLock.writeLock().unlock();
    }

    public boolean isAttention(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            return false;
        }
        return atnPatientIdMap.containsKey(patientId);
    }

    /*
     * Inner class
     */
    class PatientMessageTask implements Runnable {
        private PatientMessageInfoService patientMessageInfoService = new PatientMessageInfoServiceImpl(
                MyApplication.context, null);
        private RecordService recordService = new RecordServiceImpl(MyApplication.context, null);
        private ReentrantReadWriteLock lock;
        private PatientMessage pm;
        private List<PatientMessage> pmList;
        String userId = "";
        String taskAction = ""; // add, update, delete
        String taskScale = ""; // single, batch

        public PatientMessageTask(String taskAction, PatientMessage pm, String userId, ReentrantReadWriteLock lock) {
            this.pm = pm;
            this.userId = userId;
            this.taskAction = taskAction;
            this.taskScale = "single";
            this.lock = lock;
        }

        public PatientMessageTask(String taskAction, List<PatientMessage> pmList, String userId,
                                  ReentrantReadWriteLock lock) {
            this.pmList = pmList;
            this.userId = userId;
            this.taskAction = taskAction;
            this.taskScale = "batch";
            this.lock = lock;
        }

        @Override
        public void run() {
            if (pm != null) {
                Log.d("RDCDebug", "<Class PatientMessageTask:run()> run thread " + taskAction + " " + taskScale
                        + " message, userId:" + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID()
                        + " content:" + pm.getContent());
            } else {
                Log.d("RDCDebug", "<Class PatientMessageTask:run()> run thread " + taskAction + " " + taskScale
                        + " messagessss, #:" + pmList.size());
            }
            if (taskAction.equals("add")) {
                if (taskScale.equals("single")) {
                    lock.writeLock().lock();
                    if (patientMessageInfoService.addPatientMessageInfo(this.pm)) {
                        addPatientMessageList(this.userId, this.pm);
                        lock.writeLock().unlock();
                        if (pm.getContentType().equals("image")) {
                            try {
                                JSONObject jo = new JSONObject(pm.getContent());
                                String thumbnailPath = jo.getString("thumbnailPath");
                                String avatarPath = jo.getString("avaterPath");
                                Record record = new Record();
                                record.setCreated(pm.getTimestamp());
                                record.setDoctorId(spManager.getOne("keySid"));
                                record.setPatientId(DataAdapter.getUserId(pm));
                                record.setRecordPic(avatarPath);
                                record.setRecordType(Record.TYPE_IMAGE);
                                record.setThumbnail(thumbnailPath);
                                if (recordService.addRecord(record) < 0) {
                                    Log.d("RDCDebug", "<Class PatientMessageTask:run()> add to record FAIL!");
                                    // to do exception
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.d("RDCDebug",
                                "<Class PatientMessageTask:run()> db addPatientMessageInfo FAIL! message, userId:"
                                        + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID() + " content:"
                                        + pm.getContent());
                        lock.writeLock().unlock();
                    }

                } else if (taskScale.equals("batch")) {
                    lock.writeLock().lock();
                    if (patientMessageInfoService.addPatientMessageInfo(this.pmList)) {
                        addPatientMessageList(this.userId, this.pmList);
                    } else {
                        Log.d("RDCDebug",
                                "<Class PatientMessageTask:run()> db addPatientMessageInfo FAIL! messagessss, #:"
                                        + pmList.size());
                    }
                    lock.writeLock().unlock();
                } else {
                    // to do
                }
            } else if (taskAction.equals("update")) {
                if (taskScale.equals("single")) {
                    lock.writeLock().lock();
                    if (patientMessageInfoService.updatePatientMessageInfo(this.pm)) {
                        updatePatientMessageList(this.userId, this.pm);
                    } else {
                        Log.d("RDCDebug",
                                "<Class PatientMessageTask:run()> db updatePatientMessageInfo FAIL! message, userId:"
                                        + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID() + " content:"
                                        + pm.getContent());
                    }
                    lock.writeLock().unlock();
                } else if (taskScale.equals("batch")) {
                    lock.writeLock().lock();
                    if (patientMessageInfoService.updatePatientMessageInfo(this.pmList)) {
                        updatePatientMessageList(this.userId, this.pmList);
                    } else {
                        Log.d("RDCDebug",
                                "<Class PatientMessageTask:run()> db updaetPatientMessageInfo FAIL! messagessss, #:"
                                        + pmList.size());
                    }
                    lock.writeLock().lock();
                } else {
                    // to do exception
                }
            } else if (taskAction.equals("delete")) {
                if (taskScale.equals("single")) {
                    lock.writeLock().lock();
                    if (patientMessageInfoService.deletePatientMessageInfo(pm.getGuid())) {
                        deletePatientMessageList(this.userId, this.pm);
                        // List<PatientMessage> list =
                        // patientMessageInfoService.listLastKPatientMessageBeforeTime(userId,
                        // 1,
                        // patientMessageMap.get(this.userId).get(0).getTimestamp());
                        List<PatientMessage> list = patientMessageInfoService.listLastKPatientMessageBeforeId(userId,
                                1, patientMessageMap.get(this.userId).get(0).getGuid());
                        if (!list.isEmpty()) {
                            insertHeadPatientMessageList(this.userId, list.get(0));
                        }
                    } else {
                        Log.d("RDCDebug",
                                "<Class PatientMessageTask:run()> db deletePatientMessageInfo FAIL! message, userId:"
                                        + userId + ", from:" + pm.getFromID() + ", to:" + pm.getToID() + " content:"
                                        + pm.getContent());
                    }
                    lock.writeLock().unlock();
                } else {
                    // to do exception
                }
            } else {
                // to do exception
            }

        }
    }

    class BriefMessageTask implements Runnable {
        private CurrentChatUserService currentChatUserService = new CurrentChatUserServiceImpl(MyApplication.context,
                null);
        private BriefMessagePojo bm;
        private List<BriefMessagePojo> bmList;
        String taskAction = ""; // add, update
        String taskScale = ""; // single, batch

        public BriefMessageTask(String taskAction, BriefMessagePojo bm) {
            this.taskAction = taskAction;
            this.taskScale = "single";
            this.bm = bm;
        }

        public BriefMessageTask(String taskAction, List<BriefMessagePojo> bmList) {
            this.taskAction = taskAction;
            this.taskScale = "batch";
            this.bmList = bmList;
        }

        @Override
        public void run() {
            if (bm != null) {
                Log.d("RDCDebug",
                        "<Class BriefMessageTask:run()> run thread " + taskAction + " " + taskScale + ", userId:"
                                + bm.getUserId() + ", userName:" + bm.getUserName() + ", content:" + bm.getMessage());
            } else {
                // to do
            }
            if (this.taskAction.equals("add")) {
                if (this.taskScale.equals("single")) {
                    currentChatUserService.addChatUserOne(bm);
                } else if (this.taskScale.equals("batch")) {
                    currentChatUserService.addChatUserList(bmList);
                } else {
                    // to do exception
                }
            } else if (this.taskAction.equals("update")) {
                if (this.taskScale.equals("single")) {
                    currentChatUserService.updateChatUser(bm);
                } else {
                    // to do exception
                }
            } else if (this.taskAction.equals("delete")) {
                if (this.taskScale.equals("single")) {
                    currentChatUserService.deleteChatUser(bm.getUserId());
                } else {
                    // to do exception
                }
            } else {
                // to do exception
            }
        }
    }

    // test methods
    public void testAddPatientMessage(String userId, PatientMessage pm) {
        Log.d("TestRDC", "<testAddPatientMessage>: Before Add Single");
        testPrintLogPatientMessageList(userId);
        addPatientMessageList(userId, pm);
        Log.d("TestRDC", "<testAddPatientMessage>: After Add Single");
        testPrintLogPatientMessageList(userId);

        List<PatientMessage> list = new LinkedList<PatientMessage>();
        pm.setContent("我是Batch Test");
        list.add(pm);
        list.add(pm);
        list.add(pm);

        Log.d("TestRDC", "<testAddPatientMessage>: Before Add Batch");
        testPrintLogPatientMessageList(userId);
        addPatientMessageList(userId, list);
        Log.d("TestRDC", "<testAddPatientMessage>: After Add Batch");
        testPrintLogPatientMessageList(userId);

    }

    public void testUpdatePatientMessage(String userId, PatientMessage pm) {
        Log.d("TestRDC", "<testUpdatePatientMessage>: Before Update Single");
        testPrintLogPatientMessageList(userId);
        this.updatePatientMessageList(userId, pm);
        Log.d("TestRDC", "<testUpdatePatientMessage>: After Update Single");
        testPrintLogPatientMessageList(userId);

        List<PatientMessage> list = new LinkedList<PatientMessage>();
        pm.setGuid("026");
        pm.setContent("我是Batch Test pm");
        PatientMessage pm2 = new PatientMessage();
        PatientMessage pm3 = new PatientMessage();
        pm2.setGuid("027");
        pm2.setContent("我是Batch Test pm2");
        pm3.setGuid("123455");
        pm3.setContent("我是Batch Test pm3");

        list.add(pm);
        list.add(pm2);
        list.add(pm3);

        Log.d("TestRDC", "<testUpdatePatientMessage>: Before Update Batch");
        testPrintLogPatientMessageList(userId);
        this.updatePatientMessageList(userId, list);
        Log.d("TestRDC", "<testUpdatePatientMessage>: After Update Batch");
        testPrintLogPatientMessageList(userId);
    }

    public void testDeletePatientMessage(String userId, PatientMessage pm) {
        Log.d("TestRDC", "<testDeletePatientMessage>: Before Delete Single");
        testPrintLogPatientMessageList(userId);
        testPrintLogBriefMessage();
        // this.deletePatientMessageList(userId, pm);
        this.deletePatientMessage(pm, userId);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("TestRDC", "<testUpdatePatientMessage>: After Delete Single");
        testPrintLogPatientMessageList(userId);
        testPrintLogBriefMessage();
    }

    public void testPrintLogPatientMessageList(String userId) {
        ReentrantReadWriteLock lock = lockMap.get(userId);
        lock.readLock().lock();
        List<PatientMessage> list = patientMessageMap.get(userId);
        lock.readLock().unlock();
        for (Iterator<PatientMessage> iter = list.iterator(); iter.hasNext(); ) {
            PatientMessage tmp = iter.next();
            Log.d("TestRDC", "From: " + tmp.getFromID() + " To: " + tmp.getToID() + " Content: " + tmp.getContent());
        }
    }

	/*
	 * public void testNewBriefMessage(BriefMessagePojo bm){ Log.d("TestRDC",
	 * "<testAddBriefMessage>: Before Add Single"); testPrintLogBriefMessage();
	 * this.newBriefMessagePojo(bm); Log.d("TestRDC",
	 * "<testAddBriefMessage>: After Add Single"); testPrintLogBriefMessage(); }
	 */

    public void testUpdateBriefMessage(BriefMessagePojo bm) {
        Log.d("TestRDC", "<testAddBriefMessage>: Before Update Single");
        testPrintLogBriefMessage();
        this.updateBriefMessagePojo(bm);
        Log.d("TestRDC", "<testAddBriefMessage>: After Update Single");
        testPrintLogBriefMessage();
    }

    public void testDeleteBriefMessage(BriefMessagePojo bm) {
        Log.d("TestRDC", "<testDeleteBriefMessage>: Before Delete Single");
        testPrintLogBriefMessage();
        // this.deleteBriefMessagePojo(bm);

        this.deleteBriefMessage(bm);

        Log.d("TestRDC", "<testDeleteBriefMessage>: After Delete Single");
        testPrintLogBriefMessage();
    }

    public void testPrintLogBriefMessage() {
        briefMessageLock.readLock().lock();
        for (Iterator<BriefMessagePojo> iter = briefMessageList.iterator(); iter.hasNext(); ) {
            BriefMessagePojo bm = iter.next();
            Log.d("TestRDC",
                    "UserId: " + bm.getUserId() + " UserName: " + bm.getUserName() + " Content: " + bm.getMessage()
                            + " Time: " + bm.getTime());
        }
        briefMessageLock.readLock().unlock();
    }

    public void testSavePatientBriefMessage() {
        Log.d("TestRDC", "<testSavePatientMessage>: Before Save Patient");
        Log.d("TestRDC", "Cache: ");
        this.testPrintLogPatientMessageList("p1");
        PatientMessage pm = new PatientMessage();
        pm.setContent("我是测试哥");
        pm.setContentType("text");
        pm.setDeleted(0);
        pm.setFromID("p1");
        pm.setToID("d1");
        pm.setGuid(UUID.randomUUID().toString());
        pm.setId(10);
        pm.setIsRead(0);
        pm.setSendStatus(0);
        pm.setTimestamp(System.currentTimeMillis());
        this.savePatientMessage(pm, "p1");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d("TestRDC", "<testSavePatientMessage>: After Save Patient");
        Log.d("TestRDC", "Cache: ");
        this.testPrintLogPatientMessageList("p1");

        Log.d("TestRDC", "<testSavePatientMessage>: Before Save BriefMessage");
        this.testPrintLogBriefMessage();
        this.saveBriefMessage(pm, "p1", false);
        Log.d("TestRDC", "<testSavePatientMessage>: After Save BriefMessage");
        this.testPrintLogBriefMessage();

        PatientMessage pm2 = new PatientMessage();
        pm2.setContent("我是另一个测试哥");
        pm2.setContentType("text");
        pm2.setDeleted(0);
        pm2.setFromID("p10");
        pm2.setToID("d1");
        pm2.setGuid(UUID.randomUUID().toString());
        pm2.setId(10);
        pm2.setIsRead(0);
        pm2.setSendStatus(0);
        pm2.setTimestamp(System.currentTimeMillis());

        Log.d("TestRDC", "<testSavePatientMessage>: Before Save BriefMessage");
        this.testPrintLogBriefMessage();
        this.saveBriefMessage(pm2, "p10", false);
        Log.d("TestRDC", "<testSavePatientMessage>: After Save BriefMessage");
        this.testPrintLogBriefMessage();
    }

    public void testReceiveMessage() {
        PatientMessage pm = new PatientMessage();
        pm.setContent("我是测试哥");
        pm.setContentType("text");
        pm.setDeleted(0);
        pm.setFromID("p1");
        pm.setToID("d1");
        pm.setGuid(UUID.randomUUID().toString());
        pm.setId(10);
        pm.setIsRead(0);
        pm.setSendStatus(0);
        pm.setTimestamp(System.currentTimeMillis());
        Log.d("TestRDC", "<testReceiveMessage>: Before Receive Message");
        this.testPrintLogPatientMessageList("p1");
        this.testPrintLogBriefMessage();
        this.receiveMessage(pm);
        Log.d("TestRDC", "<testReceiveMessage>: After Receive Message");
        this.testPrintLogPatientMessageList("p1");
        this.testPrintLogBriefMessage();
    }
}
