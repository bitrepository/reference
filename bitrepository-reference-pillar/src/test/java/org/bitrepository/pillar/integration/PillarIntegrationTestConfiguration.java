/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.integration;

import java.io.IOException;
import java.util.Properties;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;

public class PillarIntegrationTestConfiguration {
    private final Properties properties = new Properties();

    public PillarIntegrationTestConfiguration(String propertiesFilePath) {
        loadProperties(propertiesFilePath);
    }

    public HttpServerConfiguration getHttpServerConfig() {
        HttpServerConfiguration config = new HttpServerConfiguration();
        int portNumber =
                Integer.parseInt(properties.getProperty("pillar.integrationtest.httpserver.port", "80"));
        config.setPortNumber(portNumber);
        config.setHttpServerName(properties.getProperty("pillar.integrationtest.httpserver.name"));
        config.setHttpServerPath(properties.getProperty("pillar.integrationtest.httpserver.path"));
        return config;
    }

    private void loadProperties(String propertiesFilePath) {
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean useEmbeddedPillar() {
        return properties.getProperty("pillar.integrationtest.useembeddedpillar", "false").equals("true");
    }

    public String getPillarUnderTestID() {
            return properties.getProperty("pillar.integrationtest.pillarid");
    }

    public boolean useEmbeddedMessagebus() {
        return properties.getProperty("pillar.integrationtest.useembeddedmessagebus", "false").equals("true");
    }
}
