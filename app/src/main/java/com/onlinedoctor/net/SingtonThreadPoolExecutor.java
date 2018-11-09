package com.onlinedoctor.net;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingtonThreadPoolExecutor {

	private static ThreadPoolExecutor threadPool;
	
	private SingtonThreadPoolExecutor() {
	}
	
	public static ThreadPoolExecutor getThreadPool() {
		if (threadPool == null) {
			synchronized (SingtonThreadPoolExecutor.class) {
				if (threadPool == null) {
					BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
					threadPool = new ThreadPoolExecutor(5, 50, 1000, TimeUnit.MILLISECONDS, queue);
				}
			}
		}
		return threadPool;
	}
}
