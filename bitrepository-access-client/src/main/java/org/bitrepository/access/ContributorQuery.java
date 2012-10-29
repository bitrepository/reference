/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.access;

import java.util.Date;
import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * Used to limit a request for information elements from components in a collection.
 */
public class ContributorQuery {
    private final String componentID;
    private final Date minTimestamp;
    private final Date maxTimestamp;
    private final Integer maxNumberOfResults;

    /**
     * @param componentID If set, only results from the indicated component is requested.
     * @param fileIDs If set defines the fileID to retrieve results for.
     * @param minTimestamp If set, only elements with timestamp later than or equal to <code>minTimestamp</code> are
     *                     requested.
     * @param maxTimestamp If set, only elements with timestamp earlier than or equal to <code>maxTimestamp</code> are
     *                     requested.
     * @param maxNumberOfResults If set will limit the number of results returned. If the result set is limited, only
     * the lowest sequence numbers are returned
     */
    public ContributorQuery(String componentID, Date minTimestamp, Date maxTimestamp, Integer maxNumberOfResults) {
        if (minTimestamp != null && maxTimestamp != null && minTimestamp.after(maxTimestamp)) {
            throw new IllegalArgumentException(
                "minTimestamp=" + minTimestamp + " can not be later than " + "maxTimestamp=" + maxTimestamp);
        }
        this.componentID = componentID;
        this.maxNumberOfResults = maxNumberOfResults;
        this.minTimestamp = minTimestamp;
        this.maxTimestamp = maxTimestamp;
    }

    public String getComponentID() {
        return componentID;
    }

    public Date getMinTimestamp() {
        return minTimestamp;
    }

    public Date getMaxTimestamp() {
        return maxTimestamp;
    }

    public Integer getMaxNumberOfResults() {
        return maxNumberOfResults;
    }

    @Override
    public String toString() {
        return getClass() + "{" +
            "componentID='" + componentID + '\'' +
            ", minTimestamp=" + minTimestamp +
            ", maxTimestamp=" + maxTimestamp +
            ", maxNumberOfResults=" + maxNumberOfResults +
            '}';
    }
}
