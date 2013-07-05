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
 * Class to format GetChecksums client output. 
 * Output format 'style' is 'info' i.e.
 * Tab separated columns with the columns: 
 * Count Checksum FileID 
 */
public class GetChecksumsDisagreementInfoFormatter implements GetChecksumsOutputFormatter {

    private final OutputHandler outputHandler;
    final static String header = "Count: \tFileID: \tChecksum: \t \t \tPillars:";
    
    public GetChecksumsDisagreementInfoFormatter(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }
    
    @Override
    public void formatHeader() {
        outputHandler.resultHeader(header);
    }
    
    @Override
    public void formatResult(Collection<ChecksumResult> results) {
        for(ChecksumResult result : results) {
            if(result.isDirty()) {
                printDisagreement(result);
            } else {
                outputHandler.resultLine(result.getContributors().size() + " \t" + result.getID() + " \t" 
                        + result.getChecksum(result.getContributors().get(0)) + " \tAll");
            }
        }
    }
    
    /**
     * Prints the different checksums
     * @param result
     */
    private void printDisagreement(ChecksumResult result) {
        for(Map.Entry<String, List<String>> checksumsDistribution : retrieveChecksumDistribution(result).entrySet()) {
            outputHandler.resultLine(result.getContributors().size() + " \t" + result.getID() + " \t" 
                    + checksumsDistribution.getKey() + " \t" + checksumsDistribution.getValue());                    
        }
    }
    
    private Map<String, List<String>> retrieveChecksumDistribution(ChecksumResult result) {
        Map<String, List<String>> res = new HashMap<String, List<String>>();
        
        for(String pillarId : result.getContributors()){
            String checksum = result.getChecksum(pillarId);
            List<String> pillarIds;
            if(res.containsKey(checksum)) {
                pillarIds = res.get(checksum);
            } else {
                pillarIds = new ArrayList<String>();
            }
            pillarIds.add(pillarId);
            res.put(checksum, pillarIds);
        }
        
        return res;
    }
}
