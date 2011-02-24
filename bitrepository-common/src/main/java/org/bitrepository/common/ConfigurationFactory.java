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
 * General class for instantiating configurations based on xml files
 *
 */
public final class ConfigurationFactory {
	/** The <code>ConfigurationFactory</code> this suffix to construct the '${module} + 
	 * CONFIGURATION_FILE_SUFFIX_PROPERTY' property used when trying to find a configuration file.
	 */
	public static final String CONFIGURATION_FILE_PROPERTY_SUFFIX = ".configuration.file";
	/**
	 * The <code>ConfigurationFactory</code> looks for configuration files here.
	 */
	public static final String CONFIGURATION_DIR_PROPERTY = "configuration.dir";
	public static final String TEST_CONFIGURATION_PATH = "target/test-classes/configurations/xml";
	
	/** Private constructor to prevent instantiation of this class */
    private ConfigurationFactory () {
    }

    /**
     * Loads a module configuration. The following naming conventions must be followed:
     * 
     * <ul>
     * <li> The configuration class is named ${Module}Configuration, where 
     * ${Module} is the name of the java module in upper camel case notation, eg. AccessClientConfiguration.xsd.
     * <li> The naming standard as defined by the ModuleCharacteristics object
     * <li> The name space of the xml is 'org.bitrepository.${module}.configuration.${module}configuration, where 
     * ${module} is the name of the java module in lower case notation, eg. accessclient.
     * </ul>
     * 
     * The following prioritized sequence is used to find the configuration file. 
     * <ol>
     * <li>If the ${module}.configuration.dir property is set, the configuration will be load from here.</li>
     * <li>If the configuration.file property is set, the configuration will be load from here as 
     * ${module}-configuration.xml.</li>
     * <li>If a configuration/${module}-configuration.xml file exists, this will be used.</li>
     * <li>If a target/test-classes/configurations/xml/${module}-configuration.xml file exists, this will be used. 
     * This option is used to load the default test configuration</li>
     * </ol>
     * 
     * @param configurationClass The class to instantiate and load to
     * @param moduleCharacteristics Used for extracting name strings for the module
     * @return Returns a new instance of the indicated messageClass
     */
    public static <T> T loadConfiguration( 
    		ModuleCharacteristics moduleCharacteristics,
    		Class<T> configurationClass) {

		String namespace = "org.bitrepository." + moduleCharacteristics.getLowerCaseName() + ".configuration";
		
			String absoluteConfigurationFilePath = 
				System.getProperty(moduleCharacteristics.getLowerCaseName()+CONFIGURATION_FILE_PROPERTY_SUFFIX);
			
			if (absoluteConfigurationFilePath == null || absoluteConfigurationFilePath.isEmpty() ) {
				String generalConfigurationFolder = System.getProperty(CONFIGURATION_DIR_PROPERTY);
				if (generalConfigurationFolder != null && !generalConfigurationFolder.isEmpty()) {
					File file = new File(generalConfigurationFolder, 
							moduleCharacteristics.getLowerCaseName() + "-configuration.xml");
					if (file.exists()) {
						absoluteConfigurationFilePath = file.getAbsolutePath();
					}
				}
			}
			
			if (absoluteConfigurationFilePath == null || absoluteConfigurationFilePath.isEmpty() ) {
				File file = new File( "configuration", 
						moduleCharacteristics.getLowerCaseName() + "-configuration.xml");
				if (file.exists()) {
					absoluteConfigurationFilePath = file.getAbsolutePath();
				}
			}
			
			if (absoluteConfigurationFilePath == null || absoluteConfigurationFilePath.isEmpty() ) {
				File file = new File( TEST_CONFIGURATION_PATH, 
						moduleCharacteristics.getLowerCaseName() + "-configuration.xml");
				if (file.exists()) {
					absoluteConfigurationFilePath = file.getAbsolutePath();
				}
			}
			
			// TODO The exception message should contain information on the attempts to lookup the configuration file
			if (absoluteConfigurationFilePath == null || absoluteConfigurationFilePath.isEmpty() ) {
				throw new ConfigurationException("Unable to find a configuration file for the " + 
						moduleCharacteristics.getLowerCaseNameWithHyphen() + " module");
			}
			
			return loadConfiguration(namespace, configurationClass, new File(absoluteConfigurationFilePath));
    }
    
    /**
     * 
     * @param namespace The name space for the xml we are going to load
     * @param configurationClass The class to instantiate and load to
     * @param configurationFile The file containing the configuration data.
     * @return Returns a new instance of the indicated messageClass
     */
    @SuppressWarnings("unchecked")
    private static <T> T loadConfiguration(
    		String namespace, 
    		Class<T> configurationClass, 
    		File configurationFile) {
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
