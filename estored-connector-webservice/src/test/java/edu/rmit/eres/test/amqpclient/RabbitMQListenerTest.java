package edu.rmit.eres.test.amqpclient;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.rmit.eres.amqpclient.RabbitMQListener;

public class RabbitMQListenerTest {
	
	private String rabbitmqBrokerAddress;
	private int rabbitmqBrokerPort;
	private String rabbitmqBrokerSslProtocol;
	private String rabbitMqVirtualHost;
	private String rabbitmqUserName;
	private String rabbitmqUserPassword;
	
    @Before
    public void setUp() {
    	this.rabbitmqBrokerAddress = "192.168.99.100";
    	this.rabbitmqBrokerPort = 5671;
    	this.rabbitmqBrokerSslProtocol = "tlsv1.2";
    	this.rabbitMqVirtualHost = "/estored";
    	this.rabbitmqUserName = "estored-user";
    	this.rabbitmqUserPassword = "estored-user-pw";
    }
    
	@Test
    public void registerToTopicTest() {
		
		
		/*RabbitMQListener listener = new RabbitMQListener(
				rabbitmqBrokerAddress, 
				rabbitmqBrokerPort, 
				rabbitmqBrokerSslProtocol, 
				rabbitMqVirtualHost, 
				rabbitmqUserName, 
				rabbitmqUserPassword);
    	listener.openConnection();
    	try {
			listener.listen("amq.topic", "*.command");
	    	Assert.assertNotNull(listener);
    	} catch (IOException e) {
			e.printStackTrace();
	    	Assert.fail(e.getMessage());
		}*/
	}
}
