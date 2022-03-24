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

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.ChunkyManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.CoordinationLayerException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;


public class HttpsFileExchange extends HttpFileExchange {
    private final HostnameVerifier hostnameVerifier;

    public HttpsFileExchange(Settings settings) {
        super(settings);
        hostnameVerifier = NoopHostnameVerifier.INSTANCE;
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

    @Override
    protected CloseableHttpClient getHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        try {
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", new SSLConnectionSocketFactory(
                            SSLContext.getDefault(),
                            hostnameVerifier))
                    .build();
            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry,
                    new ChunkyManagedHttpClientConnectionFactory(HTTP_CHUNK_SIZE),
                    SystemDefaultDnsResolver.INSTANCE);

            SocketConfig socketConfig = SocketConfig.custom()
                    .setSoKeepAlive(true)
                    .setSndBufSize(HTTP_BUFFER_SIZE)
                    .setRcvBufSize(HTTP_BUFFER_SIZE).build();
            connectionManager.setDefaultSocketConfig(socketConfig);

            builder.setConnectionManager(connectionManager);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not make Https Client.", e);
        }

        return builder.build();
    }
}
