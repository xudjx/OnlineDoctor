package com.onlinedoctor.net;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.VersionXmlParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by wds on 15/11/28.
 */
public class VersionUpdateManager {

    private Context mContext;
    private HashMap<String, String> mHashMap;

    private static final int TIME_OUT = 5; //5秒超时

    // 服务器返回的apk安装包下载路径
    private static String appName = "yixingzhe.apk";
    private NotificationManager mNotificationManager = null;
    private Notification mNotification;
    private static final int UPDATE_CHECKCOMPLETED = 1;
    private static final int UPDATE_DOWNLOADING = 2;
    private static final int UPDATE_DOWNLOAD_ERROR = 3;
    private static final int UPDATE_DOWNLOAD_COMPLETED = 4;
    private static final int UPDATE_DOWNLOAD_CANCELED = 5;

    private int progress;
    private boolean isDownLoading = false;

    private boolean hasNewVersion = false;

    // 下载包安装本地存放路径
    private static final String savePath = Environment.getExternalStorageDirectory() + "/Download";
    private static final String saveFileName = savePath + '/' + appName;


    /**
     * 更新UI的Handler
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CHECKCOMPLETED:
                    showDownloadDialog();
                    break;
                case UPDATE_DOWNLOADING:
                    mNotification.contentView.setTextViewText(R.id.progress, progress + "%");
                    mNotification.contentView.setProgressBar(R.id.content_view_progress, 100, progress, false);
                    mNotificationManager.notify(0, mNotification);
                    break;
                case UPDATE_DOWNLOAD_COMPLETED:
                    mNotification.contentView.setTextViewText(R.id.progress, progress + "%");
                    mNotification.contentView.setProgressBar(R.id.content_view_progress, 100, 100, true);
                    mNotificationManager.notify(0, mNotification);
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };


    public VersionUpdateManager(Context mContext) {
        this.mContext = mContext;
    }


    public void checkUpdate() {
        hasNewVersion = false;

        new Thread(){
            @Override
            public void run() {
                InputStream inStream = getVersionFromNetwork();
                Logger.i("@@@@@@", ">>>>>>getVersionFromNetwork()");
                if(isUpdate(inStream)){
                    hasNewVersion = true;
                    handler.sendEmptyMessage(UPDATE_CHECKCOMPLETED);
                }
            }
        }.start();
    }

    /**
     * 检测是否是最新版本
     *
     * @return
     */
    public boolean isUpdate(InputStream inStream) {

        if (inStream == null) {
            Logger.e("获取version.xml信息失败");
            return false;
        }

        int localVersionCode = getVersionCode(mContext);

        try {
            mHashMap = VersionXmlParser.parseXml(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (null != mHashMap) {
            int serverVersionCode = Integer.valueOf(mHashMap.get(VersionXmlParser.TAG_VERSION));
            if (serverVersionCode > localVersionCode) {
                return true;
            }
        }
        return false;
    }


    /**
     * 从云上获取version.xml
     *
     * @return
     */
    private InputStream getVersionFromNetwork() {
        URL url;
        HttpURLConnection conn;
        InputStream inStream = null;
        try {
            url = new URL(Common.CHECK_VERSION_URL);
            conn = (HttpURLConnection) url.openConnection();
//            conn.setConnectTimeout(TIME_OUT * 1000);
            conn.setReadTimeout(TIME_OUT * 1000);
            conn.setRequestMethod("GET");
            conn.connect();
            inStream = conn.getInputStream();
//            inStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inStream;
    }

    /**
     * 下载进度对话框
     */
    public void showDownloadDialog() {
        if (isDownLoading) return;
        if (mNotification == null) {
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pintent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification = new Notification();
            mNotification.icon = R.drawable.ic_logo;
            mNotification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.download_progress);
            mNotification.contentIntent = pintent;
        }
        downloadApk();
    }

    /**
     * 启动线程下载apk
     */
    private void downloadApk() {
        Thread downLoadThread = new Thread(downApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装包下载线程
     */
    private Runnable downApkRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.v( "Download APK", "It comes here." );
            try {
                isDownLoading = true;
                URL url = new URL(mHashMap.get(VersionXmlParser.TAG_URL));
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.connect();
                //Log.v( "Download APK", "It comes here 2." );
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File file = new File(savePath);
                if (!file.exists()) {
                    if (!file.mkdir())
                        Log.v("Download", "Created directory at: " + savePath + " failed.");
                }
                //Log.v( "Download APK", "It comes here 3." );
                //String apkFile = saveFileName;
                //File ApkFile = new File(apkFile);
                //Log.v( "Download APK", "It comes here 4." );
                FileOutputStream fos = new FileOutputStream(saveFileName);
                //Log.v( "Download APK", "It comes here 5." );
                int count = 0;
                byte buf[] = new byte[1024];
                int _count = 0;
                do {
                    _count++;
                    int numread = is.read(buf);
                    count += numread;
                    Log.v("Download APK", numread + "has been read.");
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    if (_count % 100 == 0) handler.sendEmptyMessage(UPDATE_DOWNLOADING);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        handler.sendEmptyMessage(UPDATE_DOWNLOAD_COMPLETED);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true);// 点击取消就停止下载.
                fos.close();
                is.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isDownLoading = false;
            }
        }
    };

    /**
     * 安装APK
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            Log.v("installApk", "This APK file does not exist.");
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //安装完自动打开新版本app
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
        mNotificationManager.cancelAll();
    }

    /**
     * 获取本地versionCode
     *
     * @param context
     * @return
     */
    private static int getVersionCode(Context context) {
        int code = 0;

        try {
            code = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * 获取本地versionName
     *
     * @param context
     * @return
     */
    private static String getVersionName(Context context) {
        String name = "";

        try {
            name = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }
}

