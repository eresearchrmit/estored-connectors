package edu.rmit.eres.estored.connectors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;

import edu.rmit.eres.amqpclient.RabbitMQClient;

/**
 * Consumer that handles RabbitMQ messages by requesting data from a web service 
 * Based on RabbitMQ default consumer (see @{link com.rabbitmq.client.DefaultConsumer})
 * 
 * @author Guillaume Prevost
 * @since 17 Jul. 2017
 * 
 * @see @{link com.rabbitmq.client.Consumer}
 */
public class WebServiceRMQConsumer extends DefaultConsumer {
	
	private static final Logger logger = LoggerFactory.getLogger(WebServiceRMQConsumer.class);
	
	private static final Boolean DEFAULT_SSL_AUTOTRUST_SELFSIGNED = true;
	
	private String rabbitmqExchangeName;
	
	/**
	 * Default contructor of WebServiceRMQConsumer, calling the parent class constructor @{link com.rabbitmq.client.DefaultConsumer}
	 * @param channel: 
	 */
	public WebServiceRMQConsumer(Channel channel, String rabbitMqExchangeName) throws IOException {
		super(channel);
		this.rabbitmqExchangeName = rabbitMqExchangeName;
	}
	
	/**
	 * Handles a request for retrieving a single MyTardis data file.
	 */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    	
    	// Extracts content of the received message body
    	String message = new String(body, "UTF-8");
    	logger.info("[handleDelivery] Received:'" + message + "'");
    	
    	// Extracts data from received message headers
    	Map<String, Object> receivedHeader = properties.getHeaders();
    	//String protocol = getStringFromHeader(receivedHeader.get("protocol"));
    	//String host = getStringFromHeader(receivedHeader.get("host"));
    	//String user = getStringFromHeader(receivedHeader.get("user"));
    	//String password = getStringFromHeader(receivedHeader.get("password"));
    	String url = getStringFromHeader(receivedHeader.get("parameter"));
    	
    	byte[] data = null;
    	try {
    		final Boolean trustSelfSigned = (System.getenv("SSL_AUTOTRUST_SELFSIGNED") != null) ? Boolean.valueOf(System.getenv("SSL_AUTOTRUST_SELFSIGNED")) : DEFAULT_SSL_AUTOTRUST_SELFSIGNED;
    		
    		// Trusts self-signed certificate
    		String host = getHostName(url);
    		if (url.startsWith("https") && trustSelfSigned == true) {
        		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals(host));
        		InstallCert.installCert(host);
        	}
        	
        	WebServiceClient client = new WebServiceClient(url);
        	
    		// Retrieves file content with MyTardis API
        	logger.debug("Downloading content from " + host);
    		data = client.readBytes();
    		logger.debug("Content downloaded.");
            
            byte[] encodedData = Base64.encodeBase64(data);
            
            // Retrieving headers (size and content type)
    		Map<String, Object> headers = new HashMap<String, Object>();
    		headers.put("content-length", client.getContentLength());
    		String contentType = client.getContentType();
    		headers.put("content-type", contentType);
    		
   		 	// Trims the routing key to keep only the routing key to return
            String returnRoutingKey = envelope.getRoutingKey()
            		.replaceAll("estored.", "")
            		.replaceAll("ds.", "")
            		.replaceAll(".recompute", "");
        	
            // Sends response back to new routing key, with the meta-data as message headers and the file content as message body
            sendMessage(this.rabbitmqExchangeName, returnRoutingKey, contentType, encodedData, headers);
    	
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    	}            	
    }
	
    public String getHostName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String hostname = uri.getHost();
        // to provide faultproof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
        }
        return hostname;
    }
    
	/**
	 * Extract a String from RabbitMQ header value Object
	 * 
	 * @param headerValue: the Object to convert into a String value
	 * @return the String value of the RabbitMQ header
	 * 
	 * @throws UnsupportedEncodingException
	 */
	protected String getStringFromHeader(Object headerValue) throws UnsupportedEncodingException {
		LongString ls = (LongString) headerValue;
		byte[] lsAsByteArray = ls.getBytes();
		return new String(lsAsByteArray, "UTF-8");
	}
	
	/**
	 * Sends a message to the given routing key using the given content type, message body, and message headers
	 * 
	 * @param exchangeName: the RabbitMQ exhange to which send the message to
	 * @param routingKey: the routing key to send to message to
	 * @param contentType: content-type of the message to send
	 * @param message: body of the message to send
	 * @param headers: headers of the message to send
	 */
	public void sendMessage(String exchangeName, String routingKey, String contentType, byte[] message, Map<String, Object> headers) {
    	
        try {
        	this.getChannel().exchangeDeclare(exchangeName, RabbitMQClient.EXCHANGE_TYPE_TOPIC, true);
        	
        	this.getChannel().basicPublish(exchangeName, routingKey, new AMQP.BasicProperties.Builder()
	                .contentType(contentType)
	                .headers(headers)
	                .build(), 
	                message);
	        
	        logger.info("[handleDelivery] Sent message to routing key '" + routingKey + "'");
	        
	    } catch (FileNotFoundException e) {
	  		e.printStackTrace();
	  	} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
