/*
 * #%L
 * Bitrepository Integrity Service
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

package org.bitrepository.pillar.common;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.settings.repositorysettings.Collection;

/**
 *
 Provides functionality for creating data derived from the pillar settings
 */
public class SettingsHelper {
    /**
     * Calculates the list of pillars
     * @param pillarID
     * @param collections
     * @return
     */
    public static String[] getPillarCollections(String pillarID, List<Collection> collections) {
        List<String> relevantCollectionIDs = new ArrayList<String>();
        for (Collection collection: collections) {
            for (String pillar:collection.getPillarIDs().getPillarID()) {
                if(pillarID.equals(pillar)) {
                    relevantCollectionIDs.add(collection.getID());
                    break;
                }
            }
        }
        return relevantCollectionIDs.toArray(new String[relevantCollectionIDs.size()]);
    }
}
