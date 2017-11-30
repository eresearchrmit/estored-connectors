package edu.rmit.eres.estored.datasource.examples;

import net.minidev.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExamplesGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ExamplesGenerator.class);

	public static byte[] plainText() {
		logger.info("Inside plainText");
		
		String message = "<strong>Hello, World!<strong>";
		return message.getBytes();
	}
	
	public static byte[] randomNumbers(int howManyNumbers) {
		logger.info("Inside randomNumbers");

		Integer i = 0;
		Random randomGenerator = new Random();

		// Returns a series of random numbers (each between 1-100)
		List<Integer> randomNumbers = new ArrayList<Integer>();
		for (i = 0; i < howManyNumbers; i++) {
			randomNumbers.add(randomGenerator.nextInt(100));
		}
		
		return randomNumbers.toString().getBytes();
	}
	
	public static byte[] randomCoordinates(int howManyCoordinates) {
		logger.info("Inside randomCoordinates");

		Random randomGenerator = new Random();
		
		JSONObject coordinatesList = new JSONObject();
		for (Integer i = 0; i < howManyCoordinates; i++) {
			JSONObject randomCoordinates = new JSONObject();
			randomCoordinates.put("x", randomGenerator.nextInt(100));
			randomCoordinates.put("y", randomGenerator.nextInt(100));
			coordinatesList.put("coordinates", randomCoordinates);
		}

		return coordinatesList.toString().getBytes();
	}

	public static byte[] graphRdSwitchboard() {
		logger.info("Inside sendGraphRdSwitchboard");

		// Demo graph for RD switchboard
		String message = readFromUrl("http://rd-switchboard.net/api/graph/?reqkey=2007-08-voyage-mineralogy-biota-433724&accesskey=demo");
		// logger.info(message);

		if (message == null || message.isEmpty()) {
			logger.info("Could not send JSON graph (message is null or empty)");
			return null;
		}
		
		return message.getBytes();
	}
	
	public static byte[] graphResultsNeo4J() {
		logger.info("Inside graphResultsNeo4J");

		return readLocalFile("./datafiles/biggraph.json");		
	}

	public static byte[] festoStructure() {
		logger.info("Inside festoStructure");

		return readLocalFile("./datafiles/festo-topology.json");
	}

	public static byte[] festoSensors() {
		logger.info("Inside festoSensors");

		return readLocalFile("./datafiles/festo-events.json");
	}

	public static byte[] csvComplete() {
		logger.info("Inside csvComplete");
		
		return readLocalFile("./datafiles/test.csv");
	}

	public static byte[] mapFeatures() {
		logger.info("Inside mapFeatures");
		
		return readLocalFile("./datafiles/geojson-example.geojson");
	}

	public static byte[] mapGreenLanewaysMapFeatures() {
		logger.info("Inside mapGreenLanewaysMapFeatures");

		String url = "https://data.melbourne.vic.gov.au/api/geospatial/s8dq-xd4g?method=export&format=GeoJSON";
		String message = readFromSecuredUrl(url);

		if (message == null || message.isEmpty()) {
			logger.info("Could not send map features (message is null or empty)");
			return null;
		}
		
		return message.getBytes();
	}
	
	public static byte[] image(String extension) {
		logger.info("Inside image");
		
		return readLocalFile("./datafiles/earthquakes." + extension);
	}

	
	/**
	 ** Private utility methods
	 **/
	 
	/**
	 * Reads the content of a file in the local filesystem and return it 
	 * @param filePath: the path of the file to read
	 * @return the content of the file as a byte array
	 */
	private static byte[] readLocalFile(String filePath) {
		logger.info("Inside readGraphFromFile");

		byte[] fileContent = null;
		try {
			fileContent = Files.readAllBytes(Paths.get(filePath));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (fileContent == null || fileContent.length == 0) {
			logger.info("Could not read file (file content null or empty)");
			return null;
		}
		
		return fileContent;
	}
	
	/**
	 * Retrieves some data from a URL
	 * @param url: the URL where the data is located
	 * @return the data retrieved, as a String
	 */
	private static String readFromUrl(String url) {
		logger.info("Inside readFromUrl");

		StringBuilder sb = new StringBuilder();
		int cp;

		try {
			InputStream is = new URL(url).openStream();

			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
	
	/**
	 * Retrieves some data from a URL using SSL protocol
	 * @param url: the URL where the data is located
	 * @return the data retrieved, as a String
	 */
	private static String readFromSecuredUrl(String url) {
		logger.info("Inside readFromSecuredUrl");

		StringBuilder sb = new StringBuilder();
		int cp;

		try {
			HttpsURLConnection httpsCon = (HttpsURLConnection) new URL(url).openConnection();
			httpsCon.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			httpsCon.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			httpsCon.connect();
			InputStream is = httpsCon.getInputStream();

			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
}