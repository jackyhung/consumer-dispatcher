package com.thenetcircle.comsumerdispatcher.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thenetcircle.comsumerdispatcher.job.JobExecutor;

public class FileUtil {
	private static Log _logger = LogFactory.getLog(FileUtil.class);

	public static boolean logJobRawDataToFile(String fileName, String data) {
		String filePath = "/tmp/" + fileName;
		try {
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();
		} catch (IOException e) {
			_logger.error(e, e);
			return false;
		}
		return true;
	}
	
	public static String getErrorJobFileName(JobExecutor je) {
		try {
			String host = new URL(je.getUrl()).getHost().replace('.', '-');
			String jobName = je.getLogicName();
			return "failed_consumer_jobs__" +host + "__" + jobName;
		} catch (MalformedURLException e) {
			_logger.error(e, e);
			return "failed_consumer_jobs";
		}
		
	}
}
