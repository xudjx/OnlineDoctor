package com.onlinedoctor.net.message;

import com.onlinedoctor.net.message.SimpleMessage;

import org.apache.http.NameValuePair;

import java.util.List;

public class TextHttpsMessage extends SimpleMessage {

	public List<NameValuePair> params;

	public TextHttpsMessage(String url, List<NameValuePair> params, HttpCallBack onSuccess, HttpFailedCallBack onFailed) {
		super(url, onSuccess, onFailed);
		this.params = params;
	}

}
