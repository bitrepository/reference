/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.PillarType;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.RepositorySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.bitrepository.settings.referencesettings.PillarIntegrityDetails.PillarDetails;

public class BasicClient {
    private final Settings settings;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public BasicClient(Settings settings) {
        log.debug("---- Basic client instantiating ----");
        this.settings = settings;
        log.debug("---- Basic client instantiated ----");

    }

    public List<String> getCollectionIDs() {
        List<String> collections = new ArrayList<>();
        for (Collection collection : settings.getRepositorySettings().getCollections().getCollection()) {
            collections.add(collection.getID());
        }
        return collections;
    }

    public String getHostname(String pillarID) {
        log.debug("Fetching PillarHostnames");
        List<PillarDetails> details = settings.getReferenceSettings().getPillarIntegrityDetails().getPillarDetails();
        for (PillarDetails d: details) {
            if (d.getPillarID().equals(pillarID)) {
                return d.getPillarHostname();
            }
        }
        return null;
    }

    public PillarType getPillarType(String pillarID) {
        log.debug("Fetching PillarHostnames");
        List<PillarDetails> details = settings.getReferenceSettings().getPillarIntegrityDetails().getPillarDetails();
        for (PillarDetails d: details) {
            if (d.getPillarID().equals(pillarID)) {
                return d.getPillarType();
            }
        }
        return null;
    }

    public void shutdown() {
        // Currently, there's nothing to do here
    }

    public String getSettingsSummary() {
        StringBuilder sb = new StringBuilder();
        RepositorySettings repositorySettings = settings.getRepositorySettings();
        sb.append("Collections:<br>");
        for (Collection collection : settings.getCollections()) {
            sb.append("<i>ID:").append(collection.getID());
            sb.append("<i>Pillars:");
            for (String pillarID : collection.getPillarIDs().getPillarID()) {
                sb.append("&nbsp; ").append(pillarID);
            }
        }
        sb.append("</i><br>");
        sb.append("</i>");
        sb.append("Messagebus URL: <br> &nbsp;&nbsp;&nbsp; <i>");
        sb.append(repositorySettings.getProtocolSettings().getMessageBusConfiguration().getURL()).append("</i><br>");
        return sb.toString();
    }

    public Settings getSettings() {
        return settings;
    }
}
