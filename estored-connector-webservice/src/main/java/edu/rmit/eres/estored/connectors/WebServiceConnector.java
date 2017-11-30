package edu.rmit.eres.estored.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rmit.eres.amqpclient.RabbitMQListener;

/**
 * This programs creates a listener for RabbitMQ binding key on a given 
 * instance and channel. When receiving a message, the listener attempts 
 * to extract information from the message headers to use as connection 
 * information to a Web Service end point. It will retrieve the required data 
 * file and send back it's content as RabbitMQ message.
 * 
 * @author Guillaume Prevost
 * @since 17 Jul. 2017
 */
public class WebServiceConnector {
	
	private static final Logger logger = 
			LoggerFactory.getLogger(WebServiceConnector.class);

	private static final String DEFAULT_RABBITMQ_BROKER_ADDRESS = "127.0.0.1";
	private static final int DEFAULT_RABBITMQ_BROKER_PORT = 5671;
	private static final String DEFAULT_RABBITMQ_USER_NAME = "estored-user";
	private static final String DEFAULT_RABBITMQ_PASSWORD = "estored-user-pw";
	private static final String DEFAULT_RABBITMQ_VHOST = "/estored";
	private static final String DEFAULT_RABBITMQ_SSL_PROTOCOL = "tlsv1.2";
	
	/**
	 * Start of the eStoRED MyTardis Datasource program
	 */
	public static void main(String[] argv) {
			
			logger.info("eStoRED MyTardis connector starting");
		
			final String rabbitmqHost = (System.getenv("RABBITMQ_BROKER_ADDRESS") != null) ? System.getenv("RABBITMQ_BROKER_ADDRESS") : DEFAULT_RABBITMQ_BROKER_ADDRESS;
			final int rabbitmqPort = (System.getenv("RABBITMQ_BROKER_PORT") != null) ? new Integer(System.getenv("RABBITMQ_BROKER_PORT")) : DEFAULT_RABBITMQ_BROKER_PORT;
			final String rabbitmqUsername = (System.getenv("RABBITMQ_USER_NAME") != null) ? System.getenv("RABBITMQ_USER_NAME") : DEFAULT_RABBITMQ_USER_NAME;
			final String rabbitmqPassword = (System.getenv("RABBITMQ_USER_PASSWORD") != null) ? System.getenv("RABBITMQ_USER_PASSWORD") : DEFAULT_RABBITMQ_PASSWORD;
			final String rabbitmqVhost = (System.getenv("RABBITMQ_VHOST") != null) ? System.getenv("RABBITMQ_VHOST") : DEFAULT_RABBITMQ_VHOST;
			final String rabbitmqSslprotocol = DEFAULT_RABBITMQ_SSL_PROTOCOL;
			
			try {
				logger.info("Trying to connect: " + rabbitmqUsername + ":" + rabbitmqPassword + "@" + rabbitmqHost + ":" + rabbitmqPort + rabbitmqVhost + " (" + rabbitmqSslprotocol + ")");
				RabbitMQListener listener = new RabbitMQListener(
						rabbitmqHost,
						rabbitmqPort, 
						rabbitmqSslprotocol, 
						rabbitmqVhost, 
						rabbitmqUsername, 
						rabbitmqPassword);
		    	listener.openConnection();
				listener.listen("amq.topic", "estored.ds.webservice.#");
	    	} catch (Exception e) {
				e.printStackTrace();
			}
	  }
}