package com.onlinedoctor.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.widget.Toast;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;

/**
 * 提供一些
 */
public class VoiceUtil {

	private static MediaPlayer mMediaPlayer = new MediaPlayer();

	public static boolean exists(String audioPath) {
		File file = new File(audioPath);
		return file.exists();
	}

	/**
	 * @Description 只需要传入audio name，会去SD卡默认的存储位置进行查找
	 * @param audio
	 *            name
	 */
	public static void playMusic(String audioPath) {
		File file = new File(audioPath);
		if (!file.exists()) {
			Toast.makeText(MyApplication.context, "语音文件已被删除!", Toast.LENGTH_LONG).show();
			return;
		}
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(audioPath);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					mp.stop();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Description 停止MediaPlayer
	 */
	public static void stopPlay(){
		if(mMediaPlayer != null){
			mMediaPlayer.stop();
		}
	}

	/**
	 * 得到amr的时长
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static long getAmrDuration(String audioPath) {
		File file = new File(audioPath);
		long length = file.length();
		long duration = -1;
		int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0 };
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
			int pos = 6;
			int frameCount = 0;
			int packedPos = -1;
			byte[] datas = new byte[1];
			while (pos <= length) {
				randomAccessFile.seek(pos);
				if (randomAccessFile.read(datas, 0, 1) != 1) {
					duration = length > 0 ? ((length - 6) / 650) : 0;
					break;
				}
				packedPos = (datas[0] >> 3) & 0x0F;
				pos += packedSize[packedPos] + 1;
				frameCount++;
			}
			duration += frameCount * 20;
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return duration;
	}

	/**
	 * 得到amr的时长,通过MediaPlayer
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static long getDuration(String audioPath){
		MediaPlayer mp = MediaPlayer.create(MyApplication.context, Uri.parse(audioPath));
		int duration = mp.getDuration();
		return duration;
	}

	/**
	 * 根据传入的VoicePath获取amr数据byte
	 */
	public static byte[] readAudio(String audioPath) throws IOException {
		File file = new File(audioPath);
		if (audioPath == null || audioPath.equals("")) {
			throw new NullPointerException("无效的音频文件路径");
		}
		long len = file.length();
		byte[] bytes = new byte[(int) len];
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
		int r = bufferedInputStream.read(bytes);
		if (r != len)
			throw new IOException("读取文件不正确");
		bufferedInputStream.close();
		return bytes;
	}

	/**
	 * 传入Voice文件的Url进行下载
	 */
	public static String saveAudioByUrl(final String voiceUrl) {
		Logger.d("VoiceUrl", voiceUrl);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		Future<String> future = pool.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				String voicePath = null;
				try {
					URL url = null;
					InputStream is = null;
					try {
						url = new URL(voiceUrl);
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
					voicePath = Common.sdAudioSave + File.separator + voiceUrl.substring(voiceUrl.lastIndexOf("/") + 1);
					File file = new File(voicePath);
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buffer = new byte[2048];
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
				} catch (IOException e) {
					e.printStackTrace();
				}
				return voicePath;
			}
		});
		String voicePath = "";
		try {
			voicePath = future.get().toString();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return voicePath;
	}

}
