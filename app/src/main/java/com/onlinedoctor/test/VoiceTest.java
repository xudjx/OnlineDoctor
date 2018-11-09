package com.onlinedoctor.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.net.MessageManager;
import com.onlinedoctor.net.message.SimpleMessage.HttpCallBack;
import com.onlinedoctor.net.message.SimpleMessage.HttpFailedCallBack;
import com.onlinedoctor.net.message.VoiceMessage;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.util.VoiceUtil;

public class VoiceTest {

	private Context context;

	public VoiceTest(Context context) {
		super();
		this.context = context;
	}

	public void getVoiceFile(String voiceUrl) {
		String audioPath = VoiceUtil.saveAudioByUrl(voiceUrl);
		Log.i("Voice", audioPath);
	}

	public void postVoiceToServer() {
		MessageManager manager = MessageManager.getMessageManager();
		byte[] byteArray = readRawFile().getBytes();
		VoiceMessage tm = new VoiceMessage(Common.VoiceHttpUrl, byteArray, new HttpCallBack() {
			@Override
			public boolean handle(String result) {
				Gson gson = new Gson();
				Type type = new TypeToken<Map<String, Object>>() {
				}.getType();
				Map<String, Object> map = gson.fromJson(result, type);
				Log.d("VoiceBack", result);
				Map<String, Object> dataMap = (Map<String, Object>) map.get(Common.data);
				String audioPath = (String) dataMap.get(Common.KEY_VOICE_voicePath);
				getVoiceFile(audioPath);
				return true;
			}
		}, new HttpFailedCallBack() {

			@Override
			public boolean handle(int errorCode, String errorMessage) {
				Log.i("OnFailed", "ErrorCode=" + errorCode + ", ErrorMessage=" + errorMessage + "]");
				return false;
			}
		});
		manager.sendMessage(tm);
	}

	String readRawFile() {
		String content = null;
		Resources resources = context.getResources();
		InputStream is = null;
		try {
			is = resources.openRawResource(R.raw.gg);
			byte buffer[] = new byte[is.available()];
			is.read(buffer);
			content = new String(buffer);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return content;
	}

}
