package edu.rmit.eres.amqpclient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Consumer;

/**
 * RabbitMQClient that is able to listen to a routing key
 * 
 * @author Guillaume Prevost
 * @since 22 Dec. 2016
 * 
 * @see edu.rmit.eres.amqpclient.RabbitMQClient
 */
public class RabbitMQListener extends RabbitMQClient {
	
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);
	
	/**
	 * Constructor of RabbitMQ listener, requiring the info to connect to an RabbitMQ server
	 * @see edu.rmit.eres.amqpclient.RabbitMQClient#RabbitMQClient(String, Integer, String, String, String, String, String)
	 * 
	 * @param rabbitmqBrokerAddress: the address of the RabbitMQ broker host
	 * @param rabbitmqBrokerPort: the port on which the RabbitMQ broker is listening
	 * @param rabbitmqBrokerSslProtocol: the SSL protocol used for secured connection
	 * @param rabbitmqVirtualHost: the name of the virtual host to connect to
	 * @param rabbitmqUserName: the user name to use to authenticate with RabbitMQ
	 * @param rabbitmqUserPassword: the password to use to authenticate with RabbitMQ
	 */
	public RabbitMQListener(String rabbitmqBrokerAddress, Integer rabbitmqBrokerPort,
			String rabbitmqBrokerSslProtocol, String rabbitmqVirtualHost, String rabbitmqUserName,
			String rabbitmqUserPassword) {
		
		super(rabbitmqBrokerAddress, rabbitmqBrokerPort, rabbitmqBrokerSslProtocol, rabbitmqVirtualHost,
				rabbitmqUserName, rabbitmqUserPassword);
	}
	
	/**
	 * Starts the listening to a given binding key on a given RabbitMQ exchange
	 * 
	 * @param exchangeName: the name of the exchange to be listened on
	 * @param bindingKey: the binding key to listen for (see @link{https://www.rabbitmq.com/tutorials/tutorial-five-java.html}).
	 * 
	 * @throws IOException: RabbitMQ methods may throw this exception (queueDeclare, queueBind and basicConsume)
	 */
	public void listen(String exchangeName, String bindingKey) throws IOException {
        String queueName = this.getChannel().queueDeclare().getQueue();
        this.getChannel().queueBind(queueName, exchangeName, bindingKey);
        
        Consumer consumer = new RabbitMQMyTardisConsumer(this.getChannel(), exchangeName);
        this.getChannel().basicConsume(queueName, true, "estored-mytardis-datasource-consumer-channel", true, true, null, consumer);
        
        logger.info("Listening to " + bindingKey + " on " + exchangeName);
	}
}
