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

import java.io.InputStream;

import javax.xml.bind.JAXBException;

/**
 * General class for instantiating configurations based on xml files
 */
public final class ConfigurationFactory {

    /**
     * This is configuration the <code>\ConfigurationFactory</code> will look for, if no test configuration is found.
     */
    public static final String DEFAULT_CONFIGURATION_CLASSPATH_LOCATION = 
        "configuration/xml/%s-configuration.xml";
    
    /**
     * This is test configuration the <code>ConfigurationFactory</code> will look for.
     */
    public static final String DEFAULT_TEST_CONFIGURATION_CLASSPATH_LOCATION = 
        "configuration/xml/%s-test-configuration.xml";
    
    /**
     * Loads the configuration for a module. 
     * 
     * The following prioritized sequence is used to find the configuration file. 
     * <ol>
     * <li>Tries to load the 'configuration/xml/%s-test-configuration.xml' file from the classpath,
     *  where %s is the modules name.</li>
     * <li>Tries to load the 'configuration/xml/%s--configuration.xml' file from the classpath,
     *  where %s is the modules name.</li>
     * </ol>
     * 
     * The modules name is retrieved via. the moduleCharacteristics objects getLowerCaseNameWithHyphen method.
     * 
     * @param moduleCharacteristics Used for extracting name strings for the module
     * @param configurationClass The class to load the configuration to. 
     * The namespace for this class (defined in the Jaxb generated package-info.java) is also used for the xml loading.
     * @return Returns a new instance of the indicated configuration loaded from file
     */
    public <T> T loadConfiguration(ModuleCharacteristics moduleCharacteristics, Class<T> configurationClass) {

        String defaultClassPathLocation = String.format(DEFAULT_CONFIGURATION_CLASSPATH_LOCATION, 
                moduleCharacteristics.getLowerCaseName());
        String defaultTestClassPathLocation = String.format(DEFAULT_TEST_CONFIGURATION_CLASSPATH_LOCATION, 
                        moduleCharacteristics.getLowerCaseName());

        InputStream configStream = ClassLoader.getSystemResourceAsStream(defaultTestClassPathLocation);
        if (configStream == null) {
            configStream = ClassLoader.getSystemResourceAsStream(defaultClassPathLocation);
        }
        if (configStream == null) {
            throw new ConfigurationException("Failed to find " + defaultTestClassPathLocation + " or " + 
                    defaultClassPathLocation + " in classpath");
        }
        try {
            return (T) JaxbHelper.loadXml(configurationClass, configStream);
        } catch (JAXBException e) {
            throw new ConfigurationException("Failed to load the configuration from " + configStream, 
                    e);
        }
    }
}
