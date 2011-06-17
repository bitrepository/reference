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
package org.bitrepository.protocol.fileexchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.bitrepository.protocol.CoordinationLayerException;
import org.jaccept.TestEventManager;
import org.testng.Assert;

/**
 * Test helper class for uploading and downloading files from a http server. 
 * <p>
 * Also contains functionality for asserting whether a file is present on the HTTP server.
 */
public class HttpServer {
    /** The lower boundary for the error codes of the HTTP codes.*/
    private static final int HTTP_ERROR_CODE_BARRIER = 300;

    /** The configuration for the file exchange.*/
    private final HttpServerConfiguration config; 
    private final TestEventManager testEventManager;
    public static final String TEMP_DOWNLOADED_FILES_ROOT = "target/temp-download-dir";
    private final String tempDownloadedFilesDir;

    /**
     * Initialize HTTP file exchange.
     *
     * @param configuration The configuration for file exchange.
     */
    public HttpServer(HttpServerConfiguration config, TestEventManager testEventManager) {
        this.config = config;
        this.testEventManager = testEventManager;
        tempDownloadedFilesDir = TEMP_DOWNLOADED_FILES_ROOT + File.separator + config.getHttpServerName();
        File tempDownloadedFilesDirHandle = new File(tempDownloadedFilesDir);
        tempDownloadedFilesDirHandle.delete();
        tempDownloadedFilesDirHandle.mkdirs();
    }

    /**
     * Simulates a upload from a pillar to the HTTP server.
     * @param in The input stream for the file which should be uploaded. 
     * @param fileAddress The url where the file should be uploaded.
     */
    public void uploadFile(FileInputStream in, URL fileAddress) {
        performUpload(in, fileAddress);
    }

    /**
     * Simulates a download from a http server to a pillar.
     * @param out The output stream where the file should be saved.
     * @param url The url the download the file from.
     */
    public void downloadFile(OutputStream out, URL url)
    throws IOException {
        performDownload(out, url);
    }

    /**
     * Removes the indicated file from the server. Will not complain if the file isn't present.
     * @param filename
     */
    public void removeFile(String filename) throws Exception {
        URL url = getURL(filename);
        HttpURLConnection conn = getConnection(url);
        conn = getConnection(url);
        conn.setRequestMethod("DELETE");
        try {
            conn.getInputStream();
        } catch (FileNotFoundException fnf) {} // No problem
    }

    private void loadFile(File outputFile, String fileAddress) {
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
     * Used in test to save files onto the http server. Simulates a client putting a file on the http server prior to a 
     * put/replace request.
     * 
     * ToDo: Used more flexibl;e mapping between urls and file names.
     * @param in Read the file from here
     * @param filename Name of the file
     * @return
     * @throws IOException
     */
    public URL saveFile(FileInputStream in, String filename)
    throws IOException {
        URL url = getURL(filename);
        performUpload(in, url);
        return url;
    }


    /**
     * Retrieves the data from a given url and puts it onto a given 
     * outputstream. It has to be a 'HTTP' url, since the data is retrieved 
     * through a HTTP-request.
     * 
     * @param out The output stream to put the data.
     * @param url The url for where the data should be retrieved.
     */
    private void performDownload(OutputStream out, URL url) {
        try {
            HttpURLConnection conn = getConnection(url);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            copyInputStreamToOutputStream(is, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for putting data on the HTTP-server of a given url.
     * 
     * @param in The data to put into the url.
     * @param url The place to put the data.
     */
    private void performUpload(InputStream in, URL url) {
        HttpURLConnection conn = null;
        OutputStream out = null;
        try {
            conn = getConnection(url);
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            out = conn.getOutputStream();
            copyInputStreamToOutputStream(in, out);
            out.flush();

            // HTTP code >= 300 means error!
            if(conn.getResponseCode() >= HTTP_ERROR_CODE_BARRIER) {
                throw new RuntimeException("Could not upload file, got "
                        + "responsecode '" + conn.getResponseCode() 
                        + "' with message: '" + conn.getResponseMessage() 
                        + "'");
            }
            testEventManager.addResult("Uploaded file to url '" + url.toString() + "' and "
                    + "received the response code '" + conn.getResponseCode() 
                    + "' with the response message '" 
                    + conn.getResponseMessage() + "'.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Calculates the url for the giving file based on the http configuration */
    public URL getURL(String filename) throws MalformedURLException {
        return new URL(
                config.getProtocol(), 
                config.getHttpServerName(), 
                config.getPortNumber(), 
                config.getHttpServerPath() + filename);
    }

    /**
     * Utility function for moving data from an inputstream to an outputstream.
     * 
     * @param in The input stream to copy to the output stream.
     * @param out The output stream where the input stream should be copied.
     */
    private void copyInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
        if(in == null || out == null) {
            throw new IllegalArgumentException("InputStream: " + in 
                    + ", OutputStream: " + out);
        }

        try {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        } finally {
            in.close();
        }
    }

    /**
     * Method for opening a HTTP connection to the given URL.
     * 
     * @param url The URL to open the connection to.
     * @return The HTTP connection to the given URL.
     * @throws IOException 
     */
    private HttpURLConnection getConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Compares the 
     * @param referenceFile
     * @param fileAddress
     * @throws IOException 
     */
    public void assertFileEquals(File referenceFile, String fileAddress) throws IOException {
        File serverFile = new File(tempDownloadedFilesDir, referenceFile.getName()); 
        loadFile(serverFile, fileAddress);
        if (!serverFile.exists()) {
            Assert.fail("File " + referenceFile.getName()+ " not found on httpServer" ); 
        } else {
            Assert.assertEquals( 
                    FileUtils.readFileToString(serverFile, "utf-8"),
                    FileUtils.readFileToString(referenceFile, "utf-8"
                    ));
        }
    }

    public void clearFiles() {
        //What to do here?
    }
}
