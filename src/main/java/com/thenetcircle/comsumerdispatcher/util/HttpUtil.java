package com.thenetcircle.comsumerdispatcher.util;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	private static Log _logger = LogFactory.getLog(HttpUtil.class);
	
	public static String sendHttpPost(String url, String host, Map<String, String> parameters, int timeout) throws Exception {
		DefaultHttpClient httpclient = getDefaultHttpClient(host);
		UrlEncodedFormEntity formEntity = null;
		try {
			formEntity = new UrlEncodedFormEntity(getParamsList(parameters), HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw e;
		}
		HttpPost hp = new HttpPost(url);
		hp.setEntity(formEntity);
		if(host !=null && host.length() > 0)
			hp.setHeader("Host", host);

		String responseStr = null;
		try {
			HttpResponse response = httpclient.execute(hp);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				responseStr = new String(EntityUtils.toByteArray(entity));
			} else {
				responseStr = "get the response status:" + response.getStatusLine().getStatusCode();
			}
		} catch (Exception e) {
			_logger.error(e, e);
			return null;
		} finally {
			abortConnection(hp, httpclient);
		}
		return responseStr;
	}

	private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient) {
		if (hrb != null) {
			hrb.abort();
		}
		if (httpclient != null) {
			httpclient.getConnectionManager().shutdown();
		}
	}

	private static DefaultHttpClient getDefaultHttpClient(String host) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpclient.getParams().setParameter("Host", host);

		return httpclient;
	}

	private static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
		if (paramsMap == null || paramsMap.size() == 0) {
			return null;
		}
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> map : paramsMap.entrySet()) {
			params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
		}
		return params;
	}
	
	public static String convertUrlToHostNameAsNodeName(String url) throws MalformedURLException {
		String host = new URL(url).getHost().replace('.', '-');
		return host;
	}
	
	public static String getLocalHostName() {
		String hostname = null;
		try {
		    InetAddress addr = InetAddress.getLocalHost();
		    //byte[] ipAddr = addr.getAddress();
		    hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			_logger.error(e, e);
		}
		return hostname;
	}
}
