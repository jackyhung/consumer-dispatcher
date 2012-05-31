package com.thenetcircle.comsumerdispatcher.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.thenetcircle.comsumerdispatcher.job.JobExecutor;

public class QueueUtil {
	private static Log _logger = LogFactory.getLog(QueueUtil.class);
	
	public static boolean purgeQueue(JobExecutor je) {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(je.getFetcherQConf().getUserName());
		factory.setPassword(je.getFetcherQConf().getPassword());
		factory.setVirtualHost(je.getFetcherQConf().getVhost());
		factory.setHost(je.getFetcherQConf().getHost());
		factory.setPort(je.getFetcherQConf().getPort());
		
		Connection conn = null;
		Channel channel = null;
		
		try {
			conn = factory.newConnection();
			channel = conn.createChannel();
			PurgeOk ok = channel.queuePurge(je.getQueue());
			_logger.info("purged queue: " +je.getQueue() + " result: " + ok.toString() + " on " + je.getFetcherQConf().getHost() + "," + je.getFetcherQConf().getVhost());
			return true;
		} catch (IOException e) {
			_logger.error(e, e);
		} finally {
			try {
				if (channel != null) channel.close();
			} catch (IOException e) {
				_logger.error(e, e);
			}
			try {
				if (conn!= null) conn.close();
			} catch (IOException e) {
				_logger.error(e, e);
			}
		}
		return false;
	}
}
