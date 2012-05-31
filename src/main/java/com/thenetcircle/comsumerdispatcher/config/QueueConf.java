package com.thenetcircle.comsumerdispatcher.config;

public class QueueConf {
	String name;
	String host;
	int port;
	String userName;
	String password;
	String vhost;
	
	public QueueConf(String name, String host, int port, String username, String passwd, String vhost){
		this.name = name;
		this.host = host;
		this.port = port;
		this.userName = username;
		this.password = passwd;
		this.vhost = vhost;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getVhost() {
		return vhost;
	}
	public void setVhost(String vhost) {
		this.vhost = vhost;
	}
}