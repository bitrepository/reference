package org.bitrepository.integrityclient.checking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * Container for the results of an integrity check.
 */
public class IntegrityReport {
    /** Whether any errors was found during the check. This is set to 'true', and only changed when actual errors 
     * are reported. */
    private boolean valid = true;
    /** The FileIDs for the report.*/
    private FileIDs fileIDs;
    /** Any bad results for checksum validation. Map between the fileId and results of the checksum check, whereas the
     * results of the checksum check is a map between each checksum value and their count.*/
    private Map<String, Map<String, Integer>> checksumResults = new HashMap<String, Map<String, Integer>>();
    /** The files which are missing.*/
    private Map<String, List<String>> missingFileIDs = new HashMap<String, List<String>>();
    
    /**
     * Constructor.
     * @param fileIDs The FileIDs for this report.
     */
    public IntegrityReport(FileIDs fileIDs) {
        this.fileIDs = fileIDs;
    }
    
    /**
     * @return Whether the integrity check gave a positive result. E.g. returns false, if any integrity problems 
     * occurred (whether any files were missing or any disagreements about checksum).
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * @return The FileIDs for this report.
     */
    public FileIDs getFileIDs() {
        return fileIDs;
    }
    
    /**
     * @return The map of checksum errors. Empty if no such errors.
     */
    public Map<String, Map<String, Integer>> getChecksumErrors() {
        return checksumResults;
    }
    
    /**
     * @return The map of missing file ids and the pillars, where they are missing.
     */
    public Map<String, List<String>> getMissingFileIDs() {
        return missingFileIDs;
    }
    
    /**
     * Insert the checksum results, when a checksum disagreement occurs.
     * This means that the integrity check found an error.
     * @param fileId The id of the file for the checksum disagreement.
     * @param checksums The map of different checksums and their count.
     */
    public void addIncorrectChecksums(String fileId, Map<String, Integer> checksums) {
        checksumResults.put(fileId, checksums);
        valid = false;
    }
    
    /**
     * Insert the id of a file, which has not been found on every pillar.
     * This means that the integrity check found an error.
     * @param fileId The id of the file, which is missing.
     * @param pillarIds The list of ids for the pillars, where the file is missing.
     */
    public void addMissingFile(String fileId, List<String> pillarIds) {
        missingFileIDs.put(fileId, pillarIds);
        valid = false;
    }
    
    /** 
     * @return A human readable report for the integrity problem.
     */
    public String generateReport() {
        if(valid) {
            return "Valid";
        }
        StringBuffer res = new StringBuffer();
        
        res.append("Invalid integrity on " + fileIDs + "\n");
        
        if(!checksumResults.isEmpty()) {
            res.append("Checksum errors: \n");
            for(Map.Entry<String, Map<String, Integer>> checksumErrors : checksumResults.entrySet()) {
                res.append(checksumErrors.getKey());
                for(Map.Entry<String, Integer> csError : checksumErrors.getValue().entrySet()) {
                    res.append(" : " + csError.getValue() + " '" + csError.getKey() + "' ");
                }
                res.append("\n");
            }
        }
        
        if(!missingFileIDs.isEmpty()) {
            res.append("Missing files: \n");
            for(Map.Entry<String, List<String>> missingFile : missingFileIDs.entrySet()) {
                res.append(missingFile.getKey() + ": ");
                for(String pillarId : missingFile.getValue()) {
                    res.append(pillarId + ", ");
                }
                res.append("\n");
            }
        }
        
        return res.toString();
    }
}
