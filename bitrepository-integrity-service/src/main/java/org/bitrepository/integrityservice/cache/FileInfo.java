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

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

/**
 * Class for containing the information about a given file at a given pillar.
 */
public class FileInfo {
    private final String fileID;
    private XMLGregorianCalendar fileCreationTimestamp;
    private String checksum;
    private XMLGregorianCalendar checksumLastCheck;
    private final String pillarID;
    private final Long fileSize;
    private Date lastSeenGetFileIDs;
    private Date lastSeenGetChecksums;

    /**
     * @param fileID            The id of the file (may not be null)
     * @param fileLastCheck     The date for the last check of the file id (if null, replaced by Epoch).
     * @param checksum          The checksum of the file.
     * @param fileSize          The size for the file, in Bytes
     * @param checksumLastCheck The date for the last check of the checksum (if null, replaced by Epoch).
     * @param pillarID          The id of the pillar (may not be null)
     */
    public FileInfo(String fileID, XMLGregorianCalendar fileLastCheck, String checksum, Long fileSize,
                    XMLGregorianCalendar checksumLastCheck, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        this.fileID = fileID;
        this.fileCreationTimestamp = fileLastCheck;
        this.checksum = checksum;
        this.fileSize = fileSize;
        this.checksumLastCheck = checksumLastCheck;
        this.pillarID = pillarID;

        // If file id date is null, then replace with epoch.
        if (fileLastCheck == null) {
            this.fileCreationTimestamp = CalendarUtils.getEpoch();
        }

        // If checksum date is null, then replace with epoch.
        if (checksumLastCheck == null) {
            this.checksumLastCheck = CalendarUtils.getEpoch();
        }
    }

    /**
     * Constructor for only file id and pillar id.
     *
     * @param fileID   The id of the file.
     * @param pillarID The id of the pillar.
     */
    public FileInfo(String fileID, String pillarID) {
        this(fileID, null, null, null, null, pillarID);
    }

    public String getFileId() {
        return fileID;
    }

    public XMLGregorianCalendar getDateForLastFileIDCheck() {
        return fileCreationTimestamp;
    }

    public void setDateForLastFileIDCheck(XMLGregorianCalendar dateForLastFileIDCheck) {
        this.fileCreationTimestamp = dateForLastFileIDCheck;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public XMLGregorianCalendar getDateForLastChecksumCheck() {
        return checksumLastCheck;
    }

    public void setDateForLastChecksumCheck(XMLGregorianCalendar dateForLastChecksumCheck) {
        this.checksumLastCheck = dateForLastChecksumCheck;
    }

    public String getPillarId() {
        return pillarID;
    }

    public Long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        return "Pillar id: " + pillarID + ", File id: " + fileID + " (date: "
                + fileCreationTimestamp + "), Checksum: " + checksum + " (date: "
                + checksumLastCheck + ", lastSeenGetFileIDs: " + lastSeenGetFileIDs
                + ", lastSeenGetChecksums: " + lastSeenGetChecksums + ")";
    }

    public Date getLastSeenGetFileIDs() {
        return lastSeenGetFileIDs;
    }

    public void setLastSeenGetFileIDs(Date lastSeenGetFileIDs) {
        this.lastSeenGetFileIDs = lastSeenGetFileIDs;
    }

    public Date getLastSeenGetChecksums() {
        return lastSeenGetChecksums;
    }

    public void setLastSeenGetChecksums(Date lastSeenGetChecksums) {
        this.lastSeenGetChecksums = lastSeenGetChecksums;
    }
}
