/*
 * #%L
 * bitrepository-common
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
package org.bitrepository.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bitrepository.common.exception.ConfigurationException;

/**
 * General class for instantiating configurations based on xml
 *
 */
public class ConfigurationFactory {
    private ConfigurationFactory () {
    }

    /**
     * 
     * @param namespace The name space for the xml we are going to load
     * @param configurationClass The class to instantiate and load to
     * @param configurationFile The file containing the configuration data.
     * @return Returns a new instance of the indicated messageClass
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static <T> T createConfiguration(String namespace, Class<T> configurationClass, File configurationFile) 
    throws ConfigurationException {
        try {
            JAXBContext context = JAXBContext.newInstance(namespace);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StreamSource(new FileInputStream(configurationFile)));
        } catch(JAXBException jex) {
            throw new ConfigurationException("Unable to load configuration " + configurationClass + 
            		" from " + configurationFile, jex);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Didn't find indicated configuration file " + configurationFile, e);
		}
    }
}
