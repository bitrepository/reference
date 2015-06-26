package org.bitrepository.integrityservice.reports;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Class to support reading integrity reports and parts of them. 
 */
public class IntegrityReportReader {

    private final File reportDir;
    
    public IntegrityReportReader(File reportDir) {
        this.reportDir = reportDir;
    }
    
    /**
     * Retrieve the full report 
     * @throws FileNotFoundException if no report could be found
     */
    public File getFullReport() throws FileNotFoundException {
         File report = new File(reportDir, IntegrityReportConstants.REPORT_FILE);
         if(report.exists()) {
             return report;
         } else {
             throw new FileNotFoundException("Could not locate report file");
         }
    }
    
    /**
     * Retrieve a part of the report
     * @param part The part of the report that is requested
     * @param pillarID The pillar for which the part is for
     * @throws FileNotFoundException if no report part could be found
     */
    public File getReportPart(String part, String pillarID) throws FileNotFoundException {
        String reportFileName = part + "-" + pillarID;
        File reportPart = new File(reportDir, reportFileName);
        if(reportPart.exists()) {
            return reportPart;
        } else {
            throw new FileNotFoundException("Could not retrieve report part '" + part + "' for '" + pillarID + "'");
        }
    }
    
}
