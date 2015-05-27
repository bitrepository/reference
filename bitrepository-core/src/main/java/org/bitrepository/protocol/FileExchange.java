/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Provides functionality for exchanging files between two components through a shared file exchange location reference
 * through a common url.
 */
public interface FileExchange {
    
    /**
     * Method for uploading to a specific URL.
     * @param in The stream from the file to upload.
     * @param url The URL where the stream is to be put.
     * @throws IOException If any problems occurs during the transportation of
     * the data.
     */
    void putFile(InputStream in, URL url) throws IOException;

    /**
     * Puts a given file onto a http-server.
     *
     * @param file The file to be put into the http-server.
     * @return The url for the file, when it has been placed onto the
     * http-server.
     */
    URL putFile(File file);

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
    void getFile(OutputStream out, URL url)
            throws IOException;
    /**
     * Method for retrieving a specific URL as a stream.
     * @param url The URL to retrieve.
     * @return The InputStream for the data at the given URL.
     * @throws IOException If any problems occurs during the transportation of
     * the data.
     */
    InputStream getFile(URL url) throws IOException;

    /**
     * Method for downloading a file at a given adress.
     *
     * @param outputFile The file where the data at the address should be
     * placed.
     * @param fileAddress The address where the data should be downloaded from.
     */
    void getFile(File outputFile, String fileAddress);

    /**
     * Creates the URL based on a filename.
     *
     * @param filename The name of the piece of data to transfer (in the form
     * of a file).
     * @return The URL containing the filename.
     * @throws MalformedURLException If the filename prevents the creation of
     * a valid URL.
     */
    URL getURL(String filename) throws MalformedURLException;
    
    /**
     * Removes a file from the given URL.
     * 
     * @param url The URL where the file should be removed from.
     * @throws IOException If issues occurs while removing the file.
     * @throws URISyntaxException If the URL is not valid.
     */
    void deleteFile(URL url) throws IOException, URISyntaxException;
}
