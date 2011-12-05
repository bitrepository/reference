/*
 * #%L
 * Bitrepository Integrity Client
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
package org.bitrepository.integrityclient;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;

/**
 * A simple launcher for the IntegrityService.
 * Takes the arguments:
 * <br/> - Collection id,
 * <br/> - path to settings (the step before the 'collection id' folder)
 * <br/> - The time between updates of the checksums. A third of this will be used for the 'interval'.
 */
public class IntegrityServiceLauncher {
    /** The default collection id in the development.*/
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "settings/xml";
    
    /** One week. Unless a third argument is given.*/
    private static final long DEFAULT_MAX_TIME_SINCE_UPDATE = 604800000;

    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
     * <li> The collection ID to load the settings for.</li>
     * </ol>
     */
    public static void main(String[] args) throws Exception {
        String collectionId;
        String pathToSettings;
        long timeSinceLastChecksumUpdate;
        if(args.length >= 2) {
            collectionId = args[0];
            pathToSettings = args[1];
            
            if(args.length >= 3) {
                timeSinceLastChecksumUpdate = Long.parseLong(args[2]);
            } else {
                timeSinceLastChecksumUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
            }
        } else {
            collectionId = DEFAULT_COLLECTION_ID;
            pathToSettings = DEFAULT_PATH_TO_SETTINGS;
            timeSinceLastChecksumUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
        }
        
        
        SettingsProvider settingsLoader = new SettingsProvider(
                new XMLFileSettingsLoader(pathToSettings));
        try {
            Settings settings = settingsLoader.getSettings(collectionId);
            SimpleIntegrityService integrityService = new SimpleIntegrityService(settings);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
