package edu.rmit.eres.amqpclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;

import org.apache.commons.codec.binary.Base64;

import edu.rmit.eres.estored.datasource.examples.ExamplesGenerator;

/**
 * Consumer that handles RabbitMQ messages requesting eStoRED example data
 * Based on RabbitMQ default consumer (see @{link com.rabbitmq.client.DefaultConsumer})
 * 
 * @author Guillaume Prevost
 * @since 03 Jan. 2017
 */
public class ExamplesConsumer extends DefaultConsumer {
	
	private static final Logger logger = LoggerFactory.getLogger(ExamplesConsumer.class);
	
	private String rabbitmqExchangeName;
	
	/**
	 * Default contructor of MyTardisConsumer, calling the parent class constructor @{link com.rabbitmq.client.DefaultConsumer}
	 * @param channel: 
	 */
	public ExamplesConsumer(Channel channel, String rabbitMqExchangeName) {
		super(channel);
		this.rabbitmqExchangeName = rabbitMqExchangeName;
	}
	
	/**
	 * Handles a request for retrieving example data:
	 * - examples.plain: simple "Hello, World!" text format
	 * - examples.csv: simple CSV text format, from a local CSV file
	 * - examples.numbers: 10 numbers in JSON format
	 * - examples.coordinates: 10 X and Y coordinates in JSON format
	 * - examples.map: 3 different map features (point, polyline, polygon) in GeoJSON format, from a local GeoJSON file
	 * - examples.laneways: laneways of Melbourne city as map features in GeoJSON format, from an online web service
	 * - examples.graph: graph in Neo4J JSON format, from an online web service
	 * - examples.biggraph: graph of researchers, publications and institutions in Neo4J JSON format, from a local JSON file 
	 * - examples.festostructure: graph of the topology of a production line in BeSpaceD JSON format, from a local JSON file
	 * - examples.festosensors: sensor information to be used with 'festostructure' keyword, in BeSpaceD JSON format, from a local JSON file
	 */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    	
    	// Extracts content of the received message body
    	String message = new String(body, "UTF-8");
    	logger.info("[handleDelivery] Received:'" + message + "'");
    	
    	// Extracts data from received message headers
    	//Map<String, Object> receivedHeader = properties.getHeaders();
    	
    	byte[] messageContent = null;
    	String mimetype = null;
    	try {    		
    		// Builds the map of headers to attach to the response
    		Map<String, Object> headers = buildHeaders();
    		
    		 // Trims the routing key to keep only the routing key to return
            String returnRoutingKey = envelope.getRoutingKey()
            		.replaceAll("estored.", "")
            		.replaceAll("ds.", "")
            		.replaceAll(".recompute", "");
        	
    		// plain, numbers, coordinates, graph, biggraph, map, csv, table
    		if (returnRoutingKey.equals("examples.plain")) {
    			messageContent = ExamplesGenerator.plainText();
    			mimetype = "text/plain";
    		}
    		else if (returnRoutingKey.equals("examples.csv")) {
    			messageContent = ExamplesGenerator.csvComplete();
    			mimetype = "text/csv";
    		}
    		else if (returnRoutingKey.equals("examples.numbers")) {
    			messageContent = ExamplesGenerator.randomNumbers(10);
    			mimetype = "application/json";
    		}
    		else if (returnRoutingKey.equals("examples.coordinates")) {
    			messageContent = ExamplesGenerator.randomCoordinates(10);
    			mimetype = "application/json";
    		}
    		else if (returnRoutingKey.equals("examples.graph")) {
    			messageContent = ExamplesGenerator.graphRdSwitchboard();
    			mimetype = "application/json";
    		}
    		else if (returnRoutingKey.equals("examples.biggraph")) {
    			messageContent = ExamplesGenerator.graphResultsNeo4J();
    			mimetype = "application/json";
    		}
    		else if (returnRoutingKey.equals("examples.map")) {
    			messageContent = ExamplesGenerator.mapFeatures();
    			mimetype = "application/vnd.geo+json";
    		}
    		else if (returnRoutingKey.equals("examples.laneways")) {
    			messageContent = ExamplesGenerator.mapGreenLanewaysMapFeatures();
    			mimetype = "application/vnd.geo+json";
    		}
    		else if (returnRoutingKey.equals("examples.festostructure")) {
    			messageContent = ExamplesGenerator.festoStructure();
    			mimetype = "application/json";
    		}
    		else if (returnRoutingKey.equals("examples.festosensors")) {
    			messageContent = ExamplesGenerator.festoSensors();
    			mimetype = "application/json";
    		}
    		else if (returnRoutingKey.equals("examples.jpg")) {
    			messageContent = ExamplesGenerator.image("jpg");
    			mimetype = "image/jpeg";
    		}
    		else if (returnRoutingKey.equals("examples.bmp")) {
    			messageContent = ExamplesGenerator.image("bmp");
    			mimetype = "image/bmp";
    		}
    		else if (returnRoutingKey.equals("examples.gif")) {
    			messageContent = ExamplesGenerator.image("gif");
    			mimetype = "image/gif";
    		}
    		else if (returnRoutingKey.equals("examples.png")) {
    			messageContent = ExamplesGenerator.image("png");
    			mimetype = "image/png";
    		}
           
            // Sends response back to new routing key, with the meta-data as message headers and the file content as message body
            if (messageContent != null)
            	sendMessage(this.rabbitmqExchangeName, returnRoutingKey, mimetype, Base64.encodeBase64(messageContent), headers);
            else
            	logger.info("Routing key " + envelope.getRoutingKey() + " doesn't match any example. Nothing to send back.");
            
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
	private Map<String, Object> buildHeaders() throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();
		
		headers.put("headerName#1", "headerValue#1");
		headers.put("headerName#2", "headerValue#2");
		
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
