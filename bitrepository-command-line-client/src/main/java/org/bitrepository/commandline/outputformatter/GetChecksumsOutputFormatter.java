package org.bitrepository.commandline.outputformatter;

import java.util.Collection;

import org.bitrepository.commandline.resultmodel.ChecksumResult;

/**
 * Classes for formatting and outputting results from the GetFileIDs client 
 */
public interface GetChecksumsOutputFormatter {

    /**
     * Format and output the result header 
     */
    void formatHeader();
    
    /**
     * Format and output a result line 
     */
    void formatResult(Collection<ChecksumResult> results);
}
