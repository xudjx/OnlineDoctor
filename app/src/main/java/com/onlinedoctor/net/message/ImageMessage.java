package com.onlinedoctor.net.message;

import com.onlinedoctor.net.message.SimpleMessage;

public class ImageMessage extends SimpleMessage {

	public byte[] imageByte;

	public ImageMessage(String url, byte[] imageByte, HttpCallBack onSuccess, HttpFailedCallBack onFailed) {
		super(url, onSuccess, onFailed);
		this.imageByte = imageByte;
	}

}
