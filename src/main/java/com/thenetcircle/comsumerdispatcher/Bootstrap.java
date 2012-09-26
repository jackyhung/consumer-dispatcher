package com.thenetcircle.comsumerdispatcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thenetcircle.comsumerdispatcher.config.DispatcherConfig;
import com.thenetcircle.comsumerdispatcher.distribution.DistributionManager;
import com.thenetcircle.comsumerdispatcher.jmx.ConsumerDispatcherMonitor;
import com.thenetcircle.comsumerdispatcher.job.JobAssign;



public class Bootstrap {
	private static Log _logger = LogFactory.getLog(Bootstrap.class);
	
	public static boolean once = false;
	
	public static void main(String[] args) {
		String filePath = null;
		if (args.length > 0)
			filePath = args[0];
		if (args.length > 1)
			once = "once".equals(args[2]);
		
		try {
			// load configurations either from file or from distribution server
			DispatcherConfig.getInstance().loadConfig(filePath);
			// determine if this one starts up as distribution master or distribution client or standalone
			DistributionManager.getInstance();
			
			JobAssign ja = new JobAssign();
			ja.startupJobs();
			
			ConsumerDispatcherMonitor.enableMonitor();
		} catch (Exception e) {
			_logger.error(e, e);
		}
	}
	

}
