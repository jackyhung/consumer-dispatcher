package com.thenetcircle.comsumerdispatcher.config;


public class DispatcherJob {

	protected String name;
	protected String url;
	protected String queue;
	protected String exchange;
	protected String type;
	protected int timeout = 0;
	protected int count = 0;
	protected String urlhost;
	protected String encoding;
	
	protected int defaultTimeout = 30000;
	protected int defaultCount = 0;
	protected String defaultUrl;
	protected String defaultUrlHost = "";
	protected String defaultEncoding;

	public String getQueue() {
		if (queue != null && queue.length() > 0) return queue;
		return name;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}
	
	public String getExchange() {
		if (exchange != null && exchange.length() > 0) return exchange;
		return getName() + "_router";
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getType() {
		if(type != null && type.length() > 0) return type;
		return "direct";
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLogicName() {
		return name + "-" + getFetcherQConf().getName();
	}

	public void setName(String consumerName) {
		this.name = consumerName;
	}

	public String getUrl() {
		if(url != null && url.length() > 0) return url;
		return defaultUrl;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getEncoding() {
		if(encoding != null && encoding.length() > 0) return encoding;
		return getDefaultEncoding();
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public int getTimeout() {
		if (timeout <= 0) return defaultTimeout;
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getCount() {
		if (count <= 0) return defaultCount;
		return count;
	}

	public void setCount(int threadCount) {
		this.count = threadCount;
	}

	public int getDefaultTimeout() {
		return defaultTimeout;
	}

	public void setDefaultTimeout(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	public int getDefaultCount() {
		return defaultCount;
	}

	public void setDefaultCount(int defaultCount) {
		this.defaultCount = defaultCount;
	}
	
	public String getDefaultUrl() {
		return defaultUrl;
	}

	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}
	
	public String getDefaultUrlHost() {
		return defaultUrlHost;
	}

	public void setDefaultUrlHost(String defaultUrlHost) {
		this.defaultUrlHost = defaultUrlHost;
	}

	public String getDefaultEncoding() {
		if(defaultEncoding == null || defaultEncoding.length() == 0)
			return "utf-8";
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	protected QueueConf fetcherQConf = null;

	public QueueConf getFetcherQConf() {
		return fetcherQConf;
	}

	public void setFetcherQConf(QueueConf fetcherQConf) {
		this.fetcherQConf = fetcherQConf;
	}

	public String getUrlhost() {
		if(urlhost != null && urlhost.length() > 0) return urlhost;
		return getDefaultUrlHost();
	}

	public void setUrlhost(String urlhost) {
		this.urlhost = urlhost;
	}
}
