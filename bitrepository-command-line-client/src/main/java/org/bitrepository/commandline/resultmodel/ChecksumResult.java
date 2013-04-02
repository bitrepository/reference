package org.bitrepository.commandline.resultmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChecksumResult {

    /** FileID */
    private final String id;
    /** Mapping from pillar/contributorid to returned checksum */
    private final Map<String, String> pillarChecksumMap;
    /** Indication if there's checksum disagreement */
    private boolean dirty;
    
    public ChecksumResult(String id, String contributor, String checksum) {
        pillarChecksumMap = new HashMap<String, String>();
        this.id = id;
        dirty = false;
        pillarChecksumMap.put(contributor, checksum);
    }
    
    /**
     * Add a contributor with it's checksum to the result
     * @param contributor, the ID of the contributor
     * @param checksum, the checksum that the contributor delivered 
     */
    public void addContributor(String contributor, String checksum) {
        if(!dirty && !pillarChecksumMap.containsValue(checksum)) {
            dirty = true;
        }
        pillarChecksumMap.put(contributor, checksum);
    }
    
    /**
     * Is the result 'dirty', i.e. is there checksum disagreement among the answered contributors.
     * @return false if all contributors have agreed on the checksum.  
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Get the list of contributors. 
     * @return Set<String>, the set of contributors which have delivered a checksum 
     */
    public List<String> getContributors() {
        return new ArrayList<String>(pillarChecksumMap.keySet());
    }
    
    /**
     * Get the checksum from a given contributor 
     */
    public String getChecksum(String contributor) {
        return pillarChecksumMap.get(contributor);
    }
    
    /**
     * Get the fileID of the file for which the checksum is for.
     * @return String, the fileID 
     */
    public String getID() {
        return id;
    }
    
    /**
     * Determine if we have enough answers to consider the result complete
     * @param expectedNumberOfContributors, the expected number of contributors. 
     * @return true, if there's registered expectedNumberOfContributors of contributors.  
     */
    public boolean isComplete(int expectedNumberOfContributors) {
        return (pillarChecksumMap.size() == expectedNumberOfContributors);
    }
}
