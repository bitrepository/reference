/*
 * #%L
 * Bitmagasin Protocol
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.StreamUtils;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple interface for data transfer between an application and a HTTP server.
 */
public class HttpFileExchange implements FileExchange {
    /** The log. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** The lower boundary for the error codes of the HTTP codes.*/
    private static final int HTTP_ERROR_CODE_BARRIER = 300;
    /** The settings for the file exchange.*/
    protected final Settings settings;
    
    /**
     * Initialise HTTP file exchange.
     * @param settings The settings regarding the file exchange through HTTP.
     */
    public HttpFileExchange(Settings settings) {
        this.settings = settings;
    }
    
    @Override
    public void uploadToServer(InputStream in, URL url) throws IOException {
        performUpload(in, url);
    }
    
    @Override
    public InputStream downloadFromServer(URL url) throws IOException {
        return retrieveStream(url);
    }
    
    @Override
    public URL uploadToServer(InputStream in, String filename)
            throws IOException {
        if(in == null || filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("InputStream in: " + in 
                    + ", String filename: " + filename);
        }
        URL url = getURL(filename);
        performUpload(in, url);
        return url;
    }
    
    @Override
    public URL uploadToServer(File dataFile) {
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
    
    @Override
    public void downloadFromServer(OutputStream out, URL url)
            throws IOException {
        performDownload(out, url);
    }
    
    @Override
    public void downloadFromServer(File outputFile, String fileAddress) {
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
    protected void performDownload(OutputStream out, URL url)
            throws IOException {
        if(out == null || url == null) {
            throw new IllegalArgumentException("OutputStream out: '" + out
                    + "', URL: '" + url + "'");
        }
        InputStream is = retrieveStream(url);
        StreamUtils.copyInputStreamToOutputStream(is, out);
    }
    
    /**
     * Retrieves the Input stream for a given URL.
     * @param url The URL to retrieve.
     * @return The InputStream to the given URL.
     * @throws IOException If any problems occurs during the retrieval.
     */
    protected InputStream retrieveStream(URL url) throws IOException {
        HttpURLConnection conn = getConnection(url);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        return conn.getInputStream();
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
    private void performUpload(InputStream in, URL url) throws IOException {
        HttpClient httpClient = null;
        try {
            httpClient = getHttpClient();
            HttpPut httpPut = new HttpPut(url.toExternalForm());
            InputStreamEntity reqEntity = new InputStreamEntity(in, -1);
            reqEntity.setChunked(true);
            httpPut.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(httpPut);
            
            // HTTP code >= 300 means error!
            if(response.getStatusLine().getStatusCode() >= HTTP_ERROR_CODE_BARRIER) {
                throw new IOException("Could not upload file to URL '" + url.toExternalForm() + "'. got status code '" 
                        + response.getStatusLine() + "'");
            }
            log.debug("Uploaded datastream to url '" + url.toString() + "' and "
                    + "received the response line '" + response.getStatusLine() + "'.");
        } finally {
            if(httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }
    
    @Override
    public URL getURL(String filename) throws MalformedURLException {
        ArgumentValidator.checkNotNullOrEmpty(filename, "String fileName");
        ArgumentValidator.checkNotNull(settings.getReferenceSettings().getFileExchangeSettings(), 
                "The ReferenceSettings are missing the settings for the file exchange.");
        FileExchangeSettings feSettings = settings.getReferenceSettings().getFileExchangeSettings();
        try {
            String urlEncodedFilename = URLEncoder.encode(filename, "UTF-8");

            // create the URL based on hardcoded values (change to using settings!)
            URL res = new URL(feSettings.getProtocolType().value(), feSettings.getServerName(), 
                    feSettings.getPort().intValue(), feSettings.getPath() + "/" + urlEncodedFilename);
            return res;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Cannot create the URL.", e);
        }
    }
    
    /**
     * Method for opening a HTTP connection to the given URL.
     * 
     * @param url The URL to open the connection to.
     * @return The HTTP connection to the given URL.
     */
    protected HttpURLConnection getConnection(URL url) {
        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not open the connection to the url '" + url + "'", e);
        }
    }
    
    /**
     * Retrieves the HttpClient with the correct setup.
     * For HTTPS this should be overridden with SSL context.
     * @return The HttpClient for this FileExchange.
     */
    protected HttpClient getHttpClient() {
        return new DefaultHttpClient();
    }

    @Override
    public void deleteFromServer(URL url) throws IOException, URISyntaxException {
        HttpClient httpClient = null;
        try {
            httpClient = getHttpClient();
            HttpDelete deleteOperation = new HttpDelete(url.toURI()); 
            httpClient.execute(deleteOperation);
        } finally {
            if(httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }
}
