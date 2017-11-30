package edu.rmit.eres.amqpclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.mytardis.api.client.TardisClient;
import org.mytardis.api.model.DatasetFile;
import org.mytardis.api.model.Parametername;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;

import edu.rmit.eres.estored.datasource.mytardis.InstallCert;

/**
 * Consumer that handles RabbitMQ messages requesting to get a MyTardis data file
 * Based on RabbitMQ default consumer (see @{link com.rabbitmq.client.DefaultConsumer})
 * 
 * @author Guillaume Prevost
 * @since 21 Dec. 2016
 * 
 * @see @{link com.rabbitmq.client.Consumer}
 */
public class RabbitMQMyTardisConsumer extends DefaultConsumer {
	
	private static final Logger logger = LoggerFactory.getLogger(RabbitMQMyTardisConsumer.class);
	
	private static final Boolean DEFAULT_SSL_AUTOTRUST_SELFSIGNED = false;
	
	private String rabbitmqExchangeName;
	
	/**
	 * Default contructor of MyTardisConsumer, calling the parent class constructor @{link com.rabbitmq.client.DefaultConsumer}
	 * @param channel: 
	 */
	public RabbitMQMyTardisConsumer(Channel channel, String rabbitMqExchangeName) throws IOException {
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
    	String myTardisHost = getStringFromHeader(receivedHeader.get("myTardisHost"));
    	String myTardisUser = getStringFromHeader(receivedHeader.get("myTardisUser"));
    	String myTardisPassword = getStringFromHeader(receivedHeader.get("myTardisPassword"));
    	String myTardisProtocol = getStringFromHeader(receivedHeader.get("myTardisProtocol"));    	
    	String datafileIdStr = getStringFromHeader(receivedHeader.get("datafileId"));
    	Integer datafileId = Integer.valueOf(datafileIdStr);
    	
    	// Create a client for MyTardis
    	TardisClient client = new TardisClient(myTardisHost, myTardisUser, myTardisPassword, myTardisProtocol);
    	
    	
    	DatasetFile datasetFile = null;
    	byte[] myTardisDatafileContent = null;
    	try {
    		final Boolean trustSelfSigned = (System.getenv("SSL_AUTOTRUST_SELFSIGNED") != null) ? Boolean.valueOf(System.getenv("SSL_AUTOTRUST_SELFSIGNED")) : DEFAULT_SSL_AUTOTRUST_SELFSIGNED;
    		
        	// Trusts self-signed certificate
        	if (myTardisProtocol.equals("https") && trustSelfSigned == true) {
        		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals(myTardisHost));
        		InstallCert.installCert(myTardisHost);
        	}
        	
    		// Retrieves file details with MyTardis API 
    		logger.debug("Sending MyTardis request to " + myTardisHost);
    		datasetFile = (DatasetFile) client.getObjectById(DatasetFile.class, datafileId);
    		logger.debug("Received MyTardis Datasetfile.");
    		
    		// Builds the map of headers to attach to the response
    		Map<String, Object> headers = buildHeaders(datasetFile, client);
    		
    		// Retrieves file content with MyTardis API
        	logger.debug("Downloading MyTardis datafile content from " + myTardisHost);
    		myTardisDatafileContent = client.getDatasetFileContentById(datafileId);
    		logger.debug("Downloaded datafile content.");
            
            byte[] encodedData = Base64.encodeBase64(myTardisDatafileContent);
            
   		 	// Trims the routing key to keep only the routing key to return
            String returnRoutingKey = envelope.getRoutingKey()
            		.replaceAll("estored.", "")
            		.replaceAll("ds.", "")
            		.replaceAll(".recompute", "");
        	
            // Sends response back to new routing key, with the meta-data as message headers and the file content as message body
            sendMessage(this.rabbitmqExchangeName, returnRoutingKey, datasetFile.getMimetype(), encodedData, headers);
    	
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    	}            	
    }
    
    /**
     * Prepares the headers for a given DatasetFile, retrieving existing meta-data of this DatasetFile on MyTardis
     * 
     * @param datasetFile: the DatasetFile to get meta-data from
     * @param client: the instance of MyTardis client where the DatasetFile is located
     * 
     * @return the map of headers to be sent along with the given DatasetFile
     * 
     * @throws Exception
     */
	private Map<String, Object> buildHeaders(DatasetFile datasetFile, TardisClient client) throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();
		
		// Adds basic headers with file info
		headers.put("File Name", datasetFile.getFilename());
		logger.debug("Adding Header: File Name = " + datasetFile.getFilename());
		headers.put("File Size", Integer.parseInt(datasetFile.getSize()) / 1024 + " KB");
		logger.debug("Adding Header: File Size = " + Integer.parseInt(datasetFile.getSize()) / 1024 + " KB");
		
		// Retrieves MyTardis parameter sets for this Datasetfile	
		@SuppressWarnings("unchecked")
		ArrayList<AbstractMap<Object, Object>> parameterSets = (ArrayList<AbstractMap<Object, Object>>) datasetFile.getParameterSets();

		// Iterates through the parameters sets
		for (AbstractMap<Object, Object> parameterSet : parameterSets) {
			
			// For each parameter set, retrieves the parameters
			@SuppressWarnings("unchecked")
			ArrayList<AbstractMap<Object, Object>> parameters = (ArrayList<AbstractMap<Object, Object>>) parameterSet.get("parameters");
			
			// Iterates through the parameters
			for (AbstractMap<Object, Object> parameter : parameters) {
				
				// For each parameter, 
				String parameterNameUri = (String)parameter.get("name");
				Parametername parameterName = (Parametername) client.getObjectByUri(Parametername.class, parameterNameUri);
				
				headers.put(parameterName.getFullName(), (String)parameter.get("string_value"));  
				logger.debug("Adding Header: " + parameterName.getFullName() + " = " + (String)parameter.get("string_value"));
			}
		}
		
		return headers;
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
	        
	        logger.debug("[handleDelivery] Sent message to routing key '" + routingKey + "'");
	        
	    } catch (FileNotFoundException e) {
	  		e.printStackTrace();
	  	} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
