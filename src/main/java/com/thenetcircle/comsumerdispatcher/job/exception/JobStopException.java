package com.thenetcircle.comsumerdispatcher.job.exception;

/**
 * Stop the job executor. no retry.
 * 
 * @author jackyhung
 *
 */
public class JobStopException extends RuntimeException {

	private static final long serialVersionUID = 7587359595408648856L;
	
	public JobStopException(String message, Throwable cause) {
		super(message, cause);
	}
}
