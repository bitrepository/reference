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
     * <li> The time for outdated checksums (in millis)</li>
     * <li> The interval for retrieving all the file ids (in millis)</li>
     * </ol>
     */
    public static void main(String[] args) {
        try {
            String collectionId = DEFAULT_COLLECTION_ID;
            String pathToSettings = DEFAULT_PATH_TO_SETTINGS;
            long timeSinceLastChecksumUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
            long timeSinceLastFileIDsUpdate = DEFAULT_MAX_TIME_SINCE_UPDATE;
            
            // first argument the collectionId, and second the path. 
            if(args.length >= 1) {
                collectionId = args[0];
                if(args.length >= 2) {
                    pathToSettings = args[1];
                    
                    // third argument is Checksum update and fourth it the fileids update interval.
                    if(args.length >= 3) {
                        timeSinceLastChecksumUpdate = Long.parseLong(args[2]);
                        if(args.length >= 4) {
                            timeSinceLastFileIDsUpdate = Long.parseLong(args[3]);
                        }
                    }
                }
            }
            
            SettingsProvider settingsLoader = new SettingsProvider(
                    new XMLFileSettingsLoader(pathToSettings));
            Settings settings = settingsLoader.getSettings(collectionId);
            SimpleIntegrityService integrityService = new SimpleIntegrityService(settings);
            
            integrityService.startChecksumIntegrityCheck(timeSinceLastChecksumUpdate, 
                    settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval());
            for(String pillarId : settings.getCollectionSettings().getClientSettings().getPillarIDs()) {
                integrityService.startAllFileIDsIntegrityCheckFromPillar(pillarId, timeSinceLastFileIDsUpdate);
            }
        } catch (Exception e) {
            System.out.println("Usage (arguments):");
            System.out.println("1. The path to the directory containing the settings [DEFAULT: settings/xml]");
            System.out.println("2. The collection ID to load the settings for [DEFAULT: bitrepository-devel], "
                    + "must be a subdirectory to the settings directory above.");
            System.out.println("3. The time for outdated checksums (in millis) [DEFAULT: 604800000 (one week)]");
            System.out.println("4. The interval for retrieving all the file ids (in millis) [DEFAULT: "
                    + "604800000 (one week)]");
            e.printStackTrace();
            System.exit(0);
        }
    }
}
