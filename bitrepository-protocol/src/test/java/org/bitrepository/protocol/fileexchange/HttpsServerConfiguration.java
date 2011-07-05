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

public class HttpsServerConfiguration extends HttpServerConfiguration {
	protected String fileExchangeClass;
    protected String httpsKeystorePath;
    protected String httpsKeyStorePassword;
    protected String httpsCertificatePath;
    protected String httpsCertificateAlias;
    public String getFileExchangeClass() {
		return fileExchangeClass;
	}
	public void setFileExchangeClass(String fileExchangeClass) {
		this.fileExchangeClass = fileExchangeClass;
	}
	public String getHttpsKeystorePath() {
		return httpsKeystorePath;
	}
	public void setHttpsKeystorePath(String httpsKeystorePath) {
		this.httpsKeystorePath = httpsKeystorePath;
	}
	public String getHttpsKeyStorePassword() {
		return httpsKeyStorePassword;
	}
	public void setHttpsKeyStorePassword(String httpsKeyStorePassword) {
		this.httpsKeyStorePassword = httpsKeyStorePassword;
	}
	public String getHttpsCertificatePath() {
		return httpsCertificatePath;
	}
	public void setHttpsCertificatePath(String httpsCertificatePath) {
		this.httpsCertificatePath = httpsCertificatePath;
	}
	public String getHttpsCertificateAlias() {
		return httpsCertificateAlias;
	}
	public void setHttpsCertificateAlias(String httpsCertificateAlias) {
		this.httpsCertificateAlias = httpsCertificateAlias;
	}
}
