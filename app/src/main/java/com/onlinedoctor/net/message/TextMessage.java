package com.onlinedoctor.net.message;

import java.util.List;

import org.apache.http.NameValuePair;

public class TextMessage extends SimpleMessage {

	public List<NameValuePair> params;

	public TextMessage(String url, List<NameValuePair> params, HttpCallBack onSuccess, HttpFailedCallBack onFailed) {
		super(url, onSuccess, onFailed);
		this.params = params;
	}

}
