package com.thenetcircle.comsumerdispatcher.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thenetcircle.comsumerdispatcher.config.DispatcherConfig;
import com.thenetcircle.comsumerdispatcher.config.MonitorConf;

public class ConsumerDispatcherMonitor {
	private static Log _logger = LogFactory.getLog(ConsumerDispatcherMonitor.class);
	
	private static final String fmtUrl = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";
	
	public static void enableMonitor() {
		try {
			enableJMXRmi();
		} catch (IOException e) {
			e.printStackTrace();
		}
		maybeEnableJMXHttp();
	}
	
	protected static void enableJMXRmi() throws IOException {
		MonitorConf config = DispatcherConfig.getInstance().getMonitorConf();
		
		LocateRegistry.createRegistry(config.getJmxRmiPort());
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		
		String address = String.format(fmtUrl, config.getJmxRmiHost(), config.getJmxRmiPort());
		JMXServiceURL url = new JMXServiceURL(address); 
		JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
		cs.start();
		
		_logger.info("JMX enabled on:" + address);
	}
	
	/**
	 * enabled if mx4j is in classpath
	 */
	protected static boolean maybeEnableJMXHttp() {
        try {
        	MonitorConf config = DispatcherConfig.getInstance().getMonitorConf();
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName processorName = new ObjectName("Server:name=XSLTProcessor");

            Class<?> httpAdaptorClass = Class.forName("mx4j.tools.adaptor.http.HttpAdaptor");
            Object httpAdaptor = httpAdaptorClass.newInstance();
            httpAdaptorClass.getMethod("setHost", String.class).invoke(httpAdaptor, config.getJmxHttpHost());
            httpAdaptorClass.getMethod("setPort", Integer.TYPE).invoke(httpAdaptor, config.getJmxHttpPort());

            ObjectName httpName = new ObjectName("system:name=http");
            mbs.registerMBean(httpAdaptor, httpName);

            Class<?> xsltProcessorClass = Class.forName("mx4j.tools.adaptor.http.XSLTProcessor");
            Object xsltProcessor = xsltProcessorClass.newInstance();
            httpAdaptorClass.getMethod("setProcessor", Class.forName("mx4j.tools.adaptor.http.ProcessorMBean")).
                    invoke(httpAdaptor, xsltProcessor);
            mbs.registerMBean(xsltProcessor, processorName);
            httpAdaptorClass.getMethod("start").invoke(httpAdaptor);

            _logger.info("mx4j successfuly loaded at  " + config.getJmxHttpHost() + ":" + config.getJmxHttpPort());
            return true;
        }
        catch (ClassNotFoundException e)
        {
        	_logger.info("Will not load MX4J, mx4j-tools.jar is not in the classpath");
        }
        catch(Exception e)
        {
        	_logger.warn("Could not start register mbean in JMX", e);
        }
        return false;
	}
	
}
