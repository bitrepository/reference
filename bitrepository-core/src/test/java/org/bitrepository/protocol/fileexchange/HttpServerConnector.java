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
public class HttpServerConnector {
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
     * @param config The configuration for file exchange.
     */
    public HttpServerConnector(HttpServerConfiguration config, TestEventManager testEventManager) {
        this.config = config;
        this.testEventManager = testEventManager;
        tempDownloadedFilesDir = TEMP_DOWNLOADED_FILES_ROOT + File.separator + config.getHttpServerName();
        File tempDownloadedFilesDirHandle = new File(tempDownloadedFilesDir);
        tempDownloadedFilesDirHandle.delete();
        tempDownloadedFilesDirHandle.mkdirs();
    }

    /** 
     * Calculates the url for the giving file based on the http configuration.
     * @param filename
     * @return
     * @throws MalformedURLException
     */
    public URL getURL(String filename) throws MalformedURLException {
        return new URL(
                config.getProtocol(), 
                config.getHttpServerName(), 
                config.getPortNumber(), 
                config.getHttpServerPath() + "/" + filename);
    }
}
