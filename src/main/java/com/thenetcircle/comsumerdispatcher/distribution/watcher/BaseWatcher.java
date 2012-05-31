package com.thenetcircle.comsumerdispatcher.distribution.watcher;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public abstract class BaseWatcher {

	protected ZooKeeper zk;
	
	protected String watchOrGetNode(boolean watch) throws KeeperException, InterruptedException {
		String node = findTheNodePathToWatch();
		byte[] b;
		if (watch)
			b = zk.getData(node, getWatcher(), null);
		else
			b = zk.getData(node, false, null);
		return new String(b);
	}
	
	protected abstract String findTheNodePathToWatch();
	
	protected abstract Watcher getWatcher();
}
