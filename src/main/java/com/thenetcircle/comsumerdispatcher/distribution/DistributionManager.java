package com.thenetcircle.comsumerdispatcher.distribution;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

import com.thenetcircle.comsumerdispatcher.util.HttpUtil;

public class DistributionManager {
	private static Log _logger = LogFactory.getLog(DistributionManager.class);
	
	private static DistributionManager _self = null;
	
	protected Thread distributionThread;
	protected ServerConfig sc;
	protected ZooKeeper zk;
	
	/**
	 * the type of how the application is running as.
	 * -1: stand alone
	 *  1: distribution client
	 *  2: distribution server
	 */
	protected int runType = RUNTYPE_STANDALONE;
	public static int RUNTYPE_STANDALONE = -1;
	public static int RUNTYPE_DIST_CLIENT = 1;
	public static int RUNTYPE_DIST_SERVER = 2;
	
	public synchronized static DistributionManager getInstance() {
		if (null == _self) {
			_self = new DistributionManager();
		}
		return _self;
	}
	
	public boolean isDistributionMaster() {
		return  (runType == RUNTYPE_DIST_SERVER);  
	}
	
	public boolean isDistributionClient() {
		return (runType == RUNTYPE_DIST_CLIENT);
	}
	
	public boolean isStandalone() {
		return (runType == RUNTYPE_STANDALONE);
	}
	
	public ZooKeeper getZk() {
		return zk;
	}

	public void populateDataTree() {
		if (isDistributionMaster()) {
			try {
				DistributionDataPopulator pop = new DistributionDataPopulator();
				pop.populateQueueServers(zk);
				pop.populateJobsForDomains(zk);
				pop.populateMembers(zk);
				pop.populateMonitorData(zk);
				_logger.info("[Distribution Population] finished successfully.");
			} catch (Exception e) {
				cleanIfStartsError();
				_logger.error("[Distribution] populating data failed: " + e, e);
			}
		}
	}
	
	private DistributionManager() {
		String serverConfigPath = System.getProperty("distserver.cfg", null);
		String clientConfigPath = System.getProperty("distclient.address", null);
		if (serverConfigPath == null && clientConfigPath == null) {
			_logger.info("[Distribution] starts up as standalone, will laod data from job.xml....");
			runType = RUNTYPE_STANDALONE;
			
			return;
		}
		
		// the followings are for distributed version
		if (clientConfigPath != null) {
			_logger.info("[Distribution] starts up as client, will laod data from distribution server....");
			runType = RUNTYPE_DIST_CLIENT;
			initZkConnection(clientConfigPath);
			
			while(getLivingJoinedMemberNum() < 1) {
				_logger.info("[Distribution] seems master is not up. wait for master to finishe starting up....");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
		} else if (serverConfigPath != null) {
			_logger.info("[Distribution] starts up as master, needs to start distribution server and populate data to server ....");
			runType = RUNTYPE_DIST_SERVER; // this is master, need to start distribution server and populate data to server
			loadDistributionServerConfig(serverConfigPath);
			
			startupServer();
			
			initZkConnection(sc.getClientPortAddress().getHostName() + ":" + sc.getClientPortAddress().getPort());
			
			// populate data to server
			populateDataTree();
		}
		
		updateJoinedMemberNode();
	}
	
	protected void updateJoinedMemberNode() {
		String hostname = HttpUtil.getLocalHostName();
		try {
			zk.create(DistributionTreeConstants.JOINED_MEMBERS + "/" + hostname, hostname.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (Exception e) {
			_logger.error(e, e);
		}
	}
	
	public int getLivingJoinedMemberNum() {
		int size = 0;
		try {
			size = zk.getChildren(DistributionTreeConstants.JOINED_MEMBERS, false).size();
		} catch (Exception e) {
			_logger.error(e, e);
		}
		return size;
	}

	protected void initZkConnection(String hostPort) {
		// init the zk connnection
		try {
			zk = new ZooKeeper(hostPort, 500000, null);
		} catch (IOException e) {
			_logger.error("[Distribution Manager] error on inilization: " + e, e);
		}
	}
	
	protected void loadDistributionServerConfig(final String configPath) {
		sc = new ServerConfig();
		try {
			sc.parse(configPath);
		} catch (ConfigException e) {
			_logger.error("[Distribution] error while loading distribution config: " + configPath + e, e);
		}
	}
	
	public void cleanTheOldServerData() {
		try {
			FileUtils.deleteDirectory(new File(sc.getDataDir()));
		} catch (IOException e) {
			_logger.error(e, e);
		}
	}
	
	protected void startupServer() {
		if (isDistributionMaster()) {
			cleanTheOldServerData();
			distributionThread = new Thread() {
				@Override
				public void run() {
					try {
						ZooKeeperServerMain zs = new ZooKeeperServerMain();
						zs.runFromConfig(sc);
						_logger.info("[Distribution]  distribution server started up.... host:" + sc.getClientPortAddress().getHostName() + ";address:" + sc.getClientPortAddress().getAddress().getHostAddress() + ";port:" + sc.getClientPortAddress().getPort());
					} catch (Exception e) {
						_logger.error(e, e);
						cleanIfStartsError();
					}
					
					_logger.info("[Distribution]  distribution server shutdown....");
				}
			};
			
			distributionThread.setDaemon(true);
			distributionThread.start();
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				_logger.error(e, e);
			}
		}
	}
	
	protected void cleanIfStartsError() {
		distributionThread = null;
	}
}
