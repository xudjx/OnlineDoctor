package com.onlinedoctor.net.message;

import com.onlinedoctor.net.message.SimpleMessage;

import java.util.List;

import org.apache.http.NameValuePair;

public class GetVoiceMessage extends SimpleMessage {

	public List<NameValuePair> params;
	public HttpCallBack onSuccess;
	
	public GetVoiceMessage(String url, HttpCallBack onSuccess, HttpFailedCallBack onFailed) {
		// TODO Auto-generated constructor stub
		super(url, onFailed);
		this.onSuccess = onSuccess;
	}
	
	public boolean onSuccess(byte [] result){
		return (onSuccess != null) ? onSuccess.handle(result) : false;
	}
	
	public interface HttpCallBack{
		public boolean handle(byte [] result);
	}

}
