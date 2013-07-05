package org.bitrepository.integrityservice.checking.reports;

import java.util.HashMap;
import java.util.Map;

public class BasicIntegrityReporter implements IntegrityReporter {

    private final String collectionID;
    private long deletedFilesCount = 0l;
    private final Map<String, Long> missingFiles = new HashMap<String, Long>();
    private final Map<String, Long> checksumIssues = new HashMap<String, Long>();
    private final Map<String, Long> missingChecksums = new HashMap<String, Long>();
    private final Map<String, Long> obsoleteChecksums = new HashMap<String, Long>();
    
    public BasicIntegrityReporter(String collectionID) {
        this.collectionID = collectionID;
    }
    
    @Override
    public boolean hasIntegrityIssues() {
        boolean hasIssues = false;
        for(Long count : missingFiles.values()) {
            if(count != 0L) {
                hasIssues = true;
            }
        }
        
        for(Long count : checksumIssues.values()) {
            if(count != 0L) {
                hasIssues = true;
            }
        }
        
        for(Long count : missingChecksums.values()) {
            if(count != 0L) {
                hasIssues = true;
            }
        }
        
        for(Long count : obsoleteChecksums.values()) {
            if(count != 0L) {
                hasIssues = true;
            }
        }
        
        return hasIssues;
    }

    @Override
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        if(deletedFilesCount > 0l) {
            report.append("Reported " + deletedFilesCount + " files removed from the collection.\n");
        }
        for(String pillar : missingFiles.keySet()) {
            if(missingFiles.get(pillar) != 0) {
                report.append("Pillar " + pillar + " is missing " + missingFiles.get(pillar) + " file(s).\n");
            }
        }
        
        for(String pillar : checksumIssues.keySet()) {
            if(checksumIssues.get(pillar) != 0) {
                report.append("Pillar " + pillar + " has " + checksumIssues.get(pillar) + " checksum issue(s).\n");
            }
        }
        
        for(String pillar : missingChecksums.keySet()) {
            if(missingChecksums.get(pillar) != 0) {
                report.append("Pillar " + pillar + " is missing " + missingChecksums.get(pillar) + " checksum(s).\n");
            }
        }
        
        for(String pillar : obsoleteChecksums.keySet()) {
            if(obsoleteChecksums.get(pillar) != 0) {
                report.append("Pillar " + pillar + " has " + obsoleteChecksums.get(pillar) + " obsolete checksum(s).\n");
            }
        }
        
        return report.toString();
    }

    @Override
    public String generateSummaryOfReport() {
        // TODO Figure out what to write in the summary
        if(!hasIntegrityIssues()) {
            return "No integrity issues found";
        } else {
            return generateReport();
        }
    }

    @Override
    public String getCollectionID() {
        return collectionID;
    }

    @Override
    public void reportDeletedFile(String fileID) {
        deletedFilesCount++;
    }

    @Override
    public void reportMissingFile(String fileID, String pillarID) {
        if(missingFiles.containsKey(pillarID)) {
            missingFiles.put(pillarID, (missingFiles.get(pillarID) + 1));
        } else {
            missingFiles.put(pillarID, 1L);
        }
    }

    @Override
    public void reportChecksumIssue(String fileID, String pillarID) {
        if(checksumIssues.containsKey(pillarID)) {
            checksumIssues.put(pillarID, (checksumIssues.get(pillarID) + 1));
        } else {
            checksumIssues.put(pillarID, 1L);
        }
    }

    @Override
    public void reportMissingChecksum(String fileID, String pillarID) {
        if(missingChecksums.containsKey(pillarID)) {
            missingChecksums.put(pillarID, (missingChecksums.get(pillarID) + 1));
        } else {
            missingChecksums.put(pillarID, 1L);
        }
    }

    @Override
    public void reportObsoleteChecksum(String fileID, String pillarID) {
        if(obsoleteChecksums.containsKey(pillarID)) {
            obsoleteChecksums.put(pillarID, (obsoleteChecksums.get(pillarID) + 1));
        } else {
            obsoleteChecksums.put(pillarID, 1L);
        }
    }

    @Override
    public long numberOfDeletedFiles() {
        return deletedFilesCount;
    }

    @Override
    public long numberOfMissingFiles() {
        return missingFiles.size();
    }

    @Override
    public long numberOfInconsistentChecksums() {
        return checksumIssues.size();
    }

    @Override
    public long numberOfMissingChecksums() {
        return missingChecksums.size();
    }

    @Override
    public long numberOfObsoleteChecksums() {
        return obsoleteChecksums.size();
    }

}
