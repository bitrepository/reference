package org.bitrepository.commandline.outputformatter;

import java.util.Collection;

import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.FileIDsResult;

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
        for(FileIDsResult result : results) {
            String line = result.getContributors().size() + " \t"+ result.getSize() + " \t" + result.getID();
            outputHandler.resultLine(line);
        }
    }
}
