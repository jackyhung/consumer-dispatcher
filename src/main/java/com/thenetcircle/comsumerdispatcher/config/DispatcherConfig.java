package com.thenetcircle.comsumerdispatcher.config;

import java.util.List;
import java.util.Map;

import com.thenetcircle.comsumerdispatcher.distribution.DistributedConfigLoader;
import com.thenetcircle.comsumerdispatcher.distribution.DistributionManager;
import com.thenetcircle.comsumerdispatcher.job.JobExecutor;

public class DispatcherConfig {

	private static DispatcherConfig _self = null;
	private List<JobExecutor> allJobs = null;
	private Map<String, QueueConf> servers = null;
	private MonitorConf monitorConf = null;

	public static synchronized DispatcherConfig getInstance() {
		if (null == _self) {
			_self = new DispatcherConfig();
		}
		return _self;
	}
	
	public void loadConfig(String configFilePath) throws Exception {
		ConfigLoader cLoader = null; 
		if (null == configFilePath) {
			cLoader = new DistributedConfigLoader(DistributionManager.getInstance().getZk());
		} else {
			cLoader = new FileConfigLoader(configFilePath);
		}
		
		setServers(cLoader.loadServers());// go first
		setAllJobs(cLoader.loadAllJobs());
		setMonitorConf(cLoader.loadJmxConfig());
	}
	
	public List<JobExecutor> getAllJobs() {
		return this.allJobs;
	}
	
	public Map<String, QueueConf> getServers() {
		return this.servers;
	}
	
	public MonitorConf getMonitorConf() {
		return monitorConf;
	}
	
	public void setAllJobs(List<JobExecutor> allJobs) {
		this.allJobs = allJobs;
	}

	public void setServers(Map<String, QueueConf> servers) {
		this.servers = servers;
	}

	public void setMonitorConf(MonitorConf monitorConf) {
		this.monitorConf = monitorConf;
	}

	private DispatcherConfig() {
	}
}
