/*
 * #%L
 * Bitrepository Common
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
import org.bitrepository.settings.collectionsettings.MessageBusConfiguration;
import org.bitrepository.settings.collectionsettings.Permissions;
import org.bitrepository.settings.referencesettings.ReferenceSettings;

/**
 * Wraps the {@link ReferenceSettings} {@link CollectionSettings}. Use a {@link SettingsProvider} to access settings
 */
public class Settings {
    protected ReferenceSettings referenceSettings;
    protected CollectionSettings collectionSettings;
    
    protected Settings(CollectionSettings collectionSettings, ReferenceSettings referenceSettings) {
        this.referenceSettings = referenceSettings;
        this.collectionSettings = collectionSettings;
    }

    /**
     * Wraps the {@link CollectionSettings#getCollectionID()} method.
     */
    public String getCollectionID() {
        return getCollectionSettings().getCollectionID();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.collectionsettings.ProtocolSettings#getAlarmDestination()} method.
     */
    public String getAlarmDestination() {
        return getCollectionSettings().getProtocolSettings().getAlarmDestination();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.collectionsettings.ProtocolSettings#getCollectionDestination()} method.
     */
    public String getCollectionDestination() {
        return getCollectionSettings().getProtocolSettings().getCollectionDestination();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.collectionsettings.ProtocolSettings#getMessageBusConfiguration()} method.
     */
    public MessageBusConfiguration getMessageBusConfiguration() {
        return getCollectionSettings().getProtocolSettings().getMessageBusConfiguration();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.collectionsettings.ProtocolSettings#getPermissions()} method.
     */
    public Permissions getPermissions() {
        return getCollectionSettings().getProtocolSettings().getPermissions();
    }
      
    /**
     * @return The settings specific to the reference code for a collection.
     */
    public ReferenceSettings getReferenceSettings() {
        return referenceSettings;
    }
    
    /**
     * @return The standard settings for a collection.
     */
    public CollectionSettings getCollectionSettings() {
        return collectionSettings;
    }
}
