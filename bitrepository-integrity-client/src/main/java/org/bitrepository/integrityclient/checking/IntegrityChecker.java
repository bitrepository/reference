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
package org.bitrepository.integrityclient.checking;

import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * This is the interface for checking the integrity of the data in the cache.
 * 
 * It should be called by the collector, when it has finished collecting integrity data.
 * Whenever a check goes badly, it should send an Alarm message about it.
 */
public interface IntegrityChecker {
    /**
     * Validates that the pillars contain the requested fileIDs. 
     * 
     * @param fileIDs The ids of the files to validate (e.g. a list of files or all files).
     * @return Whether the given file ids where validated.
     */
    public boolean checkFileIDs(FileIDs fileIDs);
    
    /**
     * Validates the checksum of the requested files for all the pillars.
     * 
     * @param fileIDs The files, which checksum are requested to be validated.
     * @return Whether the checksums of the given file ids where validated.
     */
    public boolean checkChecksum(FileIDs fileIDs);
}
