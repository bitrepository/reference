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

import org.bitrepository.settings.referencesettings.FileExchangeSettings;

/**
 * Configuration for {@link HttpServerConnector} objects. Pretty obsoleted as it only delegates to the
 * Reference settings Fileexchange configuration.
 */
public class HttpServerConfiguration {
    private final FileExchangeSettings fileExchangeSettings;

    public HttpServerConfiguration(FileExchangeSettings fileExchangeSettings) {
        this.fileExchangeSettings = fileExchangeSettings;
    }

    /**
     * Prefix to use when working with files on the http server. The prefix is used to distinguish between different 
     * users/processes working with the server in parallel
     */
    public String getProtocol() {
        return fileExchangeSettings.getProtocolType().value();
    }
    /** The http server port */
    public int getPortNumber() {
        return fileExchangeSettings.getPort().intValue();
    }
    /** The name identifying the http server */
    public String getHttpServerName() {
        return fileExchangeSettings.getServerName();
    }
    /** The path to the location we are going to connect to as in the format ${URL}:${PORT}/${PATH} */
    public String getHttpServerPath() {
        return fileExchangeSettings.getPath();
    }
}
