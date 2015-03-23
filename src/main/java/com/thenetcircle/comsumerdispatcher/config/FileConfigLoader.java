package com.thenetcircle.comsumerdispatcher.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.thenetcircle.comsumerdispatcher.job.JobExecutor;

public class FileConfigLoader implements ConfigLoader {
	private static Log _logger = LogFactory.getLog(FileConfigLoader.class);

	protected String filePath;
	protected Document doc;
	
	public FileConfigLoader(String filePath) {
		this.filePath = filePath;
		SAXReader reader = new SAXReader();
		_logger.info("loading conf file from " + filePath);
		try {
			doc = reader.read(filePath);
		} catch (DocumentException e) {
			_logger.error("[File Cofing Loader] error while loading: " + e, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JobExecutor> loadAllJobs() {
		Node reqPre = doc.selectSingleNode("//jobs/conf/request-pre");
		String defReqPre = reqPre.getText();
		reqPre = doc.selectSingleNode("//jobs/conf/request-host");
		String defReqHost = null;
		if (reqPre != null)
			defReqHost = reqPre.getText();
		reqPre = doc.selectSingleNode("//jobs/conf/request-count");
		int defCount = Integer.valueOf(reqPre.getText());
		reqPre = doc.selectSingleNode("//jobs/conf/request-timeout");
		int defTimeout = Integer.valueOf(reqPre.getText());
		reqPre = doc.selectSingleNode("//jobs/conf/prefetch-count");
		int defPrefetchCount = Integer.valueOf(reqPre.getText());
		reqPre = doc.selectSingleNode("//jobs/conf/retry");
		int defRetry = Integer.valueOf(reqPre.getText());
		reqPre = doc.selectSingleNode("//jobs/conf/encoding");
		String defEncoding = null;
		if (reqPre != null)
			defEncoding = reqPre.getText();
		
		List<JobExecutor> allJobs = null;
		List<Element> list = doc.selectNodes("//jobs/job");
		if (null != list && !list.isEmpty()) {
			allJobs = new ArrayList<JobExecutor>();
			for (Iterator<Element> iter = list.iterator(); iter.hasNext();) {
				Element element = iter.next();
				JobExecutor je = new JobExecutor();
				je.setDefaultUrl(defReqPre);
				je.setDefaultUrlHost(defReqHost);
				je.setDefaultCount(defCount);
				je.setDefaultTimeout(defTimeout);
				je.setDefaultPrefetchCount(defPrefetchCount);
				je.setDefaultRetry(defRetry);
				je.setDefaultEncoding(defEncoding);
				QueueConf qc = DispatcherConfig.getInstance().getServers().get(element.attributeValue("server"));
				je.setFetcherQConf(qc);
				element.accept(new CustomerVistor(je));
				allJobs.add(je);
			}
		}
		return allJobs;
	}

	@Override
	public MonitorConf loadJmxConfig() {
		MonitorConf monitorConf = new MonitorConf();
		Element monitor = (Element) doc.selectNodes("//jobs/monitor").get(0);
		monitorConf.setJmxRmiHost(monitor.attributeValue("rHost"));
		monitorConf.setJmxRmiPort(Integer.valueOf(monitor.attributeValue("rPort")));
		monitorConf.setJmxHttpHost(monitor.attributeValue("httpHost"));
		monitorConf.setJmxHttpPort(Integer.valueOf(monitor.attributeValue("httpPort")));
		return monitorConf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, QueueConf> loadServers() {
		List<Element> serverNodes = doc.selectNodes("//jobs/servers/queueserver");
		Map<String, QueueConf> servers = null;
		if (null != serverNodes && !serverNodes.isEmpty()) {
			servers = new HashMap<String, QueueConf>();
			for (Iterator<Element> iter = serverNodes.iterator(); iter.hasNext();) {
				Element qs = iter.next();
				QueueConf qc = new QueueConf(qs.attributeValue("name"), qs.attributeValue("host"), 
						Integer.valueOf(qs.attributeValue("port")), 
						qs.attributeValue("userName"), 
						qs.attributeValue("password"), 
						qs.attributeValue("vhost"));
				servers.put(qs.attributeValue("name"), qc);
			}
		}
		return servers;
	}
}
