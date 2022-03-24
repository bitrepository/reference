/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.protocol;

import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocolversiondefinition.ProtocolVersionDefinition;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProtocolVersionLoader {

    private static ProtocolVersionDefinition protocolVersionDefinition;

    /**
     * Private constructor
     */
    private ProtocolVersionLoader() {
    }

    /**
     * Load protocol version definition
     *
     * @return ProtocolVersionDefinition for the protocol version the project has been build against
     */
    public static ProtocolVersionDefinition loadProtocolVersion() {
        if (protocolVersionDefinition == null) {
            String fileLocation = "versioning/ProtocolVersionDefinition.xml";
            String schemaLocation = "versioning/ProtocolVersionDefinition.xsd";
            JaxbHelper jaxbHelper = new JaxbHelper(null, schemaLocation);

            InputStream configStreamLoad = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
            InputStream configStreamValidate = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
            if (configStreamLoad == null) {
                try {
                    configStreamLoad = new FileInputStream(fileLocation);
                    configStreamValidate = new FileInputStream(fileLocation);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("Unable to load settings from " + fileLocation, e);
                }
            }

            try {
                jaxbHelper.validate(configStreamValidate);
                protocolVersionDefinition = jaxbHelper.loadXml(ProtocolVersionDefinition.class, configStreamLoad);
            } catch (SAXException e) {
                throw new RuntimeException("Unable to validate settings from " +
                        Thread.currentThread().getContextClassLoader().getResource(fileLocation), e);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load settings from " + fileLocation, e);
            }
        }
        return protocolVersionDefinition;

    }
}
