package com.thenetcircle.comsumerdispatcher.distribution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;

import com.thenetcircle.comsumerdispatcher.job.JobExecutor;
import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;

public class WatchersRegister {
	private static Log _logger = LogFactory.getLog(WatchersRegister.class);
	
	protected ZooKeeper zk;
	
	public WatchersRegister(ZooKeeper zk) {
		this.zk = zk;
	}
	
	public void register(JobExecutor job, ConsumerJobExecutorPool pool) {
		if (DistributionManager.getInstance().isStandalone())
			return;
		
		_logger.info("[Distribution Watcher] going to register watchers...");
		
	
	}
	
	protected void executeRegistion() {
	}
}
