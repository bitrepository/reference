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

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * <p>Used to limit a request for information elements from components in a collection.
 * <p>The ContributorQuery functionality can be used to request results in chunks. This is useful if the number of
 * potential results might be very large, which might cause OutOfMemory problems. A full result can be listed though
 * paging through the chunks by: <ol>
 * <li>Making a request with <code>maxNumberOfResults</code> set to the </li>
 * </ol>
 */
public class ContributorQuery {
    private final String componentID;
    private final Instant minTimestamp;
    private final Instant maxTimestamp;
    private final Integer maxNumberOfResults;

    /**
     * @param componentID        If set, only results from the indicated component is requested.
     * @param minTimestamp       If set, only elements with timestamp later than or equal to <code>minTimestamp</code> are
     *                           requested.
     * @param maxTimestamp       If set, only elements with timestamp earlier than or equal to <code>maxTimestamp</code> are
     *                           requested.
     * @param maxNumberOfResults If set will limit the number of results returned. If the result set is limited, only
     *                           the oldest timestamps are returned
     * @deprecated Use {@link #ContributorQuery(String, Instant, Instant, Integer)} instead
     */
    @Deprecated(forRemoval = true)
    public ContributorQuery(String componentID, Date minTimestamp, Date maxTimestamp, Integer maxNumberOfResults) {
        this(componentID,
                minTimestamp != null ? minTimestamp.toInstant() : null,
                maxTimestamp != null ? maxTimestamp.toInstant() : null,
                maxNumberOfResults);
    }

    /**
     * @param componentID        If set, only results from the indicated component is requested.
     * @param minTimestamp       If set, only elements with timestamp later than or equal to <code>minTimestamp</code> are
     *                           requested.
     * @param maxTimestamp       If set, only elements with timestamp earlier than or equal to <code>maxTimestamp</code> are
     *                           requested.
     * @param maxNumberOfResults If set will limit the number of results returned. If the result set is limited, only
     *                           the oldest timestamps are returned
     */
    public ContributorQuery(String componentID, Instant minTimestamp, Instant maxTimestamp, Integer maxNumberOfResults) {
        if (minTimestamp != null && maxTimestamp != null && minTimestamp.isAfter(maxTimestamp)) {
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

    /**
     * @deprecated Use {@link #getMinTimestampInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getMinTimestamp() {
        return minTimestamp != null ? Date.from(minTimestamp) : null;
    }

    public Instant getMinTimestampInstant() {
        return minTimestamp;
    }

    /**
     * @deprecated Use {@link #getMaxTimestampInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getMaxTimestamp() {
        return maxTimestamp != null ? Date.from(maxTimestamp) : null;
    }

    public Instant getMaxTimestampInstant() {
        return maxTimestamp;
    }

    public Integer getMaxNumberOfResults() {
        return maxNumberOfResults;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "componentID='" + componentID +
                "', minTimestamp=" + minTimestamp +
                ", maxTimestamp=" + maxTimestamp +
                ", maxNumberOfResults=" + maxNumberOfResults +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentID, maxNumberOfResults, maxTimestamp, minTimestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ContributorQuery other = (ContributorQuery) obj;
        return Objects.equals(componentID, other.componentID) &&
                Objects.equals(maxNumberOfResults, other.maxNumberOfResults) &&
                Objects.equals(maxTimestamp, other.maxTimestamp) &&
                Objects.equals(minTimestamp, other.minTimestamp);
    }
}
