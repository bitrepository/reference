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
