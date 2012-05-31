package com.thenetcircle.comsumerdispatcher.config;

public class MonitorConf {
	// jmx config
	private String jmxRmiHost = null;
	private int jmxRmiPort;
	private String jmxHttpHost = null;
	private int jmxHttpPort;
	
	public String getJmxRmiHost() {
		return jmxRmiHost;
	}

	public int getJmxRmiPort() {
		return jmxRmiPort;
	}

	public String getJmxHttpHost() {
		return jmxHttpHost;
	}

	public int getJmxHttpPort() {
		return jmxHttpPort;
	}

	public void setJmxRmiHost(String jmxRmiHost) {
		this.jmxRmiHost = jmxRmiHost;
	}

	public void setJmxRmiPort(int jmxRmiPort) {
		this.jmxRmiPort = jmxRmiPort;
	}

	public void setJmxHttpHost(String jmxHttpHost) {
		this.jmxHttpHost = jmxHttpHost;
	}

	public void setJmxHttpPort(int jmxHttpPort) {
		this.jmxHttpPort = jmxHttpPort;
	}
	
	
}
