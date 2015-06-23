package org.bitrepository.integrityservice.reports;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle access to integrity reports 
 */
public class IntegrityReportProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, IntegrityReportReader> reports = new HashMap<>();
    
    private final File reportsDir;
    
    public IntegrityReportProvider(File reportsDir) {
        this.reportsDir = reportsDir;
        
    }
    
    /**
     * Get the latest access to the latest Integrity report using an IntegrityReportReader
     * @param collectionID The collection to retrieve the latest report for 
     * @return IntegrityReportReader with the latest integrity report
     * @throws FileNotFoundException if no report could be found
     */
    public synchronized IntegrityReportReader getLatestIntegrityReportReader(String collectionID) throws FileNotFoundException {
        IntegrityReportReader reader = reports.get(collectionID);
        if(reader == null) {
            log.info("Trying to lookup the latest report on disk for collection {}", collectionID);
            File latestReportDir = getLatestReportFromDisk(collectionID);
            if(latestReportDir != null) {
                reader = new IntegrityReportReader(latestReportDir);
                reports.put(collectionID, reader);
            } else {
                throw new FileNotFoundException("Could not find latest integrity report");    
            }
        }
        
        return reader;
    }
    
    /**
     * Register the latest integrity report for a given collection
     * @param collectionID The collection to register the report for
     * @param reportDir The directory containing the integrity report and report parts 
     */
    public synchronized void setLatestReport(String collectionID, File reportDir) {
        reports.put(collectionID, new IntegrityReportReader(reportDir));
    }
    
    private File getLatestReportFromDisk(String collectionID) {
        File collectionReports = new File(reportsDir, collectionID);
        log.info("Looking for latest reportdir in {}", collectionReports);
        File[] reports = collectionReports.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        long lastModification = Long.MIN_VALUE;
        File latestReport = null;
        for(File reportDir : reports) {
            if(reportDir.lastModified() > lastModification) {
                File reportFile = new File(reportDir, IntegrityReportConstants.REPORT_FILE); 
                if(reportFile.exists()) {
                    latestReport = reportDir;
                    lastModification = latestReport.lastModified();    
                } else {
                    log.debug("Candidate report dir {} had no report file", reportDir);   
                }
            }
        }
                
        return latestReport;
    }
}
