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
package org.bitrepository.integrityservice.cache;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Class for containing the information about a given file at a given pillar.
 */
public class FileInfo {
    /** The id of the file.*/
    private final String fileId;
    /** The date for the last check for the file ID.*/
    private XMLGregorianCalendar fileCreationTimestamp;
    /** The checksum of the file.*/
    private String checksum;
    /** The type of checksum, e.g. algorithm and optional salt*/
    private ChecksumSpecTYPE checksumType;
    /** The date for the last check of the checksum.*/
    private XMLGregorianCalendar checksumLastCheck;
    /** The id of the pillar.*/
    private final String pillarId;
    
    /**
     * Constructor for all data.
     * @param fileId The id of the file (may not be null)
     * @param fileLastCheck The date for the last check of the file id (if null, replaced by Epoch).
     * @param checksum The checksum of the file.
     * @param checksumType The type of checksum (e.g. Algorithm and optionally salt). 
     * @param checksumLastCheck The date for the last check of the checksum (if null, replaced by Epoch).
     * @param pillarId The id of the pillar (may not be null)
     */
    public FileInfo(String fileId, XMLGregorianCalendar fileLastCheck, String checksum, 
            ChecksumSpecTYPE checksumType, XMLGregorianCalendar checksumLastCheck, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileID");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        this.fileId = fileId;
        this.fileCreationTimestamp = fileLastCheck;
        this.checksum = checksum;
        this.checksumType = checksumType;
        this.checksumLastCheck = checksumLastCheck;
        this.pillarId = pillarId;
        
        // If file id date is null, then replace with epoch.
        if(fileLastCheck == null) {
            this.fileCreationTimestamp = CalendarUtils.getEpoch();
        }
        
        // If checksum date is null, then replace with epoch.
        if(checksumLastCheck == null) {
            this.checksumLastCheck = CalendarUtils.getEpoch();
        }
    }
    
    /**
     * Constructor for only file id and pillar id.
     * @param fileId The id of the file.
     * @param pillarId The id of the pillar.
     */
    public FileInfo(String fileId, String pillarId) {
        this(fileId, null, null, null, null, pillarId);
    }
    
    /**
     * @return The id of the file.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @return The date for the last check of the file. 
     */
    public XMLGregorianCalendar getDateForLastFileIDCheck() {
        return fileCreationTimestamp;
    }
    
    /**
     * @param dateForLastFileIDCheck The new date for the last check of the file.
     */
    public void setDateForLastFileIDCheck(XMLGregorianCalendar dateForLastFileIDCheck) {
        this.fileCreationTimestamp = dateForLastFileIDCheck;
    }
    
    /**
     * @return Checksum of the file. This can be null, if the checksum has not been retrieved yet.
     */
    public String getChecksum() {
        return checksum;
    }
    
    /**
     * @param checksum The new checksum.
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /**
     * @return The type of checksum, e.g. algorithm and optional salt.
     */
    public ChecksumSpecTYPE getChecksumType() {
        return checksumType;
    }
    
    /**
     * @param checksumType The new type for the checksum, e.g. algorithm and optional salt.
     */
    public void setChecksumType(ChecksumSpecTYPE checksumType) {
        this.checksumType = checksumType;
    }
    
    /**
     * @return The date for the last check of the checksum.
     */
    public XMLGregorianCalendar getDateForLastChecksumCheck() {
        return checksumLastCheck;
    }
    
    /**
     * @param dateForLastChecksumCheck The new date for the last checksum of the checksum.
     */
    public void setDateForLastChecksumCheck(XMLGregorianCalendar dateForLastChecksumCheck) {
        this.checksumLastCheck = dateForLastChecksumCheck;
    }
    
    /**
     * @return The id of the pillar.
     */
    public String getPillarId() {
        return pillarId;
    }
    
    @Override
    public String toString() {
        return "Pillar id: " + pillarId + ", File id: " + fileId + " (date: " + fileCreationTimestamp + "), Checksum: " 
                + checksum + " (date: " + checksumLastCheck + "), Checksum type: " + checksumType;
    }
}
