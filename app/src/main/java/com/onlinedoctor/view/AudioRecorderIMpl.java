package com.onlinedoctor.view;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaRecorder;

import com.onlinedoctor.pojo.Common;

public class AudioRecorderIMpl implements AudioRecorder {

	private MediaRecorder recorder;
	public String fileName;
	public String fileFolder = Common.sdAudioSave;

	private boolean isRecording = false;

	@Override
	public void ready() {
		// TODO Auto-generated method stub
		File file = new File(fileFolder);

		if (!file.exists()) {
			file.mkdirs();
		}
		
		fileName = getCurrentDate();
		recorder = new MediaRecorder();
		recorder.setOutputFile(fileFolder + "/" + fileName + ".amr");
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置MediaRecorder的音频源为麦克风
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);// 设置MediaRecorder录制的音频格式
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 设置MediaRecorder录制音频的编码为amr
	}

	// 以当前时间作为文件名
	private String getCurrentDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}

	@Override
	public void start() {
		if (!isRecording) {
			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			isRecording = true;
		}

	}

	@Override
	public void stop() {
		if (isRecording) {
			recorder.stop();
			recorder.release();
			isRecording = false;
		}

	}

	@Override
	public void deleteOldFile() {
		File file = new File(fileFolder + "/" + fileName + ".amr");
		file.deleteOnExit();
	}

	public String getFilePath() {
		return fileFolder + "/" + fileName + ".amr";
	}

	@Override
	public double getAmplitude() {
		if (!isRecording) {
			return 0;
		}
		return recorder.getMaxAmplitude();
	}

}
