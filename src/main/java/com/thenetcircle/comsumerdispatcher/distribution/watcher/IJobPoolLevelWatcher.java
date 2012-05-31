package com.thenetcircle.comsumerdispatcher.distribution.watcher;

import org.apache.zookeeper.Watcher;

import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;

public interface IJobPoolLevelWatcher extends Watcher {

	public void register(ConsumerJobExecutorPool pool);
}
