package edu.rmit.eres.amqpclient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Basic client for a RabbitMQ topic exchange using SSL protocol
 * 
 * @author Guillaume Prevost
 * @since 22 Dec. 2016
 * 
 * @see edu.rmit.eres.amqpclient.RabbitMQClient
 */
public class RabbitMQClient {

	/**
	 * Connection object to the RabbitMQ broker
	 */
	private Connection connection;
	
	/**
	 * RabbitMQ channel to which the client is connected
	 */
	private Channel channel;

	/**
	 * Address of the RabbitMQ broker to establish connection with
	 */
	protected String rabbitmqBrokerAddress;

	/**
	 * The port of the RabbitMQ broker to establish connection with
	 */
	protected Integer rabbitmqBrokerPort;

	/**
	 * Name of the SSL protocol to use for connecting (should be matching the one used by the RabbitMQ broker)
	 */
	protected String rabbitmqBrokerSslProtocol;
	
	/**
	 * Keyword used by RabbitMQ to reference the 'topic' type of exchange
	 */
	public final static String EXCHANGE_TYPE_TOPIC = "topic";
	
	/**
	 * The name of the virtual host to connect to
	 */
	protected String rabbitmqVirtualHost;
	
	/**
	 * The user name to use to authenticate with RabbitMQ
	 */
	protected String rabbitmqUserName;

	/**
	 * The password to use to authenticate with RabbitMQ
	 */
	protected String rabbitmqUserPassword;

	/**
	 * Constructor of RabbitMQ listener, requiring the info to connect to an RabbitMQ server
	 * 
	 * @param rabbitmqBrokerAddress: the address of the RabbitMQ broker host
	 * @param rabbitmqBrokerPort: the port on which the RabbitMQ broker is listening
	 * @param rabbitmqBrokerSslProtocol: the SSL protocol used for secured connection
	 * @param rabbitmqExchangeName: the name of the exchange to send messages to
	 * @param rabbitmqVirtualHost: the name of the virtual host to connect to
	 * @param rabbitmqUserName: the user name to use to authenticate with RabbitMQ
	 * @param rabbitmqUserPassword: the password to use to authenticate with RabbitMQ
	 */
	public RabbitMQClient(String rabbitmqBrokerAddress, Integer rabbitmqBrokerPort,
			String rabbitmqBrokerSslProtocol, String rabbitmqVirtualHost, String rabbitmqUserName,
			String rabbitmqUserPassword) {
		this.rabbitmqBrokerAddress = rabbitmqBrokerAddress;
		this.rabbitmqBrokerPort = rabbitmqBrokerPort;
		this.rabbitmqBrokerSslProtocol = rabbitmqBrokerSslProtocol;
		this.rabbitmqVirtualHost = rabbitmqVirtualHost;
		this.rabbitmqUserName = rabbitmqUserName;
		this.rabbitmqUserPassword = rabbitmqUserPassword;
	}
	
	/**
	 * Opens a connection to a RabbitMQ broker and creates a channel using the properties of this client
	 * 
	 * @return true if the connection has been opened successfully, false otherwise
	 */
	public boolean openConnection() {
		// Creates the RabbitMQ connection factory using properties
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(this.rabbitmqBrokerAddress);
		factory.setPort(rabbitmqBrokerPort);
		factory.setVirtualHost(rabbitmqVirtualHost);
		factory.setUsername(rabbitmqUserName);
		factory.setPassword(rabbitmqUserPassword);

		// Try setting the SSL protocol
		try {
			factory.useSslProtocol(rabbitmqBrokerSslProtocol);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}

		// Try to create the connection and the channel, then declare a topic exchange
		try {
			this.connection = factory.newConnection();
			this.channel = this.connection.createChannel();
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * Closes the RabbitMQ channel and the connection if they exists 
	 * 
	 * @return true if the channel and connection could be closed, false otherwise
	 */
	public boolean closeConnection() {
		try {
			// Closes channel if exists
			if (this.channel != null) {
				this.channel.close();
				this.channel = null;
			}
			// Closes connection if exists
			if (this.connection != null) {
				this.connection.close();
				this.connection = null;				
			}
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Getter for the connection object to the RabbitMQ broker
	 * @return the connection object to the RabbitMQ broker
	 */
	protected Connection getConnection() {
		return this.connection;
	}
	
	/**
	 * Getter for the RabbitMQ channel to which the client is connected
	 * @return the RabbitMQ channel to which the client is connected
	 */
	protected Channel getChannel() {
		return this.channel;
	}
}
