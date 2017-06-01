package com.thenetcircle.comsumerdispatcher.thread;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.thenetcircle.comsumerdispatcher.distribution.watcher.CountChangedWatcher;
import com.thenetcircle.comsumerdispatcher.distribution.watcher.IJobPoolLevelWatcher;
import com.thenetcircle.comsumerdispatcher.distribution.watcher.NewUrlWatcher;
import com.thenetcircle.comsumerdispatcher.distribution.watcher.QueuePurgeWatcher;
import com.thenetcircle.comsumerdispatcher.job.JobExecutor;

public class ConsumerJobExecutorPool implements ConsumerJobExecutorPoolMBean {
	private static Log _logger = LogFactory.getLog(ConsumerJobExecutorPool.class);
	
    private final ObjectName mbeanName;
    private final NamedThreadFactory threadFactory;
    private final JobExecutor job;
    private final HashSet<Worker> workers = new HashSet<Worker>();
    
    protected final AtomicInteger completeTaskCount = new AtomicInteger(0);
    private final AtomicInteger activeExecutorCount = new AtomicInteger(0);
    private final AtomicBoolean logErrorJobToFile = new AtomicBoolean(false);
    
    private IJobPoolLevelWatcher purgeWatcher, countChangedWatcher, urlChangedWatcher;

    public ConsumerJobExecutorPool(JobExecutor job) {
       	String jmxType = job.getQueue() + "_" + job.getFetcherQConf().getHost() + "_" + job.getFetcherQConf().getVhost();
       	this.job = job;  
    	threadFactory = new NamedThreadFactory(jmxType);
    	
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        
        try
        {
            URL url = new URL(job.getUrl());
            String domain = url.getHost();
            Hashtable<String, String> kv = new Hashtable<String, String>();
            kv.put("queue", job.getQueue());
            kv.put("q_host", job.getFetcherQConf().getHost());
            kv.put("q_vhost", job.getFetcherQConf().getVhost());
            mbeanName =  new ObjectName(domain, kv);
            mbs.registerMBean(this, new ObjectName(domain, kv));
            
            //watchers
            purgeWatcher = new QueuePurgeWatcher();
            purgeWatcher.register(this);
            countChangedWatcher = new CountChangedWatcher();
            countChangedWatcher.register(this);
            urlChangedWatcher = new NewUrlWatcher();
            urlChangedWatcher.register(this);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public JobExecutor getJobDefinition() {
    	return this.job;
    }
    
    /**
     * initial job executors (threads) 
     * according to the COUNT number defined in configuration
     * 
     * @return the number of executors initialized
     */
    public int startJobExecutors() {
		addJobExecutor(job.getCount());
    	
    	return job.getCount();
    }
    
    public void setJobExecutorNum(int num) {
    	_logger.info("[THREAD NUM Set] going to set " + num + " of executors ...");
    	int currentNum = activeExecutorCount.intValue();
    	if(currentNum > num) {
    		removeJobExecutorsByNum(currentNum - num);
    	} else if (currentNum < num) {
    		addJobExecutorByNum(num - currentNum);
    	}
    }
    
    public void refreshUrl(String newUrl) {
    	String oldUrl = job.getUrl();
    	job.setUrl(newUrl);
    	int currentNum = activeExecutorCount.intValue();
    	setJobExecutorNum(0);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
    	setJobExecutorNum(currentNum);
    	_logger.info("[THREAD REFRESH URL] queue: " + job.getQueue() + "changed url from: " + oldUrl + " to new one: " + newUrl);
    }
    
    //-----------------------  JMX ---------------------------
    
	@Override
	public long getCompletedTasks() {
		return this.completeTaskCount.longValue();
	}

	@Override
	public int getActiveJobExecutorCount() {
		return activeExecutorCount.intValue();
	}

	@Override
	public void addJobExecutor(int numToAdd) {
		_logger.info("[THREAD ADD] going to add " + numToAdd + " new executors ...");
		
        CountChangedWatcher ccw = (CountChangedWatcher) countChangedWatcher;
        ccw.setNumToSet(numToAdd + activeExecutorCount.intValue());
        ccw.execute();
	}
	
	@Override
	public void removeJobExecutors(int numToRemove) {
		_logger.info("[THREAD INTERRUPT] going to stop " + numToRemove + " executors ...");
        CountChangedWatcher ccw = (CountChangedWatcher) countChangedWatcher;
        ccw.setNumToSet(activeExecutorCount.intValue() - numToRemove);
        ccw.execute();
		_logger.info("[THREAD INTERRUPT] stopped");
	}
	
	@Override
    public void stopAllExecutors() {
		if(workers.size() <= 0) {
			_logger.info("[THREAD INTERRUPT] no executor to stop :" + job.getQueue());
			return;
		}
		
		_logger.info("[THREAD INTERRUPT] going to stop all executors ...");
		
        CountChangedWatcher ccw = (CountChangedWatcher) countChangedWatcher;
        ccw.setNumToSet(0);
        ccw.execute();
    }

	@Override
	public void purgeQueue() {
		_logger.info("[Queue Purge] going to purge queue ...: " + job.getQueue());
		((QueuePurgeWatcher) purgeWatcher).execute();
	}
	
	@Override
	public void logErrorJobToFile(boolean onOrOff) {
		this.logErrorJobToFile.set(onOrOff);
		_logger.info("[Log Error Job] is " + onOrOff + " for queue: " + job.getQueue());
	}
	
	@Override
	public String getLoggingLevel() {
		return Logger.getRootLogger().getLevel().toString() ;
	}
	
	@Override
	public void setLoggingLevel(String level) {
		_logger.warn("Setting logging level to: " + level);
        Level newLevel = Level.toLevel(level, Level.INFO);
        Logger.getRootLogger().setLevel(newLevel);
	}
	
	@Override
	public void setJobUrl(String url) {
		_logger.warn("[Set URL] going to set url: " + url + " for queue: " + job.getQueue());
		NewUrlWatcher ucw = (NewUrlWatcher) urlChangedWatcher;
		ucw.setNewUrl(url);
		ucw.execute();
	}
	
	@Override
	public String getJobUrl() {
		return job.getUrl();
	}
	
	
	//-----------------------  JMX END ------------------------
	
	//------------------------ Worker ------------------------
    private final class Worker implements Runnable {

        private final ReentrantLock runLock = new ReentrantLock(true);

        private JobExecutor task;

        Thread thread;

        Worker(Runnable task) {
            if(task instanceof JobExecutor) {
            	this.task = (JobExecutor) task;
            	this.task.setRunLock(runLock);
            	this.task.setCompletedJobs(completeTaskCount);
            	this.task.setLogErrorJobToFile(logErrorJobToFile);
            }
        }

        /**
         * Interrupts thread if the jobexecutor is not running
         */
        void interruptGracefully() {
            final ReentrantLock runLock = this.task.getRunLock();
            _logger.info("[THREAD INTERRUPT] try to get lock :" + thread.getName());
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            runLock.lock();
            _logger.info("[THREAD INTERRUPT] got lock :" + thread.getName());
            try {
            	if (thread != Thread.currentThread())
            		thread.interrupt();// get the jobexecutor out of loop
            } finally {
                runLock.unlock();
            }
            
            _logger.info("[THREAD INTERRUPT] gracefully interrupt :" + thread.getName());
        }

		@Override
		public void run() {
            if (Thread.interrupted())
                thread.interrupt();

            boolean run = true;
            while (run) {
	            try {
	                task.run();
	            } catch (Exception ex) {
	            	_logger.error(ex, ex);
	            	try {
						Thread.sleep(5000);// before restart, sleep
						_logger.info("[THREAD INTERRUPT] thread quitting:" + thread.getName() + ", will restart");
					} catch (InterruptedException e) {
					}
	                //afterExecute(this, ex);
	                // never quit loop
	                //will run the task again by creating a new connection to queue server
	            }
			}
        }

    }
    //-------------- end worker ------------
    
    protected void unregisterMBean() {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbeanName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
	protected void removeJobExecutorsByNum(int numToRemove) {
		HashSet<Worker> workerSet = getWorkersToBeRemoved(numToRemove);
        for (Worker w : workerSet) {
            removeOneWorker(w);
        }
	}
	
	protected void addJobExecutorByNum(int numToAdd) {
		for(; numToAdd > 0; numToAdd--) {
			JobExecutor clonedJob;
			try {
				clonedJob = (JobExecutor) job.clone();
		        Worker w = new Worker(clonedJob);
		        Thread t = threadFactory.newThread(w);
		        if (t != null) {
		            w.thread = t;
		            workers.add(w);
		            activeExecutorCount.incrementAndGet();
		            t.start();
		        }
			} catch (CloneNotSupportedException e) {
				_logger.error(e, e);
			}
		}
	}
    
	protected void removeOneWorker(Worker w) {
		w.interruptGracefully();
 
		String tname = w.thread.getName();
		while(!w.thread.getState().equals(State.TERMINATED)){}
		
		afterExecute(w, null);
		_logger.info("[THREAD INTERRUPT] Thread is stopped...: " + tname);
	}
	
    protected void afterExecute(Runnable r, Throwable t) {
    	Worker w = (Worker) r;
		workers.remove(w);
		activeExecutorCount.decrementAndGet();
		
		if(t == null) {
			_logger.info("Thread is quitting...: " + w.thread.getName());
		} else {
			_logger.error("Thread is quitting...: " + w.thread.getName(), t);
		}
    }
    
    protected HashSet<Worker> getWorkersToBeRemoved(int numToRemove) {
    	HashSet<Worker> result = new HashSet<Worker>();
    	for (Worker w : workers) {
        	if(numToRemove > 0) {
        		result.add(w);
	            numToRemove--;
	        } else {
	        	break;
	        }
    	}
    	
    	return result;
    }
}
