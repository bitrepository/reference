/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Used for extracting textual information about the integrity status.
 */
public interface IntegrityReporter {
    /**
     * @return Whether the integrity check gave a positive result. E.g. returns false, if any integrity problems 
     * occurred (whether any files were missing, any disagreements about checksum, etc.).
     */
    boolean hasIntegrityIssues();
    
    /**
     * Method to test if a report is available
     * @return true if an integrity report is available.  
     */
    boolean hasReport();
    
    /**
     * Retrieves the written report 
     * @throws FileNotFoundException if no report is found
     */
    File getReport() throws FileNotFoundException;
    
    /** 
     * Creates the human readable report for the entire integrity issue.
     * This involves all the specifics, e.g. at single file-level.
     * @throws IOException 
     */
    void generateReport() throws IOException;
    
    /**
     * Create a human readable summary of the integrity issue.
     * Should only contain the overall issue description, not specifics.
     * @return The summary of the integrity issue.
     */
    String generateSummaryOfReport();
    
    /**
     * Return the ID of the collection that the report is about. 
     * @return The collectionID
     */
    String getCollectionID();
    
    /**
     * Report that a file has been deleted from the collection. Note this is not considered a integrity issue.
     * @param fileID The ID of the file that has been removed 
     * @throws IOException if writing fails
     */
    void reportDeletedFile(String fileID) throws IOException;
    
    /**
     * Report that a file is missing from a pillar 
     * @param fileID The ID of the file that is missing
     * @param pillarID The ID of the pillar that the file is missing on. 
     * @throws IOException 
     */
    void reportMissingFile(String fileID, String pillarID) throws IOException;
    
    /**
     * Report that a file have a checksum issues on a given pillar
     * @param fileID The ID of the file that has a checksum issue
     * @param pillarID The ID of the pillar with the checksum issue 
     * @throws IOException 
     */
    void reportChecksumIssue(String fileID, String pillarID) throws IOException;
    
    /**
     *  Report that a file is missing a checksum on a given pillar
     *  @param fileID The ID of the file that is missing a checksum
     *  @param pillarID The ID of the pillar that does not have the checksum for the file
     * @throws IOException 
     */
    void reportMissingChecksum(String fileID, String pillarID) throws IOException;
    
    /**
     * Report that a file have an obsolete checksum on a given pillar 
     * @param fileID The ID of the file with the obsolete checksum
     * @param pillarID The ID of the pillar that have the obsolete checksum
     * @throws IOException 
     */
    void reportObsoleteChecksum(String fileID, String pillarID) throws IOException;
    
}
