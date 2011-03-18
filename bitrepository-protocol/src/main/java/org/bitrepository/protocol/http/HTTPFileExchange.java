/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.protocol.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.bitrepository.protocol.CoordinationLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple interface for data transfer between an application and a HTTP server.
 * 
 * TODO read the configurations for the server where the data should be 
 * uploaded from settings.
 * 
 * @author jolf
 */
public final class HTTPFileExchange {
    /** The log. */
    private static Logger log = LoggerFactory.getLogger(HTTPFileExchange.class);

    /** The size of the IO buffer.*/
    private static final int IO_BUFFER_SIZE = 1024;

    /** Protocol for URLs. */
    private static final String PROTOCOL = "http";
    
    /** The default port for the HTTP communication.*/
    private static final int PORT_NUMBER = 80;
    
    /** The default name of the HTTP server. TODO retrieve from settings.*/
    private static final String HTTP_SERVER_NAME = "sandkasse-01.kb.dk";
    
    /** The path on the HTTP server to the location, where the data can be 
     * uploaded.*/
    private static final String HTTP_SERVER_PATH = "/dav";
    
    /** The lower boundary for the error codes of the HTTP codes.*/
    private static final int HTTP_ERROR_CODE_BARRIER = 300;

    /**
     * Private constructor to prevent use of this utility method.
     */
    private HTTPFileExchange() { }
    
    /**
     * Put a piece of data onto a http-server and returns the url for the 
     * location of this data.
     * 
     * @param in The inputstream to the data to be put onti the http-server.
     * @param filename The name of the piece of data to be put onto the 
     * http-server.
     * @return The url of the location for the data on the http-server.
     * @throws IOException If any problems occurs during the transportation of
     * the data.
     */
    public static URL uploadToServer(InputStream in, String filename) 
            throws IOException {
        if(in == null || filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("InputStream in: " + in 
                    + ", String filename: " + filename);
        }
        URL url = getURL(filename);
        performUpload(in, url);
        return url;
    }
    
    /**
     * Puts a given file onto a http-server.
     * 
     * @param dataFile The file to be put into the http-server.
     * @return The url for the file, when it has been placed onto the 
     * http-server.
     */
    public static URL uploadToServer(File dataFile) {
        if(dataFile == null) {
            throw new IllegalArgumentException("The datafile may not be null.");
        }
        if(!dataFile.isFile()) {
            throw new IllegalArgumentException("The datafile '" 
                    + dataFile.getPath() + "' is not a proper file.");
        }
        
        try {
            // generate the URL for the file.
            URL url = getURL(dataFile.getName());

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dataFile);
                performUpload(fis, url);
            } finally {
                if(fis != null) {
                    fis.close();
                }
            }
            return url;
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not upload the file '"
                    + dataFile.getAbsolutePath() + "' to the server." , e);
        }
    }
    
    /**
     * Retrieves the data from a given url and puts it onto a given 
     * outputstream. It has to be a 'HTTP' url, since the data is retrieved 
     * through a HTTP-request.
     * 
     * @param out The output stream to put the data.
     * @param url The url for where the data should be retrieved.
     * @throws IOException If any problems occurs during the retrieval of the 
     * data.
     */
    public static void downloadFromServer(OutputStream out, URL url) 
            throws IOException {
        performDownload(out, url);
    }
    
    /**
     * Method for downloading a file at a given adress.
     *  
     * @param outputFile The file where the data at the address should be 
     * placed.
     * @param fileAddress The address where the data should be downloaded from.
     */
    public static void downloadFromServer(File outputFile, String fileAddress) {
        try {
            // retrieve the url and the outputstream for the file.
            URL url = new URL(fileAddress);
            FileOutputStream fos = new FileOutputStream(outputFile);
            
            // download the file.
            performDownload(fos, url);
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not download data "
                    + "from '" + fileAddress + "' to the file '" 
                    + outputFile.getAbsolutePath() + "'.", e);
        }
    }
    
    /**
     * Retrieves the data from a given url and puts it onto a given 
     * outputstream. It has to be a 'HTTP' url, since the data is retrieved 
     * through a HTTP-request.
     * 
     * @param out The output stream to put the data.
     * @param url The url for where the data should be retrieved.
     * @throws IOException If any problems occurs during the retrieval of the 
     * data.
     */
    private static void performDownload(OutputStream out, URL url) 
            throws IOException {
        if(out == null || url == null) {
            throw new IllegalArgumentException("OutputStream out: '" + out
                    + "', URL: '" + url + "'");
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        InputStream is = conn.getInputStream();
        copyInputStreamToOutputStream(is, out);
    }
    
    /**
     * Method for putting data on the HTTP-server of a given url.
     * 
     * TODO perhaps make it synchronized around the URL, to prevent data from 
     * trying to uploaded several times to the same location simultaneously. 
     * 
     * @param in The data to put into the url.
     * @param url The place to put the data.
     * @throws IOException If a problem with the connection occurs during the 
     * transaction. Also if the response code is 300 or above, which indicates
     * that the transaction has not been successful.
     */
    private static void performUpload(InputStream in, URL url) throws IOException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            out = conn.getOutputStream();
            copyInputStreamToOutputStream(in, out);
            out.flush();

            // HTTP code >= 300 means error!
            if(conn.getResponseCode() >= HTTP_ERROR_CODE_BARRIER) {
                throw new IOException("Could not upload file, got "
                        + "responsecode '" + conn.getResponseCode() 
                        + "' with message: '" + conn.getResponseMessage() 
                        + "'");
            }
            log.debug("Uploaded datastream to url '" + url.toString() + "' and "
                    + "received the response code '" + conn.getResponseCode() 
                    + "' with the response message '" 
                    + conn.getResponseMessage() + "'.");
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
            if(out != null) {
                out.close();
            }
        }
    }

    /**
     * Creates the URL based on a filename.
     * 
     * TODO the real thing! Load from settings.
     * 
     * @param filename The name of the piece of data to transfer (in the form
     * of a file).
     * @return The URL containing the filename.
     * @throws MalformedURLException If the filename prevents the creation of
     * a valid URL.
     */
    public static URL getURL(String filename) throws MalformedURLException {
        // create the URL based on hardcoded values (change to using settings!)
        URL res = new URL(PROTOCOL, HTTP_SERVER_NAME, PORT_NUMBER, 
                HTTP_SERVER_PATH + "/" + filename);
        return res;
    }

    /**
     * Utility function for moving data from an inputstream to an outputstream.
     * TODO move to a utility class.
     * 
     * @param in The input stream to copy to the output stream.
     * @param out The output stream where the input stream should be copied.
     * @throws IOException If anything problems occur with transferring the 
     * data between the streams.
     */
    private static void copyInputStreamToOutputStream(InputStream in, 
            OutputStream out) throws IOException {
        if(in == null || out == null) {
            throw new IllegalArgumentException("InputStream: " + in 
                    + ", OutputStream: " + out);
        }

        try {
            byte[] buf = new byte[IO_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        } finally {
            in.close();
        }
    }
}
