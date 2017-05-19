package com.thenetcircle.comsumerdispatcher.distribution.watcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.thenetcircle.comsumerdispatcher.distribution.DistributionManager;
import com.thenetcircle.comsumerdispatcher.distribution.DistributionTreeConstants;
import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;

public class NewUrlWatcher extends BaseJobPoolLevelWatcher implements INewUrlWatcher {
	private static Log _logger = LogFactory.getLog(NewUrlWatcher.class);
	
	protected String newUrl;
	
	@Override
	public void register(ConsumerJobExecutorPool pool) {
		super.register(pool);
		if (DistributionManager.getInstance().isStandalone())
			return;
		
		_logger.info("[Distribution Watcher] going to register new url watcher...");
		
		mutex = new Integer(-1);
		try {
			watchOrGetNode(true);
		} catch (Exception e) {
			_logger.error("[Distribution NewURL Watcher] error while trying to watch." + e, e);
		}
	}

	@Override
	public void process(WatchedEvent event) {
		if(event.getType() == Watcher.Event.EventType.NodeDataChanged) {
			_logger.info("[Distribution NewURL Watcher] got NewURL event....");
			String eventUrl;
			try {
				eventUrl = watchOrGetNode(false);
				pool.refreshUrl(eventUrl);
			} catch (Exception e) {
				_logger.error("[Distribution NewURL Watcher] error while processing watched event" + e, e);
			}
		}
		try {
			watchOrGetNode(true);
		} catch (Exception e) {
			_logger.error(e, e);
		}
	}

	@Override
	protected String findTheNodePathToWatch() {
		return String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQURL, getDomainName(), pool.getJobDefinition().getLogicName());
	}

	@Override
	protected Watcher getWatcher() {
		return this;
	}

	@Override
	protected void doExecute() {
		if (DistributionManager.getInstance().isStandalone()) {
			pool.refreshUrl(newUrl);
		} else {
			try {
				zk.setData(findTheNodePathToWatch(), newUrl.getBytes(), -1);
			} catch (Exception e) {
				_logger.error("[Distribution CountChanged Watcher] error to set data for node: " + e, e);
			}
		}
	}

	@Override
	protected void preExecute() {
	}

	@Override
	protected void postExecute() {
	}
	
	public void setNewUrl(String url) {
		this.newUrl = url;
	}
}
