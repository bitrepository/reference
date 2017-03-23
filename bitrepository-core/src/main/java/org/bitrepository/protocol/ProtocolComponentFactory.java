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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.settings.referencesettings.ProtocolType;

/**
 * Provides access to the different component in the org.bitrepository.org.bitrepository.protocol module (Spring/IOC wannabe)
 */
public final class ProtocolComponentFactory {

    /**
     * Gets you an <code>FileExchange</code> instance for data communication. Is instantiated based on the
     * configurations.
     *
     * @param settings The settings for the file exchange.
     * @return The FileExchange according to the configuration.
     */
    public static FileExchange createFileExchange(Settings settings) {

        FileExchange fileExchange;
        if ((settings.getReferenceSettings().getFileExchangeSettings() != null)) {
            ProtocolType protocolType = settings.getReferenceSettings().getFileExchangeSettings().getProtocolType();
            if (protocolType == ProtocolType.HTTP) {
                fileExchange = new HttpFileExchange(settings);
            } else if (protocolType == ProtocolType.HTTPS) {
                fileExchange = new HttpsFileExchange(settings);
            } else if (protocolType == ProtocolType.FILE) {
                fileExchange = new LocalFileExchange(
                        settings.getReferenceSettings().getFileExchangeSettings().getPath());
            } else {
                fileExchange = null;
            }
        } else {
            fileExchange = new HttpFileExchange(settings);
        }

        return fileExchange;
    }
}
