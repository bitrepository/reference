/*
 * #%L
 * Bitrepository Command Line
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.commandline.resultmodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileInfoResult {

    /** FileID */
    private final String id;
    /** Mapping from pillar/contributorid to returned checksum */
    private final Map<String, FileInfo> pillarFileInfoMap;
    /** Indication if there's checksum disagreement */
    private boolean dirty;
    
    public FileInfoResult(String id, String contributor, String checksum, BigInteger fileSize) {
        pillarFileInfoMap = new HashMap<String, FileInfo>();
        this.id = id;
        dirty = false;
        FileInfo fi = new FileInfo(checksum, fileSize);
        pillarFileInfoMap.put(contributor, fi);
    }
    
    /**
     * Add a contributor with it's checksum to the result
     * @param contributor the ID of the contributor
     * @param checksum the checksum that the contributor delivered
     */
    public void addContributor(String contributor, String checksum, BigInteger fileSize) {
        FileInfo fi = new FileInfo(checksum, fileSize);
        if(!dirty && !pillarFileInfoMap.containsValue(fi)) {
            dirty = true;
        }
        pillarFileInfoMap.put(contributor, fi);
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
     * @return the set of contributors which have delivered a checksum 
     */
    public List<String> getContributors() {
        return new ArrayList<String>(pillarFileInfoMap.keySet());
    }
    
    /**
     * @param contributor The contributor to get the checksum for
     * @return the checksum from a given contributor
     */
    public String getChecksum(String contributor) {
        return pillarFileInfoMap.get(contributor).getChecksum();
    }
    
    /**
     * @param contributor The contributor to get the checksum for
     * @return the filesize from a given contributor
     */
    public BigInteger getFileSize(String contributor) {
        return pillarFileInfoMap.get(contributor).getSize();
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
     * @param expectedNumberOfContributors the expected number of contributors.
     * @return true, if there's registered expectedNumberOfContributors of contributors.  
     */
    public boolean isComplete(int expectedNumberOfContributors) {
        return (pillarFileInfoMap.size() == expectedNumberOfContributors);
    }
    
    private class FileInfo {
        private String checksum;
        private BigInteger size;
        
        FileInfo(String checksum, BigInteger size) {
            this.checksum = checksum;
            this.size = size;
        }
        
        String getChecksum() {
            return checksum;
        }
        
        BigInteger getSize() {
            return size;
        }

        private FileInfoResult getEnclosingInstance() {
            return FileInfoResult.this;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
            result = prime * result + ((size == null) ? 0 : size.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FileInfo other = (FileInfo) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (checksum == null) {
                if (other.checksum != null)
                    return false;
            } else if (!checksum.equals(other.checksum))
                return false;
            if (size == null) {
                if (other.size != null)
                    return false;
            } else if (!size.equals(other.size))
                return false;
            return true;
        }
    }
}
