/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.checking;

import java.util.Collection;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.checking.reports.IntegrityReportModel;
import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;

/**
 * This is the interface for checking the integrity of the data in the cache.
 * 
 * The checks should be performed by the steps of workflows.
 */
public interface IntegrityChecker {
    /**
     * Validates that the pillars contain the requested fileIDs. 
     * 
     * @param fileIDs The ids of the files to validate (e.g. a list of files or all files).
     * @param collectionId The collection to validate the files from
     * @return Whether the given file ids where validated.
     */
    MissingFileReportModel checkFileIDs(FileIDs fileIDs, String collectionId);
    
    /**
     * Validates the checksum of all the files for all the pillars.
     * @param collectionId The collection to validate the checksums in
     * @return Whether the checksums of the given file ids where validated.
     */
    IntegrityReportModel checkChecksum(String collectionId);
    
    /**
     * Validates whether any checksums are missing from any pillar, even though the pillar contains 
     * the file.
     * 
     * @param collectionId The collection to check for missing checksums
     * @return The report containing the information about any missing checksums
     */
    IntegrityReportModel checkMissingChecksums(String collectionId);
    
    /**
     * Validates whether any files are older than a given interval.
     * 
     * @param maxChecksumAgeProvider Defines when to mark checksums as obsolete.
     * @param pillarIDs the collection of pillars to check for obsolete checksums
     * @param collectionId the collection which to check for obsolete checksums
     * @return The report for the check.
     */
    IntegrityReportModel checkObsoleteChecksums(MaxChecksumAgeProvider maxChecksumAgeProvider,
        Collection<String> pillarIDs, String collectionId);
}
