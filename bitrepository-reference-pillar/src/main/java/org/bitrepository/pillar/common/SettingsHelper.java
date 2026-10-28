/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2026 The Royal Danish Library and The State Archives, Denmark
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

package org.bitrepository.pillar.common;

import org.bitrepository.settings.repositorysettings.Collection;

import java.util.List;

/**
 * Provides functionality for creating data derived from the pillar settings
 */
public final class SettingsHelper {
    private SettingsHelper() {}

    /**
     * Calculates the list of pillars
     *
     * @param pillarID    The ID of the pillar.
     * @param collections The collection for the whole repository.
     * @return all pillars in collections with the provided pillarID
     */
    public static List<String> getPillarCollections(String pillarID, List<Collection> collections) {
        return collections.stream()
                .filter(collection -> collection.getPillarIDs().getPillarID().contains(pillarID))
                .map(Collection::getID)
                .toList();
    }
}
