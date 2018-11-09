package com.onlinedoctor.net;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.onlinedoctor.net.message.SimpleMessage;

public class MessageManager {

	private static MessageManager instance;
	public static final String TAG = "MessageManager";
	private BlockingQueue<SimpleMessage> queueMessages = new LinkedBlockingQueue<SimpleMessage>();
	private NetworkManager networkManager;

	// 启动生产者线程
	private MessageManager() {
		Log.i(TAG, "MessageManager Create");
		networkManager = NetworkManager.getNetworkManager();
		Productor productor = new Productor();
		productor.start();
	}

	public static MessageManager getMessageManager() {
		if (instance == null) {
			synchronized (MessageManager.class) {
				if (instance == null) {
					instance = new MessageManager();
				}
			}
		}
		return instance;
	}

	public class Consumer extends Thread {

		private SimpleMessage message;

		public Consumer(SimpleMessage message) {
			super();
			this.message = message;
		}

		@Override
		public void run() {
			try {
				queueMessages.put(message);
			} catch (InterruptedException e) {
				Log.e("Message Queue", "", e);
				Thread.interrupted();
			}
		}
	}

	public class Productor extends Thread {

		@Override
		public void run() {
			Log.i(TAG, "Productor Run");
			while (true) {
				Log.i(TAG, "Productor Running");
				try {
					SimpleMessage message = queueMessages.take();
					if (message != null) {
						Log.i(TAG, "Handle Message");
					}
					networkManager.send(message);
				} catch (InterruptedException e) {
					Log.e("Message Queue", "", e);
					Thread.interrupted();
				} catch (ClassNotFoundException classNotFoundException) {
					classNotFoundException.printStackTrace();
					Log.e("Message Queue", "cannot handler the message! Check the message type!",
							classNotFoundException);
				}
			}
		}

	}

	// 处理服务器异常
	public void processException(int errorCode, String errorMessage) throws ClassNotFoundException {
		Log.e(TAG, "errorCode=" + errorCode + ", ErrorMessage=" + errorMessage);
	}

	// 消息入口
	public void sendMessage(SimpleMessage message) {
		Consumer consumer = new Consumer(message);
		consumer.start();
	}
}
