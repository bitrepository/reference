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
package org.bitrepository.protocol.fileexchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import org.bitrepository.protocol.CoordinationLayerException;
import org.jaccept.TestEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contains functionality for uploading and downloading files from a HTTPS server. */
public class HttpsServerConnector extends HttpServerConnector {
    /** The logger . */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The type of keystore.*/
    private static final String SUN_JCEKS_KEYSTORE_TYPE = "JCEKS";
    /** The type of certificate.*/
    private static final String CERTIFICATE_TYPE = "X.509";
    /** The certificate algorithm.*/
    private static final String CERTIFICATE_ALGORITHM = "SunX509";
    /** The org.bitrepository.org.bitrepository.protocol type.*/
    private static final String SSL_PROTOCOL = "SSL";
    /** The randomisation algorithm.*/
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";

    /** The class for verifying that host during the handshakes.*/
    private HostnameVerifier hostnameVerifier;
    /** The context for the SSL connection.*/
    private final SSLContext sslContext;
    /** The configuration defining the https server */
    private final HttpsServerConfiguration config;

    /** Creates a connector based on the supplied settings */
    public HttpsServerConnector(HttpServerConfiguration configuration,
            TestEventManager testEventManager) {
        super(configuration, testEventManager);
        this.config = (HttpsServerConfiguration) configuration;

        File keystore = new File(config.getHttpsKeystorePath());
        KeyStore store;
        try {
            // load the keystore from a file, or if the file does not exist, then use the certificate to create the
            // keystore file.
            if(keystore.isFile()) {
                store = KeyStore.getInstance(SUN_JCEKS_KEYSTORE_TYPE);
                store.load(new FileInputStream(config.getHttpsKeystorePath()), 
                        config.getHttpsKeyStorePassword().toCharArray());
            } else {
                store = initKeyStore();
            }
        } catch (Exception e) {
            throw new CoordinationLayerException("No valid keystore file '" + config.getHttpsKeystorePath() 
                    + "' or no valid certificate file for generating the keystore file, '" 
                    + config.getHttpsCertificatePath() + "'.", e);
        }

        try {
            // initialise the SSLContext based on the keystore.
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(CERTIFICATE_ALGORITHM);
            kmf.init(store, config.getHttpsKeyStorePassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(CERTIFICATE_ALGORITHM);
            tmf.init(store);
            sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecureRandom.getInstance(RANDOM_ALGORITHM));
        } catch (Exception e) {
            // TODO handle.
            log.error("Could not initialise the sslContext.", e);
            throw new CoordinationLayerException("Could not initialise the SSL Context.", e);
        }

        hostnameVerifier = new HostnameVerifier() {  
            // TODO handle different? We currently accept everybody.
            @Override
            public boolean verify(String string, SSLSession sslSession) {
                return true;
            }
        };
    }

    /**
     * Method for initializing the keystore.
     * @return The keystore.
     */
    private KeyStore initKeyStore() {
        KeyStore store;
        try {
            // initialise the keystore
            store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(null, null);

            // load the certificate
            CertificateFactory certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
            FileInputStream fis = new FileInputStream(config.getHttpsCertificatePath());
            Certificate cert = certFactory.generateCertificate(fis);
            fis.close();

            // insert the certificate into the keystore
            store.setCertificateEntry(config.getHttpsCertificateAlias(), cert);

            // Write the keystore to a file, with the given password.
            OutputStream out = new FileOutputStream(config.getHttpsKeystorePath());
            store.store(out, config.getHttpsKeyStorePassword().toCharArray());
        } catch (Exception e) {
            throw new CoordinationLayerException("Could not initialise the keystore.", e);
        }

        return store;
    }

    @Override
    protected HttpURLConnection getConnection(URL url) {
        try {
            HttpsURLConnection res = (HttpsURLConnection) url.openConnection();
            res.setSSLSocketFactory(sslContext.getSocketFactory());
            res.setHostnameVerifier(hostnameVerifier);
            return res;
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not open a HTTPS connection to the url '" + url + "'", e);
        }
    }
}
