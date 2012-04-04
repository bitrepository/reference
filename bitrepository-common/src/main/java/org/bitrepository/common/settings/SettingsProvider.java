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

import org.bitrepository.settings.collectionsettings.CollectionSettings;
import org.bitrepository.settings.referencesettings.ReferenceSettings;

/**
 * Used for accessing <code>Settings</code> objects. A <code>SettingsLoader</code> needs to be provides on 
 * instantiation for loading stored settings.
 */
public class SettingsProvider {
    /** The loader to use for acessing stored settings*/
    private final SettingsLoader settingsReader;
    /** The loaded settings */
    private Settings settings;
    
    /**
     * Creates a <code>SettingsProvider</code> which will use the provided <code>SettingsLoader</code> for loading the 
     * settings.
     * @param settingsReader Use for loading the settings.
     */
    public SettingsProvider(SettingsLoader settingsReader) {
        this.settingsReader = settingsReader;
    }
    
    /**
     * Loads the settings for the collection defined by the COLLECTIONID_PROPERTY system variable.
     * @return The settings 
     */
    public synchronized Settings getSettings() {
        if(settings == null) {
            loadSettings();
        }
        return settings;
    }
    
    /**
     * Will load the settings from disk if they haven't been loaded before
     * @param collectionID The collectionID to find settings for.
     * @return The settings for the indicated CollectionID
     */
    @Deprecated
    public synchronized Settings getSettings(String collectionID) {
        return getSettings();
    }
    
    /**
    * Will load the settings from disk to this providers model. Will overwrite any settings already in the provider.
    */
    public synchronized void loadSettings() {
    	CollectionSettings collectionSettings = settingsReader.loadSettings(CollectionSettings.class);
    	ReferenceSettings referenceSettings = settingsReader.loadSettings(ReferenceSettings.class);
        settings = new Settings(collectionSettings, referenceSettings);
    }
}
