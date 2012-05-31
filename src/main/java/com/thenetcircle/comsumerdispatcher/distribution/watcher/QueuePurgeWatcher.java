package com.thenetcircle.comsumerdispatcher.distribution.watcher;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.thenetcircle.comsumerdispatcher.distribution.DistributionManager;
import com.thenetcircle.comsumerdispatcher.distribution.DistributionTreeConstants;
import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;
import com.thenetcircle.comsumerdispatcher.util.QueueUtil;

public class QueuePurgeWatcher extends BaseJobPoolLevelWatcher implements IQueuePurgeWatcher {
	private static Log _logger = LogFactory.getLog(QueuePurgeWatcher.class);
	
	@Override
	public void register(ConsumerJobExecutorPool pool) {
		super.register(pool);
		if (DistributionManager.getInstance().isStandalone())
			return;
		
		_logger.info("[Distribution Watcher] going to register queue purge watcher...");
		
		mutex = new Integer(-1);
		try {
			//watchOrGetNode(true);
		} catch (Exception e) {
			_logger.error("[Distribution Purge Watcher] error while trying to watch." + e, e);
		}
	}
	
	
	
	@Override
	protected void preExecute() {
		if (DistributionManager.getInstance().isStandalone()) {
			pool.stopAllExecutors();
		} else {
			// use events to stop all threads
			try {
				// set count node to 0 so that all applications will stop all workers
				try {
					_logger.info("[Distribution Purge Watcher] set the count node to 0 so that to stop all running workers... ");
					String countNode = String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_COUNT, getDomainName(), pool.getJobDefinition().getLogicName());
					zk.setData(countNode, "0".getBytes(), -1);
				} catch (Exception e) {
					_logger.error("[Distribution Purge Watcher] error to set value of 0 to count node: " + e, e);
				}
				
				while (true) {
	                synchronized (mutex) {
	                	// watch on not-running node
	                    List<String> list = zk.getChildren(findTheNodePathToWatch(), new Watcher() {
							@Override
							public synchronized void process(WatchedEvent event) {
								_logger.info("[Distribution Purge Watcher] received one event...");
								synchronized (mutex) {
						            mutex.notify();
						        }								
							}
	                    	
	                    });

	                    if (list.size() < DistributionManager.getInstance().getLivingJoinedMemberNum()) {
	                        mutex.wait(); //TODO FIXME while start the application up, if the count is alread set to 0, need to add 'CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING_SUBNODES' expicitly 
	                    } else {
	                        break; // enough nodes, out of loop. meaning all running worker stopped, can continue to purge
	                    }
	                }
	            }
			} catch (Exception e) {
				_logger.error("[Distribution Purge Watcher] prePurge failed for job: " + pool.getJobDefinition().getLogicName() + " on domain: " + pool.getJobDefinition().getUrl() + e, e);
				throw new RuntimeException("[Distribution Purge Watcher] prePurge failed", e);
			}
		}
	}
	

	@Override
	protected void doExecute() {
		QueueUtil.purgeQueue(pool.getJobDefinition());
	}
	
	@Override
	protected void postExecute() {
		if (DistributionManager.getInstance().isStandalone()) {
			pool.startJobExecutors();
		} else {
			String num = String.valueOf(pool.getJobDefinition().getCount());
			_logger.info("[Distribution Purge Watcher] set the count node to " + num + " so that all workers start running ... ");
			String countNode = String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_COUNT, getDomainName(), pool.getJobDefinition().getLogicName());
			try {
				zk.setData(countNode, num.getBytes(), -1);
			} catch (Exception e) {
				_logger.error("[Distribution Purge Watcher] error when trying to set new count value after purging queue: " + e, e);
			}
		}
	}
	
	@Override
	public void process(WatchedEvent event) {
	}
	
	@Override
	protected String findTheNodePathToWatch() {
		return String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING, getDomainName(), pool.getJobDefinition().getLogicName());
	}

	@Override
	protected Watcher getWatcher() {
		return this;
	}
}
