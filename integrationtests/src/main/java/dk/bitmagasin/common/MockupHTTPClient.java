package dk.bitmagasin.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MockupHTTPClient {
	/** The log. */
	private static Log log = LogFactory.getLog(MockupHTTPClient.class);
	/** The settings for this setup. */
	private static final MockupSettings settings = MockupSettings.getInstance();
	
	/** Protocol for URLs. */
	private static final String PROTOCOL = "http";
	
	public static void main(String... args) {
		// ??
		try {
			File fil = new File("src/main/resources/ClientToPillarMessages.xsd");

			String path = "test.xml";
			URL url = putData(new FileInputStream(fil), path);
			System.out.println(url);
			getData(System.out, url.toExternalForm());
			url.toString();
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void putData(InputStream in, URL url) throws Exception {
		if(in == null || url == null) {
			throw new IllegalArgumentException("InputStream in: " + in 
					+ ", URL url: " + url);
		}
		log.info("Uploading data to '" + url + "'.");
		HttpURLConnection conn = null;
		OutputStream out = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			out = conn.getOutputStream();
			MockupUtils.copyInputStreamToOutputStream(in, out);
			out.flush();
			
			// HTTP code >= 300 means error!
			if(conn.getResponseCode() >= 300) {
				throw new Exception("Could not upload file, got responsecode '" 
						+ conn.getResponseCode() + "' with message: '"
						+ conn.getResponseMessage() + "'");
			}
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
			if(out != null) {
				out.close();
			}
		}
	}
	
	public static URL putData(InputStream in, String filename) throws Exception {
		if(in == null || filename == null || filename.isEmpty()) {
			throw new IllegalArgumentException("InputStream in: " + in 
					+ ", String filename: " + filename);
		}
		URL url;
		HttpURLConnection conn = null;
		OutputStream out = null;
		try {
			url = getURL(filename);
			log.info("Putting file '" + filename + "' at '" + url + "'");
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			out = conn.getOutputStream();
			MockupUtils.copyInputStreamToOutputStream(in, out);
			out.flush();
			
			// HTTP code >= 300 means error!
			if(conn.getResponseCode() >= 300) {
				throw new Exception("Could not upload file, got responsecode '" 
						+ conn.getResponseCode() + "' with message: '"
						+ conn.getResponseMessage() + "'");
			}
			
			log.info("Got the following response for putting '" + filename 
					+ "' at '" + url + "': " + conn.getResponseCode() + " "
					+ conn.getResponseMessage());
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
			if(out != null) {
				out.close();
			}
		}
		
		return url;
	}
	
	public static void getData(OutputStream out, String httpPath) 
	        throws Exception {
		if(out == null || httpPath == null || httpPath.isEmpty() 
				|| !httpPath.startsWith(PROTOCOL)) {
			throw new IllegalArgumentException("OutputStream out: '" + out
					+ "', httpPath: '" + httpPath + "'");
		}
		URL url = new URL(httpPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();
		MockupUtils.copyInputStreamToOutputStream(is, out);
	}
	
	public static URL getURL(String filename) throws MalformedURLException {
		return new URL(PROTOCOL, settings.getHttpUrl(), settings.getHttpPort(), 
				settings.getHttpPath() + "/" + filename);

	}
}
