package com.thenetcircle.comsumerdispatcher.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory{
	protected final String name;
	private final int priority;
	protected final AtomicInteger n = new AtomicInteger(1);

	public NamedThreadFactory(String id) {
		this(id, Thread.MIN_PRIORITY);
	}

	public NamedThreadFactory(String name, int priority) {
		this.name = name;
		this.priority = priority;
	}

	public Thread newThread(Runnable runnable) {
		String tName = name + ":" + n.getAndIncrement();
		Thread thread = new Thread(runnable, tName);
		thread.setPriority(priority);
		//thread.setDaemon(true);
		return thread;
	}

}
