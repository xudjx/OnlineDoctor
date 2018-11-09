package com.onlinedoctor.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.onlinedoctor.net.MessageTimer;

public class MessageTimerTest {

	public void test() {
		final MessageTimer timer = MessageTimer.getInstance();
		final List<String> list = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			String guid = UUID.randomUUID().toString();
			list.add(guid);
			timer.addMsgTimer(guid, 30, new MessageTimer.RTOHandler() {
				@Override
				public void handle() {

				}
			});
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					timer.stop(list.get(4));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void main(String[] args) {
		MessageTimerTest test = new MessageTimerTest();
		test.test();
	}
}
