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

import org.bitrepository.common.utils.XmlUtils;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.bitrepository.settings.repositorysettings.Permission;
import org.bitrepository.settings.repositorysettings.RepositorySettings;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;

/**
 * Contains the general configuration to be used by reference code components. Provides access to both
 * {@link ReferenceSettings} and {@link org.bitrepository.settings.repositorysettings.RepositorySettings}.
 * Use a {@link SettingsProvider} to access settings create <code>Settings</code> objects.
 */
public class Settings {
    public static final BigInteger MILLIS_PER_SECOND = BigInteger.valueOf(Duration.ofSeconds(1).toMillis());

    protected final String componentID;
    protected final String receiverDestinationID;
    protected final ReferenceSettings referenceSettings;
    protected final RepositorySettings repositorySettings;

    protected Settings(String componentID, String receiverDestinationID, RepositorySettings repositorySettings,
                       ReferenceSettings referenceSettings) {
        this.componentID = componentID;
        this.receiverDestinationID = receiverDestinationID;

        this.referenceSettings = referenceSettings;
        this.repositorySettings = repositorySettings;
    }

    /**
     * @return the first Collections ID as a list.
     */
    public List<Collection> getCollections() {
        return getRepositorySettings().getCollections().getCollection();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getAlarmDestination()} method.
     *
     * @return the alarm destination
     */
    public String getAlarmDestination() {
        return getRepositorySettings().getProtocolSettings().getAlarmDestination();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getCollectionDestination()} method.
     *
     * @return the collection destination
     */
    public String getCollectionDestination() {
        return getRepositorySettings().getProtocolSettings().getCollectionDestination();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getMessageBusConfiguration()} method.
     *
     * @return the message bus configuration
     */
    public MessageBusConfiguration getMessageBusConfiguration() {
        return getRepositorySettings().getProtocolSettings().getMessageBusConfiguration();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.PermissionSet#getPermission()}} method.
     *
     * @return the list of Permissions
     */
    public List<Permission> getPermissions() {
        return getRepositorySettings().getPermissionSet().getPermission();
    }

    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ClientSettings#getIdentificationTimeout()}
     * and {@link org.bitrepository.settings.repositorysettings.ClientSettings#getIdentificationTimeoutDuration()}
     * methods, preferring to use the latter.
     *
     * @return the timeout
     * @see org.bitrepository.settings.repositorysettings.ClientSettings#getIdentificationTimeoutDuration()
     */
    public Duration getIdentificationTimeout() {
        // TODO Once the old IdentificationTimeout settings in milliseconds is done away with
        //  the following code gets simpler
        javax.xml.datatype.Duration xmlDuration =
                getRepositorySettings().getClientSettings().getIdentificationTimeoutDuration();
        BigInteger identificationTimeoutMillis =
                getRepositorySettings().getClientSettings().getIdentificationTimeout();
        return getDurationFromXmlDurationOrMillis(xmlDuration, identificationTimeoutMillis);
    }

    public Duration getOperationTimeout() {
        javax.xml.datatype.Duration xmlDuration =
                getRepositorySettings().getClientSettings().getOperationTimeoutDuration();
        BigInteger operationTimeoutMillis =
                getRepositorySettings().getClientSettings().getOperationTimeout();
        return getDurationFromXmlDurationOrMillis(xmlDuration, operationTimeoutMillis);
    }

    static Duration getDurationFromXmlDurationOrMillis(
            javax.xml.datatype.Duration xmlDuration, BigInteger millis) {
        // Prefer the XML Duration
        if (xmlDuration != null) {
            XmlUtils.validateNonNegative(xmlDuration);
            return XmlUtils.xmlDurationToDuration(xmlDuration);
        }
        BigInteger[] secondsAndMillis = millis.divideAndRemainder(MILLIS_PER_SECOND);
        return Duration.ofSeconds(secondsAndMillis[0].longValueExact())
                .plusMillis(secondsAndMillis[1].intValueExact());
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
    public RepositorySettings getRepositorySettings() {
        return repositorySettings;
    }

    public String getComponentID() {
        return componentID;
    }

    public String getReceiverDestinationID() {
        return receiverDestinationID;
    }

    public String getContributorDestinationID() {
        return receiverDestinationID + "-contributor";
    }
}
