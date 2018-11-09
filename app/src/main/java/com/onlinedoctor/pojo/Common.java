package com.onlinedoctor.pojo;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;

import com.onlinedoctor.activity.R;

public class Common {

    /**
     *  Debug 模式 or Release 模式
     *  后面考虑引入日志系统
     */
    public final static boolean DEBUG = true;

    // Version 1.1.0 七牛云开关
    public final static boolean IsUploadToQiniu = true;

    /**
     * ENVIRONMENT:
     * 'prod': 正式服
     * 'test': 测试服
     */
    public final static String PROD_SERVER = "prod";
    public final static String TEST_SERVER = "test";
    public final static String ENVIRONMENT = PROD_SERVER;    //选择范围{PROD_SERVER, TEST_SERVER}

    /**
     * Server汇总:
     * 测服、正式服
     */
    public static final String TEST_HTTP_SERVER = "http://133.130.99.150:80/";
    public static final String TEST_HTTPS_SERVER = "https://133.130.99.150:443/";
    public static final String TEST_WEBSOCKET_SERVER = "ws://133.130.99.150:80/";

    public static final String PROD_HTTP_SERVER = "http://120.26.118.251:80/";
    public static final String PROD_HTTPS_SERVER = "https://120.26.118.251:443/";
    public static final String PROD_WEBSOCKET_SERVER = "ws://120.26.118.251:80/";

    public static final String HTTP_SERVER = server("http") != null ? server("http") : TEST_HTTP_SERVER;
    public static final String HTTPS_SERVER = server("https") != null ? server("https") : TEST_HTTPS_SERVER;
    public static final String WEBSOCKET_SERVER = server("websocket") != null ? server("websocket") : TEST_WEBSOCKET_SERVER;

    /**
     *
     * Mob短信认证平台相关信息
     * 链接: http://www.mob.com
     */
    public static final String TEST_MOB_APP_KEY = "c1d5f73b84aa";
    public static final String TEST_MOB_APP_SECRET = "597c6134e5d347c1813a73f3125359cb";
    public static final String PROD_MOB_APP_KEY = "73d00b9870b8";
    public static final String PROD_MOB_APP_SECRET = "eb17674bf885ee784f5fd09fa3b7696b";

    public static final String MOB_APP_KEY = getMobType("app_key") != null ? getMobType("app_key") : TEST_MOB_APP_KEY;
    public static final String MOB_APP_SECRET = getMobType("app_secret") != null ? getMobType("app_secret") : TEST_MOB_APP_SECRET;

    /**
     *
     * Qiniu(七牛)云存储平台相关信息
     * 链接:http://www.qiniu.com/
     */
    public static final String CHECK_VERSION_URL = "http://7xopuh.dl1.z0.glb.clouddn.com/version.xml";


    public enum ChatMsgType {
        TEXT, IMAGE, LINK, VOICE, SURVEY
    }

    public static final String MESSAGE_TYPE_text = "text";
    public static final String MESSAGE_TYPE_image = "image";
    public static final String MESSAGE_TYPE_link = "link";
    public static final String MESSAGE_TYPE_voice = "voice";
    public static final String MESSAGE_TYPE_survey = "survey";
    public static final String MESSAGE_TYPE_surveyFeedback = "surveyfeedback";
    public static final String MESSAGE_TYPE_paymentRequest = "paymentRequest";
    public static final String MESSAGE_TYPE_paymentSuccess = "paymentSuccess";
    public static final String MESSAGE_TYPE_prescription = "prescription";

    public enum MessageType {
        TYPE_PONG, TYPE_SYNMESSAGE, TYPE_CHATMESSAGE, TYPE_ATNMESSAGE, TYPE_SYNATNMESSAGE
    }

    /**
     * WebSocket通信进行解析的 json key
     */
    public static final String code = "code";
    public static final String data = "data";
    public static final String type = "type";
    public static final String type_pong = "pong";
    public static final String type_synmessage = "synMessage";
    public static final String type_chatmessage = "message";
    public static final String type_atnmessage = "attention";
    public static final String type_synatnmessage = "synAttention";


    // 头像的圆角半径设定
    public static final int ImageCornerRadiusNull = 0;
    public static final int ImageCornerRadiusSmall = 10;
    public static final int ImageCornerRadiusMid = 20;
    public static final int ImageCornerRadiusBig = 40;
    public static final int ImageCornerRadiusHuge = 80;
    public static final int ImageCornerRadiusRound = 200;

	/*
     * public static final String RootChatUrl =
	 * "ws://192.168.0.120:8080/api/connect/"; public static final String
	 * TestHttpUrl = "http://192.168.0.120:8080/login/";
	 */


    public static final String Url = HTTP_SERVER;
    public static final String UrlHttps = HTTPS_SERVER;

    public static final String RootChatUrl = WEBSOCKET_SERVER + "api/connect/";

    public static final String ImageHttpUrl = IsUploadToQiniu ? HTTP_SERVER + "token/img":HTTP_SERVER + "/img";
    public static final String VoiceHttpUrl = IsUploadToQiniu ? HTTP_SERVER + "token/voice":HTTP_SERVER + "/voice";

    public static final String SelfDefinedFeeUrl = HTTP_SERVER + "charging/";
    public static final String DiseaseSurveyUrl = HTTP_SERVER + "survey/";
    public static final String MineUrl = HTTP_SERVER + "info/doctor/";
    public static final String ClinicPlanUrl = HTTP_SERVER + "clinic/";
    public static final String RegUrl = HTTP_SERVER + "register/doctor";
    public static final String AddDoctorInfoUrl = HTTP_SERVER + "info/doctor/add";

    public static final String GetDoctorInfoUrl = HTTP_SERVER + "info/doctor/get";
    public static final String QRCodeUrl = "http://121.43.102.185/qr?no=";
    public static final int MessageReceive = 1001;

    public static final boolean ifTestDB = true; // If to test DB

    /*
     * 数据库升级版本管理
     */
    //public static final int Version = 1; // apk version 1.0.0 以及 1.0.1
    public static final int Version = 2; // apk version 2.0.0

    // shared preference
    // 左边是调用名，右边是存储名。代码中只能通过调用名来访问。存储名是表单中实际的存储名称。调用名一旦定义，不要更改，以免牵扯面过大。存储名可以更改。
    public static final HashMap<String, String> preferenceMap = new HashMap<String, String>() {
        {
            put("perfName", "user");
            put("keySid", "sid");
            put("keyPhone", "phone");
            put("keyPassword", "password");
            put("keyToken", "token");
            put("keyLatestTime", "latestTime");
            put("keyAtnLatestTime", "atnLatestTime");
            put("keyVersion", "version");
            put("keyClinicJson", "clinicJson");
            put("keyQuiet", "quiet");
            put("keyLastPhone", "lastPhone");
            put("keyLastThumb", "lastThumb");
            put("keyUsers", "users");
            put("keySynTimes", "synTimes");
            put("keyUnReadPayItemNum", "unReadPayItemNum");
            //put("keyDoctorInfo", "doctorInfo");
            put("keyQiniuTokenImg","QiniuTokenImg");
            put("keyQiniuTokenVoice","QiniuTokenVoice");
        }
    };

    /**
     * Http通信进行解析的Json Key
     */
    public static final String KEY_IMAGE_thumbnailPath = "thumbnailPath";
    public static final String KEY_IMAGE_avaterPath = "avaterPath";
    public static final String KEY_VOICE_voicePath = "voicePath";

    /**
     * 保存图片和音频文件，temp文件夹用来保存缓存数据（压缩格式的）
     */
    public static final String sdPicSave = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "yixingzhe" + File.separator + "picture";
    public static final String sdAudioSave = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "yixingzhe" + File.separator + "audio";
    public static final String sdTempSave = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "yixingzhe" + File.separator + "temp";

    /**
     * Handler消息通知标识符
     */
    public static final int MSG_WHAT_CHAT_MESSAGE_SERVER_FEEDBACK = 1001; // Server返回反馈信息
    public static final int MSG_WHAT_CHAT_MESSAGE_RECEIVE = 1002;// 患者信息
    public static final int MSG_WHAT_ATN_MESSAGE = 1003;
    public static final int MSG_WAHT_AUDIO_MESSAGE = 1004;
    public static final int MSG_WAHT_IMAGE_MESSAGE = 1005;
    public static final int MSG_WHAT_SEND_TO_SERVER_END = 1006;
    public static final int MSG_WHAT_OPEN_JUHUA = 1007;
    public static final int MSG_WHAT_CLOSE_JUHUA = 1008;
    public static final int MSG_WHAT_SEND_FAILED = 1009;
    public static final int MSG_WHAT_MESSAGE_DELETE = 1010;
    public static final int MSG_LISTVIEW_POS_MAINTAIN = 1011;
    public static final int MSG_WAHT_SEND_AGAIN = 1012;
    public static final int MSG_WHAT_BRIEFCHANGE_MESSAGE = 2001;
    public static final int MSG_WHAT_PATIENTCOUNTCHANGE_MESSAGE = 2002;
    public static final int MSG_WHAT_SYN_MESSAGE = 3001;
    public static final int MSG_WHAT_CLINIC_PLAN = 4001;
    public static final int MSG_WHAT_QRCODE = 5001;
    public static final int MSG_WHAT_HIDE = 5002;
    public static final int MSG_WHAT_MINE_CHARGING = 5003;
    public static final int MSG_WHAT_PAYMENT_SUCCESSFUL = 5004;
    public static final int MSG_WHAT_LINK = 5001;
    /*
     *
     */
    public static final int PATIENT_MESSAGE_NUM = 18;
    public static final int BRIEF_MESSAGE_NUM = 100;
    public static final int BRIEF_MESSAGE_HASHMAP_INIT_NUM = 50;

    /*
     * 广播事件
     */
    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_REConnect_WebSocket = "com.onlinedoctor.action.reconnect.websocket";
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    public static final int RED_COLOR_LB = 94;
    public static final int RED_COLOR_UB = 252;
    public static final int GREEN_COLOR_LB = 150;
    public static final int GREEN_COLOR_UB = 252;
    public static final int BLUE_COLOR_LB = 100;
    public static final int BLUE_COLOR_UB = 252;

    public static final float H_LB = 0;
    public static final float H_UB = 360;
    public static final float S = 1;
    public static final float V_LB = 0.6f;
    public static final float V_UB = 0.98f;


    /**
     * 权威认证状态
     */
    public static final int AUTH_NOT_CHECK = 0; //未认证
    public static final int AUTH_IN_PROGRESS = 1; //认证中
    public static final int AUTH_NOT_PASS = 2; // 没通过认证
    public static final int AUTH_PASS = 3; // 通过认证
    public static final String[] AUTH_STATUS_ARRAY = {"未认证", "认证中", "认证不通过", "已认证"}; //和上面四个状态顺序对应


    /**
     * 我的钱包
     */
    public final static String URL_GET_MONEY = HTTP_SERVER + "wallet/get/money";
    public final static String URL_GET_WITHDRAW_TYPE = HTTP_SERVER + "wallet/get/withdrawType";
    public final static String URL_UPDATE_MOBILE = HTTP_SERVER + "wallet/update/cellphone";
    public final static String URL_UPDATE_ALIPAY = HTTP_SERVER + "wallet/update/alipay";
    public final static String URL_UPDATE_BANK = HTTP_SERVER + "wallet/update/bankcard";
    public final static String URL_GET_WITHDRAW_LIST = HTTP_SERVER + "withdraw/get";
    public final static String URL_WITHDRAW_REQUEST = HTTP_SERVER + "withdraw/request";
    public final static String URL_GET_ORDER_LIST = HTTP_SERVER + "trade/order/get/income";

    /**
     * 自定义收费
     */
    public static final String URL_SELF_DEFINE_FEE = HTTPS_SERVER + "charging/";

    /**
     * App版本
     */
    public static final String URL_VERSION = HTTP_SERVER + "version/get";

    /**
     * 病情调查
     */
    public final static String URL_INSERT_QUESTIONNAIRE = HTTP_SERVER + "question/naire/insert";
    public final static String URL_UPDATE_QUESTIONNAIRE = HTTP_SERVER + "question/naire/update";
    public final static String URL_DELETE_QUESTIONNAIRE = HTTP_SERVER + "question/naire/delete";
    public final static String URL_GET_QUESTIONNAIRE = HTTP_SERVER + "question/naire/getAfterTime";

    /**
     * 调查问卷题型
     */
    public static final int TYPE_SINGLE_CHOICE = 0; //必须从0开始，因为getItemViewType()返回值必须介于0 ～ getViewTypeCount() -1
    public static final int TYPE_MUTIL_CHOICES = 1;
    public static final int TYPE_SUBJECTIVE = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int SURVEY_QUESTION_TYPE_NUM = 4;


    /**
     * 根据 ENVIRONMENT 来自动设定后台服务器的其他常量
     *
     * @param str
     * @return
     */
    private static String server(final String str) {
        if (ENVIRONMENT.equals(PROD_SERVER)) {
            if (str.equals("http")) {
                return PROD_HTTP_SERVER;
            } else if (str.equals("https")) {
                return PROD_HTTPS_SERVER;
            } else if (str.equals("websocket")) {
                return PROD_WEBSOCKET_SERVER;
            }
        } else if (ENVIRONMENT.equals(TEST_SERVER)) {
            if (str.equals("http")) {
                return TEST_HTTP_SERVER;
            } else if (str.equals("https")) {
                return TEST_HTTPS_SERVER;
            } else if (str.equals("websocket")) {
                return TEST_WEBSOCKET_SERVER;
            }
        }
        return null;
    }

    /**
     * 根据TYPE类型，返回对应的字符串值
     *
     * @param context
     * @param type
     * @return
     */
    public static String getTypeName(final Context context, final int type) {
        String ret;
        switch (type) {
            case TYPE_SINGLE_CHOICE:
                ret = context.getResources().getString(R.string.survey_single_choice);
                break;
            case TYPE_MUTIL_CHOICES:
                ret = context.getResources().getString(R.string.survey_multi_choices);
                break;
            case TYPE_SUBJECTIVE:
                ret = context.getResources().getString(R.string.survey_subjective);
                break;
            case TYPE_IMAGE:
                ret = context.getResources().getString(R.string.survey_image);
                break;
            default:
                ret = null;
                break;
        }
        return ret;
    }

    /**
     * 根据 ENVIRONMENT 来自动设定MOB平台的其他常量
     *
     * @param mobType
     * @return
     */
    private static String getMobType(final String mobType) {
        if (ENVIRONMENT.equals(PROD_SERVER)) {
            if (mobType.equals("app_key")) {
                return PROD_MOB_APP_KEY;
            } else if (mobType.equals("app_secret")) {
                return PROD_MOB_APP_SECRET;
            } else {
                return null;
            }
        } else if (ENVIRONMENT.equals(TEST_SERVER)) {
            if (mobType.equals("app_key")) {
                return TEST_MOB_APP_KEY;
            } else if (mobType.equals("app_secret")) {
                return TEST_MOB_APP_SECRET;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}