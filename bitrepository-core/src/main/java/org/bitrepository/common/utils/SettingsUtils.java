/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.utils;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.IntegrityServiceSettings;
import org.bitrepository.settings.referencesettings.PillarIntegrityDetails;
import org.bitrepository.settings.referencesettings.PillarSettings;
import org.bitrepository.settings.referencesettings.PillarType;
import org.bitrepository.settings.repositorysettings.Collection;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility method for handling the settings.
 */
public class SettingsUtils {
    public final static Integer DEFAULT_MAX_CLIENT_PAGE_SIZE = 10000;
    private static Settings settings;

    /**
     * @param injectedSettings The settings to use.
     */
    public static void initialize(Settings injectedSettings) {
        settings = injectedSettings;
    }

    /**
     * Finds the collections, which the given pillar is part of.
     *
     * @param pillarID The id of the pillar.
     * @return The list of collection ids, which this pillar is part of.
     */
    public static List<String> getCollectionIDsForPillar(String pillarID) {
        List<String> res = new ArrayList<>();
        for (Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            if (c.getPillarIDs().getPillarID().contains(pillarID)) {
                res.add(c.getID());
            }
        }

        return res;
    }

    /**
     * Finds the complete list of collections in the repository.
     *
     * @return all collections in the repository
     */
    public static List<String> getAllCollectionsIDs() {
        List<String> res = new ArrayList<>();
        for (Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            res.add(c.getID());
        }
        return res;
    }

    /**
     * Gets the name of a given collection. If no name is given in the settings,
     * the ID of the collection is returned
     *
     * @param collectionID the collection id
     * @return The name of the collection
     */
    public static String getCollectionName(String collectionID) {
        String name = null;

        for (Collection collection : settings.getRepositorySettings().getCollections().getCollection()) {
            if (collection.getID().equals(collectionID)) {
                if (collection.isSetName()) {
                    name = collection.getName();
                } else {
                    name = collection.getID();
                }
                break;
            }
        }

        return name;
    }

    /**
     * Get the human-readable pillar name for the given pillarID from the ReferenceSettings.
     *
     * @param pillarID The pillarID for which the pillar name is wanted.
     * @return Returns the pillar name for the given pillar ID.
     */
    public static String getPillarName(String pillarID) {
        PillarIntegrityDetails details = settings.getReferenceSettings().getIntegrityServiceSettings().getPillarIntegrityDetails();
        if (details != null) {
            for (PillarIntegrityDetails.PillarDetails d : details.getPillarDetails()) {
                if (d.getPillarID().equals(pillarID)) {
                    return d.getPillarName();
                }
            }
        }
        return null;
    }

    public static IntegrityServiceSettings getIntegrityServiceSettings() {
        return settings.getReferenceSettings().getIntegrityServiceSettings();
    }

    /**
     * Get the configured max age for checksums for the given pillarID from the ReferenceSettings.
     *
     * @param pillarID The pillarID for which the max checksum age is wanted.
     * @return human-readable maximum age for checksums for the given pillar ID.
     */
    public static String getMaxAgeForChecksums(String pillarID) {
        PillarSettings pillarSettings = settings.getReferenceSettings().getPillarSettings();
        if (pillarSettings == null) {
            return "Not set (no pillar settings)";
        }
        if (! pillarSettings.getPillarID().equals(pillarID)) {
            return "Unknown";
        }
        BigInteger maxAge = pillarSettings.getMaxAgeForChecksums();
        try {
            return TimeUtils.millisecondsToHuman(maxAge.longValueExact());
        }
        catch (ArithmeticException ae) {
            return String.format(Locale.getDefault(Locale.Category.FORMAT), "Extremely long; %d ms", maxAge);
        }
    }

    /**
     * Get the {@link PillarType} for the given Pillar ID.
     *
     * @param pillarID The pillar ID for which the {@link PillarType} is wanted.
     * @return Returns the {@link PillarType} for the given pillar ID.
     */
    public static PillarType getPillarType(String pillarID) {
        PillarIntegrityDetails details = settings.getReferenceSettings().getIntegrityServiceSettings().getPillarIntegrityDetails();
        if (details != null) {
            for (PillarIntegrityDetails.PillarDetails d : details.getPillarDetails()) {
                if (d.getPillarID().equals(pillarID)) {
                    return d.getPillarType();
                }
            }
        }
        return null;
    }

    /**
     * Get the name of the repository
     *
     * @return The name of the repository
     */
    public static String getRepositoryName() {
        return settings.getRepositorySettings().getName();
    }

    /**
     * Retrieves all the different pillar ids defined across all collections (without duplicates).
     *
     * @return The list of pillar ids.
     */
    public static List<String> getAllPillarIDs() {
        List<String> res = new ArrayList<>();
        for (Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            for (String pillarID : c.getPillarIDs().getPillarID()) {
                if (!res.contains(pillarID)) {
                    res.add(pillarID);
                }
            }
        }
        return res;
    }

    public static List<String> getPillarIDsForCollection(String collectionID) {
        List<String> res = new ArrayList<>();
        for (Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            if (c.getID().equals(collectionID)) {
                res.addAll(c.getPillarIDs().getPillarID());
            }
        }
        return res;
    }

    /**
     * Get the maximum page size for clients.
     * If none have been set in the settings, use the default of 10000.
     *
     * @return {@link Integer} the maximum number of results per page.
     */
    public static Integer getMaxClientPageSize() {
        BigInteger maxClientSizeSettings = settings.getReferenceSettings().getClientSettings().getMaxPageSize();
        return maxClientSizeSettings != null ? maxClientSizeSettings.intValue() : DEFAULT_MAX_CLIENT_PAGE_SIZE;
    }

    /**
     * Retrieves the contributors for audit trail for a specific collection.
     *
     * @param collectionID The id of the collection.
     * @return The list of ids for the contributors of audit trails for the collection
     */
    public static Set<String> getAuditContributorsForCollection(String collectionID) {
        Set<String> contributors = new HashSet<>();
        contributors.addAll(settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getPillarIDsForCollection(collectionID));
        return contributors;
    }

    /**
     * Retrieves the contributors for status.
     *
     * @return The list of ids for the status contributors.
     */
    public static Set<String> getStatusContributorsForCollection() {
        Set<String> contributors = new HashSet<>();
        contributors.addAll(settings.getRepositorySettings().getGetStatusSettings().getNonPillarContributorIDs());
        contributors.addAll(SettingsUtils.getAllPillarIDs());
        return contributors;
    }

}
