package com.thenetcircle.comsumerdispatcher.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.thenetcircle.comsumerdispatcher.config.ConfigLoader;
import com.thenetcircle.comsumerdispatcher.config.DispatcherConfig;
import com.thenetcircle.comsumerdispatcher.config.MonitorConf;
import com.thenetcircle.comsumerdispatcher.config.QueueConf;
import com.thenetcircle.comsumerdispatcher.job.JobExecutor;
import com.thenetcircle.comsumerdispatcher.util.HttpUtil;

public class DistributedConfigLoader implements ConfigLoader {
	private static Log _logger = LogFactory.getLog(DistributedConfigLoader.class);
	
	protected ZooKeeper zk;

	public DistributedConfigLoader(ZooKeeper zk) {
		this.zk = zk;
	}
	
	@Override
	public Map<String, QueueConf> loadServers() {
		HashMap<String, QueueConf> servers = new HashMap<String, QueueConf>();
		List<String> queueServerNames;
		try {
			queueServerNames = zk.getChildren(DistributionTreeConstants.QSERVERS, false);
		} catch (Exception e) {
			_logger.error(e, e);
			return null;
		}
		
		for(String serverName : queueServerNames) {
			try {
				String host = getNodeValue(String.format(DistributionTreeConstants.QSERVERS_NAME_HOST, serverName));
				String port = getNodeValue(String.format(DistributionTreeConstants.QSERVERS_NAME_PORT, serverName));
				String pw = getNodeValue(String.format(DistributionTreeConstants.QSERVERS_NAME_PW, serverName));
				String user = getNodeValue(String.format(DistributionTreeConstants.QSERVERS_NAME_USER, serverName));
				String vhost = getNodeValue(String.format(DistributionTreeConstants.QSERVERS_NAME_VHOST, serverName));
				
				QueueConf qc = new QueueConf(serverName, host, Integer.valueOf(port), user, pw, vhost);
				servers.put(serverName, qc);
			} catch (Exception e) {
				_logger.error(e, e);
				continue;
			}
		}
		
		return servers;
	}

	@Override
	public List<JobExecutor> loadAllJobs() {
		ArrayList<JobExecutor> allJobs = new ArrayList<JobExecutor>();
		try {
			List<String> domains = zk.getChildren(DistributionTreeConstants.CD_ROOT, false);
		
			for(String domain : domains) {
				try {
					List<String> queueJobNodeNames = zk.getChildren(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN, domain), false);
					for (String queueJobNodeName : queueJobNodeNames) {
						try {
							String jobName = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER, domain, queueJobNodeName));
							String mqserver = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_MQSERVER, domain, queueJobNodeName));
							String count = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_COUNT, domain, queueJobNodeName));
							String url = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQURL, domain, queueJobNodeName));
							String host = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQHOST, domain, queueJobNodeName));
							String timeout = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQTIMEOUT, domain, queueJobNodeName));
							String prefetchCount = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQPREFETCH, domain, queueJobNodeName));
							String encoding = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_REQENCODING, domain, queueJobNodeName));
							String queueName = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_QUEUE, domain, queueJobNodeName));
							String exchange = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_EXCHANGE, domain, queueJobNodeName));
							String type = getNodeValue(String.format(DistributionTreeConstants.CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_TYPE, domain, queueJobNodeName));
							
							QueueConf qc = DispatcherConfig.getInstance().getServers().get(mqserver);
							JobExecutor je = new JobExecutor();
							try {
								je.setCount(Integer.valueOf(count));
							} catch (Exception e) {
								je.setCount(1);
							}
							je.setEncoding(encoding);
							je.setExchange(exchange);
							je.setFetcherQConf(qc);
							je.setName(jobName);
							je.setQueue(queueName);
							je.setTimeout(Integer.valueOf(timeout));
							je.setPrefetchCount(Integer.valueOf(prefetchCount));
							je.setType(type);
							je.setUrl(url);
							je.setUrlhost(host);
							
							allJobs.add(je);
							_logger.info("[Distributed Data Loader] loaded Job definition for " + queueJobNodeName + " on domain:" + domain);
						} catch (Exception e) {
							_logger.error(e, e);
							continue;						
						}
					}
				} catch (Exception e) {
					_logger.error(e, e);
					continue;
				}
			}
		} catch (Exception e) {
			_logger.error(e, e);
			return null;
		}
		return allJobs;
	}

	@Override
	public MonitorConf loadJmxConfig() {
		MonitorConf mc = new MonitorConf();
		String rmiPort = "9999";;
		try {
			rmiPort = getNodeValue(DistributionTreeConstants.MONITOR_RPORT);
		} catch (Exception e1) {
			_logger.error(e1, e1);
		}
		String httpPort = "8888";;
		try {
			httpPort = getNodeValue(DistributionTreeConstants.MONITOR_HTTPPORT);
		} catch (Exception e1) {
			_logger.error(e1, e1);
		}
		String hostname = HttpUtil.getLocalHostName();
		mc.setJmxHttpHost(hostname);
		mc.setJmxHttpPort(Integer.valueOf(httpPort));
		mc.setJmxRmiHost(hostname);
		mc.setJmxRmiPort(Integer.valueOf(rmiPort));
		return mc;
	}

	protected String getNodeValue(String path) throws KeeperException, InterruptedException {
		return new String(zk.getData(path, false, null));
	}
}
