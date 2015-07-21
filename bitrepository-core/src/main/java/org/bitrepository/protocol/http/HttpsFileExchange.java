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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.CoordinationLayerException;

/**
 * Simple interface for data transfer between an application and a HTTPS server.
 */
public class HttpsFileExchange extends HttpFileExchange {
    /** The verifier for all the hostnames.*/
    private final AllHostnameVerifier hostnameVerifier;
    
    /**
     * Initialise HTTP file exchange.
     * @param settings The settings regarding the file exchange through HTTP.
     */
    public HttpsFileExchange(Settings settings) {
        super(settings);
        hostnameVerifier = new AllHostnameVerifier();
    }
    
    /**
     * Method for opening a HTTP connection to the given URL.
     * TODO needs some SSL stuff??
     * 
     * @param url The URL to open the connection to.
     * @return The HTTP connection to the given URL.
     */
    @Override
    protected HttpURLConnection getConnection(URL url) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(hostnameVerifier);
            return connection;
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not open the connection to the url '" + url + "'", e);
        }
    }
    
    /**
     * A HostnameVerifier which verifies all hostnames.
     */
    private class AllHostnameVerifier implements HostnameVerifier {  
        @Override
        public boolean verify(String string, SSLSession sslSession) {
            return true;
        }
    };

    @Override
    protected CloseableHttpClient getHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        try {
            builder.setSslcontext(SSLContext.getDefault());
        } catch (Exception e) {
            throw new IllegalStateException("Could not make Https Client.", e);
        }
    
        return builder.build();
    }
}
