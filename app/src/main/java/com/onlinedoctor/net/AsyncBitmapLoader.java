package com.onlinedoctor.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.onlinedoctor.pojo.Common;

/**
 * 图片异步加载时的工具类
 */
public class AsyncBitmapLoader {

	/**
	 * 图片软引用
	 */
	private HashMap<String, SoftReference<Bitmap>> imageCache = null;
	private static String tempsave = Common.sdTempSave;

	public AsyncBitmapLoader() {
		Log.i("cat--->", "Create AsyncBitmapLoader");
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	public void loadBitmap(final View view, final String imageURL, final ImageCallBack imageCallBack) {
		if (imageURL == null || imageCallBack == null) {
			return;
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				imageCallBack.imageLoad(view, (Bitmap) msg.obj);
			}
		};

		// Find from memory cache
		if (imageCache.containsKey(imageURL)) {
			SoftReference<Bitmap> reference = imageCache.get(imageURL);
			Bitmap bitmap = reference.get();
			if (bitmap != null) {
				Log.i("cat-->", "Success get pic From imageCache!!");
				imageCallBack.imageLoad(view, bitmap);
				return;
			}
		} else {
			// Find from SD card
			String bitmapName = imageURL.substring(imageURL.lastIndexOf("/") + 1);
			System.out.println("bitmapName--->" + bitmapName);

			File cacheDir = new File(tempsave);

			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			} else {
				File[] cacheFiles = cacheDir.listFiles();

				int i = 0;
				int j = cacheFiles.length;
				for (; i < j; i++) {

					if (bitmapName.equals(cacheFiles[i].getName())) {
						break;
					}
					continue;
				}

				if (i < cacheFiles.length) {
					Bitmap bitmap = BitmapFactory.decodeFile(tempsave + File.separator + bitmapName);
					if (bitmap != null) {
						imageCallBack.imageLoad(view, bitmap);
						SoftReference<Bitmap> reference = new SoftReference<Bitmap>(bitmap);
						imageCache.put(imageURL, reference);
						return;
					}
				}
			}
		}
		new Thread(new Runnable() {
			public void run() {

				try {
					URL url = null;
					InputStream is = null;
					try {
						url = new URL(imageURL);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					try {
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setConnectTimeout(5 * 1000);
						is = conn.getInputStream();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					String imgPathString = tempsave + File.separator
							+ imageURL.substring(imageURL.lastIndexOf("/") + 1);
					File bitmapFile = new File(imgPathString);
					FileOutputStream fos = new FileOutputStream(bitmapFile);
					byte[] buffer = new byte[65534];
					int bytesRead;
					try {
						while ((bytesRead = is.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
						fos.flush();
						fos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Message msg = new Message();
					msg.obj = bitmap;
					handler.sendMessage(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	// 回调接口
	public interface ImageCallBack {
		public void imageLoad(View view, Bitmap bitmap);
	}

}
