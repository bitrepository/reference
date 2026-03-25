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
import java.time.Instant;
import java.util.Date;

/**
 * Class for containing the information about a given file at a given pillar.
 */
public class FileInfo {
    private final String fileID;
    private Instant fileCreationTimestamp;
    private String checksum;
    private Instant checksumLastCheck;
    private final String pillarID;
    private final Long fileSize;
    private Instant lastSeenGetFileIDs;
    private Instant lastSeenGetChecksums;

    public FileInfo(String fileID, Instant fileLastCheck, String checksum, Long fileSize,
                    Instant checksumLastCheck, String pillarID, Instant lastSeenGetFileIDs, Instant lastSeenGetChecksums) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        this.fileID = fileID;
        this.fileCreationTimestamp = fileLastCheck != null ? fileLastCheck : Instant.EPOCH;
        this.checksum = checksum;
        this.fileSize = fileSize;
        this.checksumLastCheck = checksumLastCheck != null ? checksumLastCheck : Instant.EPOCH;
        this.pillarID = pillarID;
        this.lastSeenGetFileIDs = lastSeenGetFileIDs;
        this.lastSeenGetChecksums = lastSeenGetChecksums;
    }

    /**
     * @param fileID            The id of the file (may not be null)
     * @param fileLastCheck     The date for the last check of the file id (if null, replaced by Epoch).
     * @param checksum          The checksum of the file.
     * @param fileSize          The size for the file, in Bytes
     * @param checksumLastCheck The date for the last check of the checksum (if null, replaced by Epoch).
     * @param pillarID          The id of the pillar (may not be null)
     * @deprecated Use {@link #FileInfo(String, Instant, String, Long, Instant, String, Instant, Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public FileInfo(String fileID, XMLGregorianCalendar fileLastCheck, String checksum, Long fileSize,
                    XMLGregorianCalendar checksumLastCheck, String pillarID) {
        this(fileID, 
             fileLastCheck != null ? CalendarUtils.convertFromXMLGregorianCalendarToInstant(fileLastCheck) : null,
             checksum, fileSize,
             checksumLastCheck != null ? CalendarUtils.convertFromXMLGregorianCalendarToInstant(checksumLastCheck) : null,
             pillarID, null, null);
    }

    /**
     * Constructor for only file id and pillar id.
     *
     * @param fileID   The id of the file.
     * @param pillarID The id of the pillar.
     */
    public FileInfo(String fileID, String pillarID) {
        this(fileID, (Instant) null, null, null, (Instant) null, pillarID, null, null);
    }

    public String getFileId() {
        return fileID;
    }

    public Instant getDateForLastFileIDCheckInstant() {
        return fileCreationTimestamp;
    }

    @Deprecated(forRemoval = true)
    public XMLGregorianCalendar getDateForLastFileIDCheck() {
        return CalendarUtils.getXmlGregorianCalendar(fileCreationTimestamp);
    }

    public void setDateForLastFileIDCheck(Instant dateForLastFileIDCheck) {
        this.fileCreationTimestamp = dateForLastFileIDCheck;
    }

    @Deprecated(forRemoval = true)
    public void setDateForLastFileIDCheck(XMLGregorianCalendar dateForLastFileIDCheck) {
        this.fileCreationTimestamp = dateForLastFileIDCheck != null ? 
            CalendarUtils.convertFromXMLGregorianCalendarToInstant(dateForLastFileIDCheck) : Instant.EPOCH;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Instant getDateForLastChecksumCheckInstant() {
        return checksumLastCheck;
    }

    @Deprecated(forRemoval = true)
    public XMLGregorianCalendar getDateForLastChecksumCheck() {
        return CalendarUtils.getXmlGregorianCalendar(checksumLastCheck);
    }

    public void setDateForLastChecksumCheck(Instant dateForLastChecksumCheck) {
        this.checksumLastCheck = dateForLastChecksumCheck;
    }

    @Deprecated(forRemoval = true)
    public void setDateForLastChecksumCheck(XMLGregorianCalendar dateForLastChecksumCheck) {
        this.checksumLastCheck = dateForLastChecksumCheck != null ?
            CalendarUtils.convertFromXMLGregorianCalendarToInstant(dateForLastChecksumCheck) : Instant.EPOCH;
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

    public Instant getLastSeenGetFileIDsInstant() {
        return lastSeenGetFileIDs;
    }

    @Deprecated(forRemoval = true)
    public Date getLastSeenGetFileIDs() {
        return lastSeenGetFileIDs != null ? Date.from(lastSeenGetFileIDs) : null;
    }

    public void setLastSeenGetFileIDs(Instant lastSeenGetFileIDs) {
        this.lastSeenGetFileIDs = lastSeenGetFileIDs;
    }

    @Deprecated(forRemoval = true)
    public void setLastSeenGetFileIDs(Date lastSeenGetFileIDs) {
        this.lastSeenGetFileIDs = lastSeenGetFileIDs != null ? lastSeenGetFileIDs.toInstant() : null;
    }

    public Instant getLastSeenGetChecksumsInstant() {
        return lastSeenGetChecksums;
    }

    @Deprecated(forRemoval = true)
    public Date getLastSeenGetChecksums() {
        return lastSeenGetChecksums != null ? Date.from(lastSeenGetChecksums) : null;
    }

    public void setLastSeenGetChecksums(Instant lastSeenGetChecksums) {
        this.lastSeenGetChecksums = lastSeenGetChecksums;
    }

    @Deprecated(forRemoval = true)
    public void setLastSeenGetChecksums(Date lastSeenGetChecksums) {
        this.lastSeenGetChecksums = lastSeenGetChecksums != null ? lastSeenGetChecksums.toInstant() : null;
    }
}
