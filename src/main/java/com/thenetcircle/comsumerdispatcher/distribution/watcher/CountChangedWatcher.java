package com.thenetcircle.comsumerdispatcher.distribution.watcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;

import com.thenetcircle.comsumerdispatcher.distribution.DistributionManager;
import com.thenetcircle.comsumerdispatcher.distribution.DistributionTreeConstants;
import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;

public class CountChangedWatcher extends BaseJobPoolLevelWatcher implements ICountChangedWatcher {
	private static Log _logger = LogFactory.getLog(CountChangedWatcher.class);
	
	protected int numToSet = 0;
	protected String newSubNode = null;
	
	@Override
	public void register(ConsumerJobExecutorPool pool) {
		super.register(pool);
		if (DistributionManager.getInstance().isStandalone())
			return;
		
		_logger.info("[Distribution Watcher] going to register count watcher...");
		
		mutex = new Integer(-1);
		try {
			watchOrGetNode(true);
		} catch (Exception e) {
			_logger.error("[Distribution CountChanged Watcher] error while trying to watch." + e, e);
		}
	}

	@Override
	public void process(WatchedEvent event) {
		if(event.getType() == Watcher.Event.EventType.NodeDataChanged) {
			_logger.info("[Distribution CountChanged Watcher] got countchanged event....");
			String numStr;
			try {
				numStr = watchOrGetNode(false);
				pool.setJobExecutorNum(Integer.valueOf(numStr));
				
				
				if (pool.getActiveJobExecutorCount() <= 0) {
					// if all threads stopped in the pool, give signal of purging by adding one child under purge node;
					String purgeSubNode = String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING_SUBNODES, getDomainName(), pool.getJobDefinition().getLogicName());
					newSubNode = zk.create(purgeSubNode, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					_logger.info("[Distribution CountChanged Watcher] since no worker in pool for queue" + getQueueJobNodeName() + " added subnode to purge node: " + newSubNode);
				} else {
					if(newSubNode != null && zk.exists(newSubNode, false) != null) {
						_logger.info("[Distribution CountChanged Watcher] goint to delete corresponding not running child node: " + newSubNode);
						zk.delete(newSubNode, -1);
					}
				}
			} catch (Exception e) {
				_logger.error("[Distribution CountChanged Watcher] error while processing watched event" + e, e);
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
		return String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_COUNT, getDomainName(), pool.getJobDefinition().getLogicName());
	}

	@Override
	protected Watcher getWatcher() {
		return this;
	}

	@Override
	protected void doExecute() {
		if (DistributionManager.getInstance().isStandalone()) {
			pool.setJobExecutorNum(Integer.valueOf(numToSet));
		} else {
			try {
				zk.setData(findTheNodePathToWatch(), String.valueOf(numToSet).getBytes(), -1);
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

	/**
	 * the number of jobs to be removed (- value) or added (+ value)
	 * @param numToExecute
	 */
	public void setNumToSet(int deltaToExecute) {
		this.numToSet = deltaToExecute;
	}
}
