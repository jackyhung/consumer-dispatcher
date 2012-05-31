package com.thenetcircle.comsumerdispatcher.job;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thenetcircle.comsumerdispatcher.config.DispatcherConfig;
import com.thenetcircle.comsumerdispatcher.thread.ConsumerJobExecutorPool;


public class JobAssign {
	private static Log _logger = LogFactory.getLog(JobAssign.class);

	public void startupJobs() {
		//get all jobs from config
		List<JobExecutor> allJobs = DispatcherConfig.getInstance().getAllJobs();
		
		if (null != allJobs && !allJobs.isEmpty()) {
			int jobcount = 0;
			
			// init a executor pool for each job definition
			for (JobExecutor job : allJobs) {
				ConsumerJobExecutorPool ep = new ConsumerJobExecutorPool(job);
				jobcount += ep.startJobExecutors();
			}

			_logger.info("===== started up " + jobcount + " jobs ====");
		}
	}

	public void shutdownAllTask() {
	}
}
