package com.onlinedoctor.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.onlinedoctor.activity.MainActivity;
import com.onlinedoctor.activity.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Song on 2015/8/11.
 */
public class UpdateManager {
    private Context context;
    // 发起版本检测的url
    private final static String CHECKURL = "http://loyalty.chinacloudapp.cn/customcore/appversion.php";
    // 服务器返回的apk安装包下载路径
    private static String appName = "zhongyijia.apk";
    public static String downloadUrl = "http://loyalty.chinacloudapp.cn/public/app/" + appName;
    private NotificationManager mNotificationManager = null;
    private Notification mNotification;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private int progress;
    private boolean isDownLoading = false;
    // 下载包安装本地存放路径
    private static final String savePath = Environment
            .getExternalStorageDirectory() + "/Download";
    private static final String saveFileName = savePath + '/' + appName;

    public UpdateManager(Context context) {
        this.context = context;
    }

    /**
     * 更新UI的Handler
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mNotification.contentView.setTextViewText(R.id.progress, progress + "%");
                    mNotification.contentView.setProgressBar(R.id.content_view_progress, 100, progress, false);
                    mNotificationManager.notify(0, mNotification);
                    break;
                case DOWN_OVER:
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


    /**
     * 下载进度对话框
     */
    public void showDownloadDialog() {
        if (isDownLoading) return;
        if (mNotification == null) {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pintent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotification = new Notification();
            mNotification.icon = R.drawable.ic_logo;
            mNotification.contentView = new RemoteViews(context.getPackageName(), R.layout.download_progress);
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
                URL url = new URL(downloadUrl);
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
                    if (_count % 100 == 0) handler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        handler.sendEmptyMessage(DOWN_OVER);
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
        mNotificationManager.cancelAll();
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            Log.v("installApk", "This APK file does not exist.");
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        context.startActivity(i);
    }
}
