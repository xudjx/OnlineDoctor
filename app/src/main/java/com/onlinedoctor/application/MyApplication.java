package com.onlinedoctor.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onlinedoctor.activity.login.LoginActivity;
import com.onlinedoctor.net.mine.MineNetManager;
import com.onlinedoctor.activity.mine.homepage.MinePageActivity;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.net.MyChatClient;
import com.onlinedoctor.net.WebMessageDispatcher;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.mine.DoctorInfo;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.service.BootReceiver;
import com.onlinedoctor.service.KeepAliveService;
import com.onlinedoctor.service.NotifyManager;
import com.onlinedoctor.sqlite.dao.DoctorInfoServiceImpl;
import com.onlinedoctor.sqlite.service.DoctorInfoService;
import com.onlinedoctor.util.SharedpreferenceManager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.onlinedoctor.log.manager.LogManager;

import cn.smssdk.SMSSDK;

public class MyApplication extends Application {

	public static MyChatClient chatClient = null;
	public static final String TAG = "MyApplication";
	public static Context context = null;

	public static BootReceiver bootReceiver;

	public static Lock lock = new ReentrantLock();

	private static final String APPKEY = Common.MOB_APP_KEY;
	private static final String APPSECRET = Common.MOB_APP_SECRET;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		bootReceiver = new BootReceiver();
		LogManager.getManager(context).registerCrashHandler();
		initImageLoader(getApplicationContext());
		initSharedpreference();
		regiestBroadReceiver();
		SMSSDK.initSDK(this, APPKEY, APPSECRET);
		initDir();
	}

	// 初始化目录结构
	private void initDir(){
		File file1 = new File(Common.sdPicSave);
		if(!file1.exists()){
			file1.mkdirs();
		}
		File file2 = new File(Common.sdAudioSave);
		if(!file2.exists()){
			file2.mkdirs();
		}
		File file3 = new File(Common.sdTempSave);
		if(!file3.exists()){
			file3.mkdirs();
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		LogManager.getManager(context).unregisterCrashHandler();
	}

	public static synchronized void sendChatClient(MyChatClient chatClient) {
		MyApplication.chatClient = chatClient;
	}

	public static synchronized MyChatClient refreshChatClient() {
		return MyApplication.chatClient;
	}

	public static synchronized void chatClientDisconnect() {
		MyApplication.chatClient = null;
		Intent intent = new Intent(context, KeepAliveService.class);
		context.stopService(intent);

		// 发送广播消息，重连websocket
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Intent intentBroad = new Intent();
				intentBroad.setAction(Common.ACTION_REConnect_WebSocket);
				context.sendBroadcast(intentBroad);
			}
		}).start();
	}

	public static void createWebSocket(final String sid, final String token){
		if(MyApplication.chatClient != null){
			return;
		}
//		lock.lock();
//		if(IsCreate){
//			return;
//		}
//		IsCreate = true;
//		lock.unlock();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (MyApplication.chatClient != null) {
					return;
				}
				Logger.d("MyApplication", "MyApplication create chatclient");
				URI uri = null;
				try{
					uri = new URI(Common.RootChatUrl + sid + "/" + token);
				}catch (URISyntaxException e){
					e.printStackTrace();
				}
				new MyChatClient(uri, new MyChatClient.ChatClientCallBack() {

					@Override
					public void handle(int statudCode, String result) {
						if (MyChatClient.CODE_CONNECT_OK == statudCode) {
							Log.d("MyApplication", "Reconnect Success!");
							// 启动KeepAliveService
							Intent intent = new Intent(context, KeepAliveService.class);
							context.startService(intent);

							// 查询‘权威认证’状态变化
							DoctorInfoServiceImpl docInfoImpl = new DoctorInfoServiceImpl(
									context, null);
							DoctorInfo docInfo = docInfoImpl.get(MinePageActivity.LOCAL_DOCTOR_TB_ITEM_ID);
							if(docInfo.getAuth_status() != Common.AUTH_PASS){//权威认证还没有通过
								MineNetManager.authStatusCheck(context, docInfo.getAuth_status());
							}
						} else if ((MyChatClient.CODE_TIMEOUT == statudCode) || (MyChatClient.CODE_UNREACHABLE == statudCode)) {
							Logger.d("MyApplication", "Reconnect failed!" + result);
							// 网络问题无法连接，重连
//							if(isRunningForeground(context)){
//								Intent intent = new Intent(context, MainActivity.class);
//								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//								context.startActivity(intent);
//							}
						}else if(MyChatClient.CODE_TOKEN_INVALID == statudCode){// Token失效，把用户踢到登陆界面
							Logger.d("MyApplication", "Reconnect failed!" + result);
							// 正在运行时，才可创建Activity
							if(isRunningForeground(context)){
								Intent intent = new Intent(context, LoginActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent);
							}
						}
					}
				});
			}
		});
		thread.start();
	}

	private static boolean isRunningForeground (Context context) {
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if(!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
			return true ;
		}
		return false ;
	}

	public static void initImageLoader(Context context) {
		// 对UIL进行配置
		// 暂定配置，需要根据实际情况调整
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.memoryCache(new LruMemoryCache(50 * 1024 * 1024));
		config.diskCacheSize(100 * 1024 * 1024); // 100 MB
		config.tasksProcessingOrder(QueueProcessingType.FIFO);
		//if(Common.DEBUG) {
			config.writeDebugLogs();
		//}
		ImageLoader.getInstance().init(config.build());

	}

	public static void initSharedpreference() {
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		if (spManager.getOne("keyLatestTime").isEmpty()) {
			spManager.setOne("keyLatestTime", "0");
		}
		if (spManager.getOne("keyAtnLatestTime").isEmpty()) {
			spManager.setOne("keyAtnLatestTime", "0");
		}
	}

	public static void regiestBroadReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Common.ACTION_BOOT_COMPLETED);
		filter.addAction(Common.ACTION_CONNECTIVITY_CHANGE);
		filter.addAction(Common.ACTION_REConnect_WebSocket);
		context.registerReceiver(bootReceiver, filter);
	}

	// 运用list来保存们每一个activity是关键
	private List<Activity> mList = new LinkedList<Activity>();
	private static MyApplication instance;

	// 构造方法
	// 实例化一次
	public synchronized static MyApplication getInstance() {
		if (null == instance) {
			instance = new MyApplication();
		}
		return instance;
	}

	// add Activity
	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	// 关闭每一个list内的activity,退出程序
	public void exit() {
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MobclickAgent.onKillProcess(this);
			System.exit(0);
		}
	}

	// 关闭List中的Activity
	public void finishActivities(){
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateUserInfo(){
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		DoctorInfoService dis = new DoctorInfoServiceImpl(MyApplication.context, null);
		DoctorInfo di = dis.get(1);
		String users = spManager.getOne("keyUsers");
		String minePhone = spManager.getOne("keyPhone");
		JSONArray array;
		boolean flag = false;
		try {
			if(users == null || users.isEmpty()){
				array = new JSONArray();
			}
			else {
				array = new JSONArray(users);
				for (int i = 0; i < array.length(); i++) {
					JSONObject one = array.getJSONObject(i);
					String phone = one.get("phone").toString();
					if (phone.equals(minePhone)) {
						String thumbnail = di.getThumbnail();
						one.put("thumbnail", thumbnail);
						flag = true;
						break;
					}
				}
			}
			if(!flag){
				JSONObject one = new JSONObject();
				one.put("phone", minePhone);
				one.put("thumbnail", di.getThumbnail());
				array.put(one);
			}
			String arrayString = array.toString();
			spManager.setOne("keyUsers", arrayString);
			spManager.setOne("keyLastPhone", minePhone);
			spManager.setOne("keyLastThumb", di.getThumbnail());
		}
		catch(JSONException e){
			e.printStackTrace();
		}
	}

	public void updateTime(){
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		String times = spManager.getOne("keySynTimes");
		String minePhone = spManager.getOne("keyPhone");
		String msgTime = spManager.getOne("keyLatestTime");
		String atnTime = spManager.getOne("keyAtnLatestTime");
		JSONObject synTime = new JSONObject();
		if(times != null && !times.isEmpty()){
			try {
				synTime = new JSONObject(times);
			}
			catch(JSONException e){
				e.printStackTrace();
			}
		}
		try{
			JSONObject in = new JSONObject();
			in.put("msgTime", msgTime);
			in.put("atnTime", atnTime);
			if(synTime != null) {
				synTime.put(minePhone, in);
			}
			else{
				return;
			}
			spManager.setOne("keySynTimes", synTime.toString());
		}
		catch(JSONException e){
			e.printStackTrace();
		}
	}

	/*
	 * 统一做logout处理
	 */
	public void logout(){
		updateUserInfo();
		updateTime();
		SharedpreferenceManager spManager = SharedpreferenceManager.getInstance();
		spManager.remove("keySid");
		spManager.remove("keyToken");

		try {
			if(MyApplication.chatClient != null){
				MyApplication.chatClient.onClose();    // 主动断开websocket连接
			}
			Intent intent = new Intent(context, KeepAliveService.class);
			context.stopService(intent);
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		RunDataContainer.getContainer().destory(); // RunDataContainer
		WebMessageDispatcher.getDispatcher().destroy();

		// 清除通知栏信息
		NotifyManager.getManager().cancelAll();

		// 销毁当前页面，销毁相关界面
		MyApplication.getInstance().finishActivities();
	}
}
