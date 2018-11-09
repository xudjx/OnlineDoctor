package com.onlinedoctor.net.message;

public class SimpleMessage {
	public String url;
	public HttpCallBack onSuccess;
	public HttpFailedCallBack onFailed;

	public SimpleMessage(String url, HttpCallBack onSuccess, HttpFailedCallBack onFailed) {
		super();
		this.url = url;
		this.onSuccess = onSuccess;
		this.onFailed = onFailed;
	}
	
	public SimpleMessage(String url, HttpFailedCallBack onFailed){
		super();
		this.url = url;
		this.onFailed = onFailed;
	}

	public boolean onSuccess(String result) {
		return (onSuccess != null) ? onSuccess.handle(result) : false;
	}

	public boolean onFailed(int errorCode, String message) {
		return (onFailed != null) ? onFailed.handle(errorCode, message) : false;
	}

	public interface HttpCallBack {
		public boolean handle(String result);
	}

	public interface HttpFailedCallBack {
		public boolean handle(int errorCode, String errorMessage);
	}

}
