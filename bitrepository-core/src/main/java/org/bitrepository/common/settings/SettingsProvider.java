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
package org.bitrepository.common.settings;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.messagebus.destination.DestinationHelper;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.RepositorySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for accessing <code>Settings</code> objects. A <code>SettingsLoader</code> needs to be provides on 
 * instantiation for loading stored settings.
 */
public class SettingsProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The loader to use for accessing stored settings*/
    private final SettingsLoader settingsReader;
    /** The loaded settings */
    private Settings settings;
    private String componentID;

    /**
     * Creates a <code>SettingsProvider</code> which will use the provided <code>SettingsLoader</code> for loading the 
     * settings.
     * @param settingsReader Use for loading the settings.
     * @param componentID The componentID to use for these settings
     */
    public SettingsProvider(SettingsLoader settingsReader, String componentID) {
        this.settingsReader = settingsReader;
        this.componentID = componentID;
    }

    /**
     * Gets the settings, if no settings has been loaded into memory, will load the settings from disk
     * @return The settings 
     */
    public synchronized Settings getSettings() {
        if(settings == null) {
            reloadSettings();
            SettingsUtils.initialize(settings);
        }
        return settings;
    }

    /**
     * Will load the settings from disk. Will overwrite any settings already in the provider.
     */
    public synchronized void reloadSettings() {
        try {
            RepositorySettings repositorySettings = settingsReader.loadSettings(RepositorySettings.class);
            ReferenceSettings referenceSettings = settingsReader.loadSettings(ReferenceSettings.class);

            String componentID = getComponentID(referenceSettings);
            String receiverDestination = getReceiverDestination(referenceSettings, repositorySettings);
            
            settings = new Settings(componentID, receiverDestination, repositorySettings, referenceSettings);
        } catch (RuntimeException re) {
            // We need to ensure this is log, as the method caller may not have implemented a fault barrier and this
            // exception is properly fatal.
            log.error("Failed to load settings", re);
            throw re;
        }
    }

    /**
     * Provides extension point for subclass componentID generation.
     * @param referenceSettings the reference settings
     * @return generate ComponentID
     */
    protected String getComponentID(ReferenceSettings referenceSettings) {
        return componentID;
    }
    
    /**
     * Provides extension point for subclass receiver destination generation.
     * @param referenceSettings the reference settings
     * @param repositorySettings the repository settings
     * @return generate receiver destination
     */
    protected String getReceiverDestination(ReferenceSettings referenceSettings, RepositorySettings repositorySettings) {
        String receiverDestinationIDFactoryClass = null;
        if (referenceSettings.getGeneralSettings() != null) {
            receiverDestinationIDFactoryClass =
                    referenceSettings.getGeneralSettings().getReceiverDestinationIDFactoryClass();
        }
        
        DestinationHelper dh = new DestinationHelper(
                getComponentID(referenceSettings),
                receiverDestinationIDFactoryClass,
                repositorySettings.getProtocolSettings().getCollectionDestination());
        return dh.getReceiverDestinationID();
    }
}
