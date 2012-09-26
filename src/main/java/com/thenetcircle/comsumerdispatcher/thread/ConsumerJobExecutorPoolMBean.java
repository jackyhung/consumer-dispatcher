package com.thenetcircle.comsumerdispatcher.thread;

public interface ConsumerJobExecutorPoolMBean {
	
	/**
	 * get the current number of active thread
	 * @return
	 */
	public int getActiveJobExecutorCount();

	/**
	 * get the number of completed tasks
	 * @return
	 */
    public long getCompletedTasks();

    /**
     * new one thread to execute the job
     * @param numToAdd number of executors to be added
     */
    public void addJobExecutor(int numToAdd);
    
    /**
     * remove the specified number of executors
     * @param numToRemove number of executors to be removed
     */
	public void removeJobExecutors(int numToRemove);

    /**
     * stop all current active executors (thread)
     * @return
     */
    public void stopAllExecutors();

    /**
     * purge all jobs in queue
     */
    public void purgeQueue();

	public void logErrorJobToFile(boolean onOrOff);
	
	public String getLoggingLevel();

	/**
	 * @param level
	 */
	public void setLoggingLevel(String level);
}
