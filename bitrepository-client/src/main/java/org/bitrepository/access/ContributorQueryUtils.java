package org.bitrepository.access;/*
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ContributorQueryUtils {

    /**
     * Used to create a <code>AuditTrailQuery[]</code> array in case no array is defined.
     * @param contributorIDs The collection of contributor IDs to create ContributorQueries for
     * @return A <code>AuditTrailQuery[]</code> array requesting all audit trails from all the defined contributers.
     */
    public static ContributorQuery[] createFullContributorQuery(Collection<String> contributorIDs) {
        List<ContributorQuery> componentQueryList = new ArrayList<>(contributorIDs.size());
        for (String contributer : contributorIDs) {
            componentQueryList.add(new ContributorQuery(contributer, null, null, null));
        }
        return componentQueryList.toArray(new ContributorQuery[componentQueryList.size()]);
    }

    /**
     * Extracts the collection of ContributorIDs from the ContribytorQueries.
     * @param queries The contributor queries to extract nonPillarContributors from
     * @return The list of ContributorIDs in the supplied queries
     */
    public static Collection<String> getContributors(ContributorQuery[] queries) {
        Collection<String> contributors = new HashSet<>();
        for (ContributorQuery query: queries) {
            contributors.add(query.getComponentID());
        }
        return contributors;
    }
}
