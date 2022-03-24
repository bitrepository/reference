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

import java.util.Collection;

/**
 * Class to format GetChecksums client output.
 * Output format 'style' is 'info' i.e.
 * Tab separated columns with the columns:
 * Count Checksum FileID
 */
public class GetChecksumsInfoFormatter implements GetChecksumsOutputFormatter {

    private final OutputHandler outputHandler;
    final static String header = "Checksum: \tCount: \tFileID:";

    public GetChecksumsInfoFormatter(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    @Override
    public void formatHeader() {
        outputHandler.resultHeader(header);
    }

    @Override
    public void formatResult(Collection<ChecksumResult> results) {
        String firstContributor;
        String checksum;
        for (ChecksumResult result : results) {
            firstContributor = result.getContributors().get(0);
            if (result.isDirty()) {
                checksum = "disagreed";
            } else {
                checksum = result.getChecksum(firstContributor);
            }
            String line = checksum + " \t" + result.getContributors().size() + " \t" + result.getID();
            outputHandler.resultLine(line);
        }
    }
}
