package com.thenetcircle.comsumerdispatcher.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.thenetcircle.comsumerdispatcher.Bootstrap;
import com.thenetcircle.comsumerdispatcher.config.DispatcherJob;
import com.thenetcircle.comsumerdispatcher.job.exception.JobFailedException;
import com.thenetcircle.comsumerdispatcher.job.exception.JobStopException;
import com.thenetcircle.comsumerdispatcher.util.FileUtil;
import com.thenetcircle.comsumerdispatcher.util.HttpUtil;

public class JobExecutor extends DispatcherJob implements Runnable, Cloneable {
	private static Log _logger = LogFactory.getLog(JobExecutor.class);
	
	private ReentrantLock runLock;
	volatile AtomicInteger completedJobs;
	private AtomicBoolean logErrorJobToFile; 
	private long DELIVERY_WAIT_TIMEOUT = 3000;

	public void run() {
		_logger.info("started: " + this.getName() + " with params: " + super.toString());
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(this.getFetcherQConf().getUserName());
		factory.setPassword(this.getFetcherQConf().getPassword());
		factory.setVirtualHost(this.getFetcherQConf().getVhost());
		factory.setHost(this.getFetcherQConf().getHost());
		factory.setPort(this.getFetcherQConf().getPort());
		factory.setAutomaticRecoveryEnabled(true);
		factory.setNetworkRecoveryInterval(5000);
		//factory.setRequestedHeartbeat();
		
		Connection conn = null;
		Channel channel = null;
		String queueName = getQueue();//"image_admin";
		try {
			conn = factory.newConnection();
			// in one thread 
			channel = conn.createChannel();
			String exchangeName = getExchange();//"image_admin_exchange";
			String type = this.getType();
			boolean exclusive = false;
			boolean autoDelete = false;
			boolean durable = true;
			String routingKey = "";
			
			channel.exchangeDeclare(exchangeName, type, durable);
			channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
			channel.queueBind(queueName, exchangeName, routingKey);
			
			boolean autoAck = false;
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicQos(getPrefetchCount());
			channel.basicConsume(queueName, autoAck, consumer);
			//channel.basicQos(getPrefetchCount());
			
			
			boolean run = true;
			while (run) {
            	final ReentrantLock runLock = this.runLock;
	            runLock.lock();
	            try {
				    QueueingConsumer.Delivery delivery;
			        delivery = consumer.nextDelivery(DELIVERY_WAIT_TIMEOUT);
			        if(null == delivery)
			        	continue;
			    
				    byte[] bobyByte = delivery.getBody();
				    String bodyStr = new String(bobyByte, this.getEncoding());//"US-ASCII", "utf-8"
				    
					if(dispatchJob(this.getName(), bodyStr)) {
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
						
						completedJobs.incrementAndGet();
						if(_logger.isDebugEnabled()) {
							_logger.debug("ack meg:" + delivery.getEnvelope().getDeliveryTag());
						}
					} else {
						channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
					}
				    
				    if (Bootstrap.once) run = false;
			    } catch (IOException ioe) {   // the exception of io, continue the loop process , retry!
			    	_logger.error("[THREAD INTERRUPT] got error, but will continue:" + queueName, ioe);
			    	continue;
			    } catch (ShutdownSignalException se) { // got the queue connection error, quit the loop, let the Pool to start a new connection.
			    	throw new JobFailedException("[THREAD INTERRUPT] got queue error:" + queueName, se);
			    } catch (ConsumerCancelledException ce) { // got the queue connection error, quit the loop, let the Pool to start a new connection.
			    	throw new JobFailedException("[THREAD INTERRUPT] got queue consumer error:" + queueName, ce);
            	} catch (InterruptedException ie) { // got the signal to quit. and also the Pool should STOP this job.
			    	throw new JobStopException("[THREAD INTERRUPT] got quit signal:" + queueName, ie);
	            } finally {
	                runLock.unlock();
	            }
			} // end loop
		} catch (IOException ioe) {
			throw new JobStopException("[THREAD INTERRUPT] got certain error:" + queueName, ioe);
		} catch (TimeoutException te) {
			throw new JobStopException("[THREAD INTERRUPT] got certain error:" + queueName, te);
		} finally {
			try {
				if (channel != null && channel.isOpen()) channel.close();
			} catch (Exception e) {
				_logger.error(e, e);
			}
			try {
				if (conn!= null && conn.isOpen()) conn.close();
			} catch (IOException e) {
				_logger.error(e, e);
			}
		}
		
	}

	private boolean dispatchJob(String qname, String body) {
		String vhost = this.getFetcherQConf().getHost() + "@" + this.getFetcherQConf().getVhost() ;
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("queueName", qname);
			map.put("bodyData", body);
			String result = HttpUtil.sendHttpPost(this.getUrl(), this.getUrlhost(), map, this.getTimeout());
			if(null != result) result = result.trim();
			if ("ok".equalsIgnoreCase(result)) {
				if(_logger.isDebugEnabled()) {
					_logger.debug("the result of job for q " + qname + " on server " + vhost + ":" + result);
				}
				return true;
			} else {
				if(_logger.isErrorEnabled())
					_logger.error("the result of job part is not right for q " + qname  + " on server " + vhost);
				if(_logger.isDebugEnabled())
					_logger.debug("the result of job part is not right for q " + qname  + " on server " + vhost + ": " + ", body: " + body + ", response: " + result);
				
				if(logErrorJobToFile.get()) { // get logged to file, then acknowledge this job to queue
					FileUtil.logJobRawDataToFile(FileUtil.getErrorJobFileName(this), body);
					return true;
				}
				
				if(getRetry() == 0) {  // disable retry
					_logger.info("get error, but wont retry for q " + qname  + " on server " + vhost + ": " + ", body: " + body + ", response: " + result);
					return true;
				}
				
				return false;
			}
		} catch (Exception e) {
			if(_logger.isErrorEnabled())
				_logger.error("the status of job part is not right for q " + qname  + " on server " + vhost + ": " + e.getMessage());
			
			if(logErrorJobToFile.get()) { // get logged to file, then acknowledge this job to queue
				FileUtil.logJobRawDataToFile(FileUtil.getErrorJobFileName(this), body);
				return true;
			}
			
			return false;
		}
	}

	public void setRunLock(ReentrantLock runLock) {
		this.runLock = runLock;
	}
	
	public ReentrantLock getRunLock() {
		return this.runLock;
	}
	
	public AtomicInteger getCompletedJobs() {
		return completedJobs;
	}

	public void setCompletedJobs(AtomicInteger completedJobs) {
		this.completedJobs = completedJobs;
	}
	
	public void setLogErrorJobToFile(AtomicBoolean logErrorJobToFile) {
		this.logErrorJobToFile = logErrorJobToFile;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
