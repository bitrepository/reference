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

/** Configuration for <code>HttpsServerConector</code> objects. */
public class HttpsServerConfiguration extends HttpServerConfiguration {

    /** @see #getFileExchangeClass() */
    protected String fileExchangeClass;
    /** The concrete fileexchange class to use */
    public String getFileExchangeClass() {
        return fileExchangeClass;
    }
    /** @see #getFileExchangeClass() */
    public void setFileExchangeClass(String fileExchangeClass) {
        this.fileExchangeClass = fileExchangeClass;
    }

    /** @see #getHttpsKeystorePath() */
    protected String httpsKeystorePath;
    /** Returns the location of the https keystore */
    public String getHttpsKeystorePath() {
        return httpsKeystorePath;
    }
    /** @see #getHttpsKeystorePath() */
    public void setHttpsKeystorePath(String httpsKeystorePath) {
        this.httpsKeystorePath = httpsKeystorePath;
    }

    /** @see #getHttpsKeyStorePassword() */
    protected String httpsKeyStorePassword;
    /** Returns the https keystore password*/
    public String getHttpsKeyStorePassword() {
        return httpsKeyStorePassword;
    }
    /** @see #getHttpsKeyStorePassword() */
    public void setHttpsKeyStorePassword(String httpsKeyStorePassword) {
        this.httpsKeyStorePassword = httpsKeyStorePassword;
    }

    /** @see #getHttpsCertificatePath() */
    protected String httpsCertificatePath;
    /** Returns the location of the https certificates.*/
    public String getHttpsCertificatePath() {
        return httpsCertificatePath;
    }
    /** @see #getHttpsCertificatePath() */
    public void setHttpsCertificatePath(String httpsCertificatePath) {
        this.httpsCertificatePath = httpsCertificatePath;
    }

    /** @see #getHttpsCertificateAlias() */
    protected String httpsCertificateAlias;
    /** Returns the certificate alias */
    public String getHttpsCertificateAlias() {
        return httpsCertificateAlias;
    }
    /** @see #getHttpsCertificateAlias() */
    public void setHttpsCertificateAlias(String httpsCertificateAlias) {
        this.httpsCertificateAlias = httpsCertificateAlias;
    }
}
