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
import org.bitrepository.commandline.resultmodel.FileIDsResult;

import java.util.Collection;

/**
 * Class to format GetFileIDs client output.
 * Output format 'style' is 'info' i.e.
 * Tab separated columns with the columns:
 * Count Size FileID
 */
public class GetFileIDsInfoFormatter implements GetFileIDsOutputFormatter {

    private final OutputHandler outputHandler;
    final static String header = "Count: \tSize: \tFileID:";

    public GetFileIDsInfoFormatter(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    public void formatHeader() {
        outputHandler.resultHeader(header);
    }

    public void formatResult(Collection<FileIDsResult> results) {
        for (FileIDsResult result : results) {
            String filesize;
            if (result.getSize() == null) {
                filesize = "???";
            } else if (result.getSize().intValue() == -1) {
                filesize = "disagreed";
            } else {
                filesize = result.getSize().toString();
            }
            String line = result.getContributors().size() + " \t" + filesize + " \t" + result.getID();
            outputHandler.resultLine(line);
        }
    }
}
