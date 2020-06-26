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
import java.util.List;

/**
 * Class for temporary storage of FileIDs result information 
 */
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
        contributors = new ArrayList<>();
        contributors.add(contributor);
    }
    
    /**
     * Updates the filesize of the file, if the filesize does not match, mark it as a unknown  
     * @param updateSize The file size to update with
     */
    public void updateSize(BigInteger updateSize) {
        if(updateSize != null) {
            if(size == null) {
                size = updateSize; 
            } else if(!updateSize.equals(size)) {
                size = new BigInteger("-1");
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
     * @param contributor The contributor to add
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
