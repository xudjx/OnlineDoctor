package com.onlinedoctor.net;

import android.os.Handler;

public class HandlerManager {

	private static HandlerManager instance;
	private Handler chatHandler = null; // 聊天界面处理Handler
	private Handler patientHandler = null; // 患者界面处理Handler
	private Handler mainHandler = null; // 主界面刷新处理Handler
	private Handler messageHandler = null; // 消息界面处理Handler
	private Handler paymentHandler = null; // 支付消息成功处理Handler
	private Handler myWalletHandler = null; // 我的钱包消息处理Handler
	private Handler mineHandler = null; // 我的页面消息处理Handler
	private HandlerManager() {
	}

	public static HandlerManager getManager() {
		if (instance == null) {
			instance = new HandlerManager();
		}
		return instance;
	}

	public Handler getChatHandler() {
		return chatHandler;
	}

	public void setChatHandler(Handler chatHandler) {
		this.chatHandler = chatHandler;
	}

	public Handler getPatientHandler() {
		return patientHandler;
	}

	public void setPatientHandler(Handler patientHandler) {
		this.patientHandler = patientHandler;
	}

	public Handler getMainHandler() {
		return mainHandler;
	}

	public void setMainHandler(Handler mainHandler) {
		this.mainHandler = mainHandler;
	}

	public Handler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(Handler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public static HandlerManager getInstance() {
		return instance;
	}

	public static void setInstance(HandlerManager instance) {
		HandlerManager.instance = instance;
	}

	public Handler getPaymentHandler() {
		return paymentHandler;
	}

	public Handler getMyWalletHandler() {
		return myWalletHandler;
	}

	public void setMyWalletHandler(Handler myWalletHandler) {
		this.myWalletHandler = myWalletHandler;
	}

	public Handler getMineHandler() {
		return mineHandler;
	}

	public void setMineHandler(Handler mineHandler) {
		this.mineHandler = mineHandler;
	}

	public void setPaymentHandler(Handler paymentHandler) {
		this.paymentHandler = paymentHandler;
	}
}
