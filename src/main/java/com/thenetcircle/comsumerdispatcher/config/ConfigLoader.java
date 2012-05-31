package com.thenetcircle.comsumerdispatcher.config;

import java.util.List;
import java.util.Map;

import com.thenetcircle.comsumerdispatcher.job.JobExecutor;

public interface ConfigLoader {

	public Map<String, QueueConf> loadServers();
	
	public List<JobExecutor> loadAllJobs();
	
	public MonitorConf loadJmxConfig();
}
