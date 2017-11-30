package edu.rmit.eres.estored.connectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceClient {

	private static final Logger logger = LoggerFactory.getLogger(WebServiceConnector.class);
	
	// Demo graph for RD switchboard
	// http://rd-switchboard.net/2007-08-voyage-mineralogy-biota/118169/
	// String message = readFromUrl("http://rd-switchboard.net/api/graph/?reqkey=2007-08-voyage-mineralogy-biota-433724&accesskey=demo");
	
	String url = null;
	URLConnection connection = null;
	
	public WebServiceClient(String url) throws MalformedURLException, IOException {
		this.url = url;
		
		if (this.url.startsWith("https")) {
			HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			conn.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			this.connection = conn;
			connection.connect();
		}
		else if (this.url.startsWith("http")) {
			this.connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
		}

	}
	
	/**
	 * Retrieves some data from a URL using SSL (HTTPS protocol)
	 * @param url: the URL where the data is located
	 * @return the data retrieved, as a String
	 */
	public String readString() {
		logger.info("Inside readString");

		StringBuilder sb = new StringBuilder();
		int cp;

		try {
			
			InputStream is = this.connection.getInputStream();
			
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
	
	public byte[] readBytes() {
		logger.info("Inside readBytes");
		return readString().getBytes();
	}
	
	public Long getContentLength() {
		logger.info("Inside getContentLength");
		return this.connection.getContentLengthLong();
	}
	
	public String getContentType() {
		logger.info("Inside getContentType");
		return this.connection.getContentType();
	}	
}