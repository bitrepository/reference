/*
 * #%L
 * bitrepository-access-client
 * *
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
package org.bitrepository.alarm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.bitrepository.alarm.handling.handlers.AlarmStorer;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.SecurityManagerUtil;
import org.bitrepository.service.ServiceSettingsProvider;
import org.bitrepository.settings.referencesettings.ServiceType;

/**
 * Class for launching an alarm service.
 */
public class AlarmServiceFactory {
    /** The alarm service. 
     * @see #getAlarmService().*/
    private static AlarmService alarmService;
    /** The path to the directory containing the configuration files.*/
    private static String configurationDir;

    /** The properties file holding implementation specifics for the alarm service. */
    private static final String CONFIGFILE = "alarmservice.properties";
    /** Property key to tell where to locate the path and filename to the private key file. */
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.alarm-service.privateKeyFile";
        
    /**
     * Private constructor as the class is meant to be used in a static way.
     */
    private AlarmServiceFactory() { }
    
    /**
     * Initialize the factory with configuration. 
     * @param confDir String containing the path to the AlarmService's configuration directory
     */
    public static synchronized void init(String confDir) {
        configurationDir = confDir;
    }
    
    /**
     * Factory method to retrieve AlarmService  
     * @return The AlarmService.
     */
    public static synchronized AlarmService getAlarmService() {
        if(alarmService == null) {

            ServiceSettingsProvider settingsLoader =
                    new ServiceSettingsProvider(new XMLFileSettingsLoader(configurationDir), ServiceType.ALARM_SERVICE);


            Settings settings = settingsLoader.getSettings();
            try {
                String componentID = settings.getReferenceSettings().getAlarmServiceSettings().getID();

                Path certificate = Paths.get(getPrivateKeyFile());

                SecurityManager securityManager = SecurityManagerUtil.getSecurityManager(settings,
                                                                                         certificate,
                                                                                         componentID);

                alarmService = new BasicAlarmService(settings, securityManager);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return alarmService;
    }
    
    /**
     * Loads the properties.
     * @throws IOException If any input/output issues occurs.
     */
    private static String getPrivateKeyFile() throws IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(configurationDir, CONFIGFILE);
        try (BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile))) {
            properties.load(propertiesReader);
        }
        return properties.getProperty(PRIVATE_KEY_FILE);
    }
}
