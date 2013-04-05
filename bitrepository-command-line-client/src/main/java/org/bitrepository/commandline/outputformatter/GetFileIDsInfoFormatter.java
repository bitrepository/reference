package org.bitrepository.commandline.outputformatter;

import java.util.Collection;

import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.FileIDsResult;

/**
 * Class to format client output. 
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
        for(FileIDsResult result : results) {
            String filesize;
            if(result.getSize() == null) {
                filesize = "???";
            } else if(result.getSize().intValue() == -1) {
                filesize = "disagreed";
            } else {
                filesize = result.getSize().toString();
            }
            String line = result.getContributors().size() + " \t" + filesize + " \t" + result.getID();
            outputHandler.resultLine(line);
        }
    }
}
