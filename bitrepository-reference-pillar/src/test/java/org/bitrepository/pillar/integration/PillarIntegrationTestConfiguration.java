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

public class PillarIntegrationTestConfiguration {
    private final Properties properties = new Properties();

    public PillarIntegrationTestConfiguration(String propertiesFilePath) {
        loadProperties(propertiesFilePath);
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

    public String getPrivateKeyFileLocation() {
        return properties.getProperty("pillar.integrationtest.privateKeyFile");
    }

    public boolean useEmbeddedMessagebus() {
        return properties.getProperty("pillar.integrationtest.useembeddedmessagebus", "false").equals("true");
    }
    
    public long getPillarOperationTimeout() {
        return Long.parseLong(properties.getProperty("pillar.integrationtest.operationtimeout", "10"));
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}
