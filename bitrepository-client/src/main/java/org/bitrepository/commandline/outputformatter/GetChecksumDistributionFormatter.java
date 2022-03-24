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
package org.bitrepository.commandline.outputformatter;

import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.ChecksumResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to deliver GetChecksums client output in a format showing the distribution of the checksum between the pillars.
 * Output format 'style' is 'info' i.e.
 * Tab separated columns with the columns:
 * Count FileID Checksum Pillars
 * <p>
 * If all pillars agree, then the 'pillars' will just say 'All'.
 */
public class GetChecksumDistributionFormatter implements GetChecksumsOutputFormatter {

    private final OutputHandler outputHandler;
    final static String header = "Count: \tChecksum: \tPillars: \tFileID: ";

    public GetChecksumDistributionFormatter(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    @Override
    public void formatHeader() {
        outputHandler.resultHeader(header);
    }

    @Override
    public void formatResult(Collection<ChecksumResult> results) {
        for (ChecksumResult result : results) {
            if (result.isDirty()) {
                printInconsistency(result);
            } else {
                outputHandler.resultLine(result.getContributors().size() + " \t"
                        + result.getChecksum(result.getContributors().get(0)) + " \tAll \t" + result.getID());
            }
        }
    }

    /**
     * Prints the different checksums along with the pillar distribution amongst these checksums.
     *
     * @param result The checksum results to format the inconsistency distribution upon.
     */
    private void printInconsistency(ChecksumResult result) {
        for (Map.Entry<String, List<String>> checksumsDistribution : retrieveChecksumDistribution(result).entrySet()) {
            outputHandler.resultLine(checksumsDistribution.getValue().size() + " \t"
                    + checksumsDistribution.getKey() + " \t" + checksumsDistribution.getValue() + " \t" + result.getID());
        }
    }

    /**
     * Creates the map between a checksum and the list of pillars having the checksum.
     *
     * @param result The checksum results.
     * @return The map between checksum and pillars.
     */
    private Map<String, List<String>> retrieveChecksumDistribution(ChecksumResult result) {
        Map<String, List<String>> res = new HashMap<>();

        for (String pillarID : result.getContributors()) {
            String checksum = result.getChecksum(pillarID);
            List<String> pillarIDs;
            if (res.containsKey(checksum)) {
                pillarIDs = res.get(checksum);
            } else {
                pillarIDs = new ArrayList<>();
            }
            pillarIDs.add(pillarID);
            res.put(checksum, pillarIDs);
        }

        return res;
    }
}
