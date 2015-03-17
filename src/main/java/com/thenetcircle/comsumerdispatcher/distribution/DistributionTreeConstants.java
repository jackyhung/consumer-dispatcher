package com.thenetcircle.comsumerdispatcher.distribution;


public class DistributionTreeConstants {

	public static String CD_ROOT = "/C_D";
	
	public static String QSERVERS = "/queue_servers";
	
	public static String JOINED_MEMBERS = "/members"; 
	
	/**
	 *\ /queue_servers/snowball
	 *
	 *  where snowball is the name of the queue server
	 */
	public static String QSERVERS_NAME = QSERVERS + "/%s";
	
	/**
	 *\ /queue_servers/snowball/host
	 *
	 *  the value is the host of the queue server
	 */
	public static String QSERVERS_NAME_HOST = QSERVERS_NAME + "/host";

	/**
	 *\ /queue_servers/snowball/port
	 *
	 *  the value is the port of the queue server
	 */
	public static String QSERVERS_NAME_PORT = QSERVERS_NAME + "/port";
	
	/**
	 *\ /queue_servers/snowball/user
	 *
	 *  the value is the user of the queue server
	 */
	public static String QSERVERS_NAME_USER = QSERVERS_NAME + "/user";
	
	/**
	 *\ /queue_servers/snowball/pw
	 *
	 *  the value is the pw of the queue server
	 */
	public static String QSERVERS_NAME_PW = QSERVERS_NAME + "/pw";
	
	/**
	 *\ /queue_servers/snowball/vhost
	 *
	 *  the value is the vhost of the queue server
	 */
	public static String QSERVERS_NAME_VHOST = QSERVERS_NAME + "/vhost";
	
	/**
	 *\ /C_D/dev_poppen_de
	 * it's value is the httphost used in request
	 */
	public static String CD_ROOT_DOMAIN = CD_ROOT + "/%s";
	
	/**
	 *\ /C_D/poppen.de/payment-a  
	 * where payment is the job name 
	 */
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER = CD_ROOT_DOMAIN + "/%s";
	
	/**
	 *\ /C_D/poppen.de/payment/mqserver
	 * where mqserver is the mq server name 
	 */
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_MQSERVER = CD_ROOT_DOMAIN_QUEUEONSERVER + "/mqserver";
	
	/**
	 *\ /C_D/poppen.de/payment-snowball/count
	 * it's value is the number of threads working on this queue 
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_COUNT = CD_ROOT_DOMAIN_QUEUEONSERVER + "/count";

	/**
	 *\ /C_D/poppen.de/payment-snowball/requrl
	 * it's value is the request url used in http request 
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_REQURL = CD_ROOT_DOMAIN_QUEUEONSERVER + "/requrl";

	/**
	 *\ /C_D/poppen.de/payment-snowball/reqhost
	 * it's value is the request host used in http request 
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_REQHOST = CD_ROOT_DOMAIN_QUEUEONSERVER + "/reqhost";
	
	/**
	 *\ /C_D/poppen.de/payment-snowball/reqtimeout
	 * it's value is the time out for http request
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_REQTIMEOUT = CD_ROOT_DOMAIN_QUEUEONSERVER + "/reqtimeout";
	
	/**
	 *\ /C_D/poppen.de/payment-snowball/reqprefetch
	 * it's value is prefetch count for amqp
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_REQPREFETCH = CD_ROOT_DOMAIN_QUEUEONSERVER + "/reqprefetch";
	
	/**
	 *\ /C_D/poppen.de/payment-snowball/reqencoding
	 * it's value is the encoding for http request
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_REQENCODING = CD_ROOT_DOMAIN_QUEUEONSERVER + "/reqencoding";

	/**
	 *\ /C_D/poppen.de/payment-snowball/state
	 * for control the state of this job pool
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_STATE = CD_ROOT_DOMAIN_QUEUEONSERVER + "/state";
	
	/**
	 *\ /C_D/poppen.de/payment-snowball/notrunning
	 * this is the node used to monitor if no worker is running from an application: can use this one to coorperate among cluster, like purging queue 
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING = CD_ROOT_DOMAIN_QUEUEONSERVER + "/notrunning";
	
	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING_SUBNODES = CD_ROOT_DOMAIN_QUEUEONSERVER_NOTRUNNING + "/notrunningfrom_";
	
	/**
	 *\ /C_D/poppen.de/payment-snowball/logerrorfile
	 * this is the node used to control purge process across the distribution servers. it might have several ephemeral nodes
	 */	
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_LOGERRORFILE = CD_ROOT_DOMAIN_QUEUEONSERVER + "/logerrorfile";
	
	/**
	 * \ /C_D/poppen.de/payment-snowball/qbinds
	 */
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS = CD_ROOT_DOMAIN_QUEUEONSERVER + "/qbinds";
	
	/**
	 * \ /C_D/poppen.de/payment-snowball/qbinds/queue
	 * the value is the queue name
	 */
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_QUEUE = CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS + "/queue";
	
	/**
	 * \ /C_D/poppen.de/payment-snowball/qbinds/exchange
	 * the value is the exchange name
	 */
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_EXCHANGE = CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS + "/exchange";
	
	/**
	 * \ /C_D/poppen.de/payment-snowball/qbinds/type
	 * the value is the type name
	 */
	public static String CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS_TYPE = CD_ROOT_DOMAIN_QUEUEONSERVER_QBINDS + "/type";
	
	public static String MONITOR = "/monitor";
	public static String MONITOR_RHOST = MONITOR + "/rhost";
	public static String MONITOR_RPORT = MONITOR + "/rport";
	public static String MONITOR_HTTPHOST = MONITOR + "/httphost";
	public static String MONITOR_HTTPPORT = MONITOR + "/httpport";
}
