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

public class HttpServerConfiguration {
    
    private String serverClass = "HTTPServer";
    /** Protocol for URLs. */
    private String protocol = "http";
    /** The default port for the HTTP communication.*/
    private int portNumber = 80;
    /** The default name of the HTTP server. TODO retrieve from settings.*/
    private String httpServerName = "sandkasse-01.kb.dk";
    /** The path on the HTTP server to the location, where the data can be 
     * uploaded.*/
    private String httpServerPath = "/dav/";
    /**
     * Prefix to use when working with files on the http server. The prefix is used to distinguish between different 
     * users/processes working with the server in parallel
     */
    private String prefix = "";
    
    public String getProtocol() {
        return protocol;
    }
    public int getPortNumber() {
        return portNumber;
    }
    public String getHttpServerName() {
        return httpServerName;
    }
    public String getHttpServerPath() {
        return httpServerPath;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
    public void setHttpServerName(String httpServerName) {
        this.httpServerName = httpServerName;
    }
    public void setHttpServerPath(String httpServerPath) {
        this.httpServerPath = httpServerPath;
    }
    public void setFilePrefix(String prefix) {
        this.prefix = prefix;
    }
}
