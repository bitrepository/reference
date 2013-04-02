package org.bitrepository.commandline.outputformatter;

import java.util.Collection;

import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.ChecksumResult;

public class GetChecksumsInfoFormatter implements GetChecksumsOutputFormatter {

    private final OutputHandler outputHandler;
    final static String header = "Count: \tChecksum: \tFileID:";
    
    public GetChecksumsInfoFormatter(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }
    
    public void formatHeader() {
        outputHandler.resultHeader(header);
    }
    
    
    public void formatResult(Collection<ChecksumResult> results) {
        String firstContributor = null;
        for(ChecksumResult result : results) {
            if(firstContributor == null) {
                result.getContributors().get(0);
            }
            String checksum;
            if(result.isDirty()) {
                checksum = "disagreed";
            } else {
                checksum = result.getChecksum(firstContributor);
            }
            String line = result.getContributors().size() + " \t" + checksum + " \t" + result.getID();
            outputHandler.resultLine(line);
        }
    }
}
