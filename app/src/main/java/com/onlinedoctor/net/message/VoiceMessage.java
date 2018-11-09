package com.onlinedoctor.net.message;

public class VoiceMessage extends SimpleMessage {

	public byte[] voiceByte;

	public VoiceMessage(String url, byte[] voiceByte, HttpCallBack onSuccess, HttpFailedCallBack onFailed) {
		super(url, onSuccess, onFailed);
		this.voiceByte = voiceByte;
	}

}
