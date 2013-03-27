package org.bitrepository.commandline.resultmodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class FileIDsResult {

    /** FileID */
    private final String id;
    /** Size of the file */
    private BigInteger size;
    /** List of contributors reported to have the file. */
    private List<String> contributors;
    
    public FileIDsResult(String id, BigInteger size, String contributor) {
        this.id = id;
        this.size = size;
        contributors = new ArrayList<String>();
        contributors.add(contributor);
    }
    
    /**
     * Updates the filesize of the file, if the filesize does not match, mark it as a unknown  
     */
    public void updateSize(BigInteger size) {
        if(size != null) {
            if(this.size == null) {
                this.size = size; 
            } else if(!size.equals(this.size)) {
                this.size = new BigInteger("-1");
            }    
        } 
    }
    
    /**
     * Gets the size of the file. 
     * @return the size of the file, or null if unknown or -1 if there are filesize confilicts between contributors 
     */
    public BigInteger getSize() {
        return size;
    }
    
    /**
     * Add a contributor to the list of contributors 
     */
    public void addContributor(String contributor) {
        if(!contributors.contains(contributor)) {
            contributors.add(contributor);
        }
    }
    
    /**
     * Get the list of contributors
     * @return the list of contributors 
     */
    public List<String> getContributors() {
        return contributors;
    }
    
    /**
     * Get the id of the file
     * @return the fileID 
     */
    public String getID() {
        return id;
    }
    
    public boolean isComplete(int numberOfExpectedContributors) {
        return (contributors.size() == numberOfExpectedContributors);
    }
}
