package com.thenetcircle.comsumerdispatcher.distribution;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.thenetcircle.comsumerdispatcher.config.DispatcherConfig;
import com.thenetcircle.comsumerdispatcher.config.MonitorConf;
import com.thenetcircle.comsumerdispatcher.config.QueueConf;
import com.thenetcircle.comsumerdispatcher.job.JobExecutor;
import com.thenetcircle.comsumerdispatcher.util.HttpUtil;

public class DistributionDataPopulator {
	private static Log _logger = LogFactory.getLog(DistributionDataPopulator.class);

	public void populateQueueServers(ZooKeeper zk) throws KeeperException, InterruptedException {
		createNode(zk, DistributionTreeConstants.QSERVERS, "queue servers");
		
		Map<String, QueueConf> servers = DispatcherConfig.getInstance().getServers();
		for(String serverName : servers.keySet()) {
			QueueConf qc = servers.get(serverName);
			createNode(zk, String.format(DistributionTreeConstants.QSERVERS_NAME, serverName), serverName);
			createNode(zk, String.format(DistributionTreeConstants.QSERVERS_NAME_HOST, serverName), qc.getHost());
			createNode(zk, String.format(DistributionTreeConstants.QSERVERS_NAME_PORT, serverName), String.valueOf(qc.getPort()));
			createNode(zk, String.format(DistributionTreeConstants.QSERVERS_NAME_PW, serverName), qc.getPassword());
			createNode(zk, String.format(DistributionTreeConstants.QSERVERS_NAME_USER, serverName), qc.getUserName());
			createNode(zk, String.format(DistributionTreeConstants.QSERVERS_NAME_VHOST, serverName), qc.getVhost());
			
			String logStr = String.format(">>>>>>>>>>>>>>>>>>>>>>>[Distribution data population] populated queue server data: name:%s,host:%s,post:%s,vhost:%s", serverName, qc.getHost(), String.valueOf(qc.getPort()), qc.getVhost());
			_logger.info(logStr);
		}
	}
	
	public void populateMembers(ZooKeeper zk) throws KeeperException, InterruptedException {
		createNode(zk, DistributionTreeConstants.JOINED_MEMBERS, "live members");
	}
	
	public void populateJobsForDomains(ZooKeeper zk) throws KeeperException, InterruptedException {
		createNode(zk, DistributionTreeConstants.CD_ROOT, "consumer dispatcher");
		
		List<JobExecutor> jobs = DispatcherConfig.getInstance().getAllJobs();
		for (JobExecutor je : jobs) {
			// create domain
			String domain;
			try {
				domain = HttpUtil.convertUrlToHostNameAsNodeName(je.getUrl());
			} catch (MalformedURLException e) {
				continue;
			}
			String hostNodeName = String.format(DistributionTreeConstants.CD_ROOT_DOMAIN, domain);
			if (null == zk.exists(hostNodeName, false)) {
				createNode(zk, hostNodeName, je.getUrlhost());
			}
			
			// create job definition
			String queueJobNodeName = je.getLogicName();
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER, domain, queueJobNodeName), je.getName());
			// add mqserver
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_MQSERVER, domain, queueJobNodeName), je.getFetcherQConf().getName());
			// add request count
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_COUNT, domain, queueJobNodeName), String.valueOf(je.getCount()));
			// add requrl node
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQURL, domain, queueJobNodeName), je.getUrl());
			// add reqhost node
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQHOST, domain, queueJobNodeName), je.getUrlhost());
			// add timeout node
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQTIMEOUT, domain, queueJobNodeName), String.valueOf(je.getTimeout()));
			// add encoding node
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQENCODING, domain, queueJobNodeName), je.getEncoding());
			// create EPHEMERAL_SEQUENTIAL purge node, so that the other application can add sub nodes to it later after they stop all threads
			String purgeNode = String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING, domain, queueJobNodeName);
			createNode(zk, purgeNode, "");
			/// add logerrorfile node
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_LOGERRORFILE, domain, queueJobNodeName), "0");
			// add binding ode
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS, domain, queueJobNodeName), "");
			// add binding queue
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_QUEUE, domain, queueJobNodeName), je.getQueue());
			// add binding exchange
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_EXCHANGE, domain, queueJobNodeName), je.getExchange());
			// add binding type
			createNode(zk, String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_TYPE, domain, queueJobNodeName), je.getType());
			
			_logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>[Distribution data population] finished one job definition, path: " + queueJobNodeName);
		}
		
	}
	
	public void populateMonitorData(ZooKeeper zk) throws KeeperException, InterruptedException {
		MonitorConf mc = DispatcherConfig.getInstance().getMonitorConf();
		createNode(zk, DistributionTreeConstants.MONITOR, "monitoring config");
		createNode(zk, DistributionTreeConstants.MONITOR_RHOST, ""); // doesnt make sense to share
		createNode(zk, DistributionTreeConstants.MONITOR_RPORT, String.valueOf(mc.getJmxRmiPort()));
		createNode(zk, DistributionTreeConstants.MONITOR_HTTPHOST, ""); // doesnt make sense to share
		createNode(zk, DistributionTreeConstants.MONITOR_HTTPPORT, String.valueOf(mc.getJmxHttpPort()));
		
	}
	
	protected void createNode(ZooKeeper zk, String path, String value) throws KeeperException, InterruptedException {
		if(null == value) value = "";
		zk.create(path, value.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		_logger.info(String.format("[Distribution data population] populated data: path:%s, value:%s", path, value));
	}
}
