/**
 * 
 */
package com.thenetcircle.comsumerdispatcher.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author jacky
 *
 */
public class ServiceInstance implements ServiceInstanceMBean {
	private static Log _logger = LogFactory.getLog(ServiceInstance.class);

	/* (non-Javadoc)
	 * @see com.thenetcircle.comsumerdispatcher.jmx.ServiceInstanceMBean#setLoggingLevel(java.lang.String)
	 */
	@Override
	public void setLoggingLevel(String level) {
		_logger.info("Setting logging level to: " + level);
        Level newLevel = Level.toLevel(level, Level.INFO);
        Logger.getRootLogger().setLevel(newLevel);
	}

	/* (non-Javadoc)
	 * @see com.thenetcircle.comsumerdispatcher.jmx.ServiceInstanceMBean#getLoggingLevel()
	 */
	@Override
	public String getLoggingLevel() {
		return Logger.getRootLogger().getLevel().toString() ;
	}

}
