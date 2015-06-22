package org.bitrepository.integrityservice.reports;

import java.io.File;

public class IntegrityReportProvider {

    private final File reportsDir;
    
    public IntegrityReportProvider(File reportsDir) {
        this.reportsDir = reportsDir;
        
    }
    
    public IntegrityReporter getLatestReport(String collectionID) {
        return null;
    }
}
