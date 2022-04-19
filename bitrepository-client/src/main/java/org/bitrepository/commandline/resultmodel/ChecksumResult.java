/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline.resultmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChecksumResult {
    private final String id;
    private final Map<String, String> pillarChecksumMap = new HashMap<>();
    private boolean conflict = false;

    public ChecksumResult(String id, String contributor, String checksum) {
        this.id = id;

        pillarChecksumMap.put(contributor, checksum);
    }

    /**
     * Adds a contributor to the list of contributors.
     *
     * @param contributor the ID of the contributor
     * @param checksum    the checksum that the contributor delivered
     */
    public void addContributor(String contributor, String checksum) {
        if (!conflict && !pillarChecksumMap.containsValue(checksum)) {
            conflict = true;
        }
        pillarChecksumMap.put(contributor, checksum);
    }

    /**
     * Is the result in 'conflict', i.e. is there checksum disagreement among the answered contributors.
     *
     * @return false if all contributors have agreed on the checksum.
     */
    public boolean inConflict() {
        return conflict;
    }

    /**
     * Get the list of contributors.
     *
     * @return the set of contributors which have delivered a checksum
     */
    public List<String> getContributors() {
        return new ArrayList<>(pillarChecksumMap.keySet());
    }

    /**
     * @param contributor The contributor to get the checksum for
     * @return the checksum from a given contributor
     */
    public String getChecksum(String contributor) {
        return pillarChecksumMap.get(contributor);
    }

    /**
     * Get the fileID of the file for which the checksum is for.
     *
     * @return String, the fileID
     */
    public String getID() {
        return id;
    }

    /**
     * Determine if we have enough answers to consider the result complete
     *
     * @param expectedNumberOfContributors the expected number of contributors.
     * @return true, if there's registered expectedNumberOfContributors of contributors.
     */
    public boolean isComplete(int expectedNumberOfContributors) {
        return (pillarChecksumMap.size() == expectedNumberOfContributors);
    }
}
