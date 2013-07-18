package org.bitrepository.integrityservice.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class BasicIntegrityReporter implements IntegrityReporter {

    private final String collectionID;
    /** Date of the report, use for identification and for placing it in time. */
    private final Date reportDate;
    private static final String DATE_FORMAT = "yyyyMMDD-HHmmss"; 
    private Long deletedFilesCount = 0L;
    //Treemaps ensures alphapetical sorting.
    private final Map<String, Long> missingFiles = new TreeMap<String, Long>();
    private final Map<String, Long> checksumIssues = new TreeMap<String, Long>();
    private final Map<String, Long> missingChecksums = new TreeMap<String, Long>();
    private final Map<String, Long> obsoleteChecksums = new TreeMap<String, Long>();
    private final IntegrityReportWriter writer;
    
    public BasicIntegrityReporter(String collectionID, File reportsDir) {
        this.collectionID = collectionID;
        reportDate = new Date();
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        File collectionReportsDir = new File(reportsDir, collectionID);
        if(!collectionReportsDir.isDirectory()) {
            collectionReportsDir.mkdir();
        }
        File reportDir = new File(collectionReportsDir, formatter.format(reportDate));
        reportDir.mkdir();
        writer = new IntegrityReportWriter(reportDir);
    }
    
    @Override
    public boolean hasIntegrityIssues() {
        return !(
                missingFiles.isEmpty() &&
                checksumIssues.isEmpty() &&
                missingChecksums.isEmpty() &&
                obsoleteChecksums.isEmpty()
        );
    }
    
    @Override
    public boolean hasReport() {
        String reportPath = writer.getReportFilePath();
        File report = new File(reportPath);
        if(report.exists()) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public File getReport() throws FileNotFoundException {
        String reportPath = writer.getReportFilePath();
        File report = new File(reportPath);
        if(report.exists()) {
            return report;
        } else {
            throw new FileNotFoundException("The report file cant be found on path: " + reportPath);
        }
    }
    
    @Override 
    public void generateReport() throws IOException {
        writer.writeReport();
        writer.close();
    }
    
    private String generateSummary() {
        StringBuilder report = new StringBuilder();
        if(deletedFilesCount != 0L) {
            report.append("Detected " + deletedFilesCount + " files as removed from the collection.");
        }
        for(String pillar : missingFiles.keySet()) {
            if(missingFiles.get(pillar) != 0) {
                report.append("\n" + pillar + " is missing " + missingFiles.get(pillar) + " file");
                if (missingFiles.get(pillar) > 1) report.append("s");
                report.append(".");
            }
        }
        
        for(String pillar : checksumIssues.keySet()) {
            if(checksumIssues.get(pillar) != 0) {
                report.append("\n" + pillar + " has " + checksumIssues.get(pillar) + " potentially corrupt file");
                if (checksumIssues.get(pillar) > 1) report.append("s");
                report.append(".");
            }
        }
        
        for(String pillar : missingChecksums.keySet()) {
            if(missingChecksums.get(pillar) != 0) {
                report.append("\n" + pillar + " is missing " + missingChecksums.get(pillar) + " checksum");
                if (missingChecksums.get(pillar) > 1) report.append("s");
                report.append(".");
            }
        }
        
        for(String pillar : obsoleteChecksums.keySet()) {
            if(obsoleteChecksums.get(pillar) != 0) {
                report.append("\n" + pillar + " has " + obsoleteChecksums.get(pillar) + " obsolete checksum");
                if (obsoleteChecksums.get(pillar) > 1) report.append("s");
                report.append(".");
            }
        }
        if(report.toString().isEmpty()) {
            return "No integrity issues found";
        } else {
            return report.toString();            
        }
    }

    @Override
    public String generateSummaryOfReport() {
        if(!hasIntegrityIssues()) {
            return "No integrity issues found";
        } else {
            return "The following integrity issues where found:" +   generateSummary();
        }
    }

    @Override
    public String getCollectionID() {
        return collectionID;
    }

    @Override
    public void reportDeletedFile(String fileID) throws IOException {
        deletedFilesCount++;
        writer.writeDeletedFile(fileID);
    }

    @Override
    public void reportMissingFile(String fileID, String pillarID) throws IOException {
        if(missingFiles.containsKey(pillarID)) {
            missingFiles.put(pillarID, (missingFiles.get(pillarID) + 1));
        } else {
            missingFiles.put(pillarID, 1L);
        }
        writer.writeMissingFile(pillarID, fileID);
    }

    @Override
    public void reportChecksumIssue(String fileID, String pillarID) throws IOException {
        if(checksumIssues.containsKey(pillarID)) {
            checksumIssues.put(pillarID, (checksumIssues.get(pillarID) + 1));
        } else {
            checksumIssues.put(pillarID, 1L);
        }
        writer.writeChecksumIssue(pillarID, fileID);
    }

    @Override
    public void reportMissingChecksum(String fileID, String pillarID) throws IOException {
        if(missingChecksums.containsKey(pillarID)) {
            missingChecksums.put(pillarID, (missingChecksums.get(pillarID) + 1));
        } else {
            missingChecksums.put(pillarID, 1L);
        }
        writer.writeMissingChecksum(pillarID, fileID);
    }

    @Override
    public void reportObsoleteChecksum(String fileID, String pillarID) throws IOException {
        if(obsoleteChecksums.containsKey(pillarID)) {
            obsoleteChecksums.put(pillarID, (obsoleteChecksums.get(pillarID) + 1));
        } else {
            obsoleteChecksums.put(pillarID, 1L);
        }
        writer.writeObsoleteChecksum(pillarID, fileID);
    }
}
