package com.thenetcircle.comsumerdispatcher.job.exception;

/**
 * The job executor failed for certain reason, need to restart.
 * 
 * @author jackyhung
 *
 */
public class JobFailedException extends RuntimeException {

	private static final long serialVersionUID = 2263285375315953049L;

	public JobFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
