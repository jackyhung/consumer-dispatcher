package com.thenetcircle.comsumerdispatcher.distribution.watcher;

import java.net.MalformedURLException;

import com.thenetcircle.comsumerdispatcher.distribution.DistributionManager;
import com.thenetcircle.comsumerdispatcher.distribution.DistributionTreeConstants;
import com.thenetcircle.comsumerdispatcher.job.JobExecutor;
import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;
import com.thenetcircle.comsumerdispatcher.util.HttpUtil;

public abstract class BaseJobPoolLevelWatcher extends BaseWatcher {
	protected static Integer mutex;
	protected ConsumerJobExecutorPool pool;
	
	public void register(ConsumerJobExecutorPool pool) {
		this.pool = pool;
		this.zk = DistributionManager.getInstance().getZk();
	}
	
	protected String getQueueJobNodeName() {
		JobExecutor job = pool.getJobDefinition();
		return String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER, getDomainName(), job.getLogicName());
	}
	
	protected String getDomainName() {
		JobExecutor job = pool.getJobDefinition();
		String domain = null;
		try {
			domain = HttpUtil.convertUrlToHostNameAsNodeName(job.getUrl());
		} catch (MalformedURLException e) {
		}
		return domain;
	}
	
	public void execute() {
		preExecute();
		doExecute();
		postExecute();
	}

	protected abstract void doExecute();
	protected abstract void preExecute();
	protected abstract void postExecute();
}
