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

    /**
     * @deprecated Use {@link #getDateForLastFileIDCheckInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public XMLGregorianCalendar getDateForLastFileIDCheck() {
        return CalendarUtils.getXmlGregorianCalendar(fileCreationTimestamp);
    }

    public void setDateForLastFileIDCheck(Instant dateForLastFileIDCheck) {
        this.fileCreationTimestamp = dateForLastFileIDCheck;
    }

    /**
     * @deprecated Use {@link #setDateForLastFileIDCheck(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setDateForLastFileIDCheck(XMLGregorianCalendar dateForLastFileIDCheck) {
        this.fileCreationTimestamp = CalendarUtils.convertFromXMLGregorianCalendarToInstant(dateForLastFileIDCheck);
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

    /**
     * @deprecated Use {@link #getDateForLastChecksumCheckInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public XMLGregorianCalendar getDateForLastChecksumCheck() {
        return CalendarUtils.getXmlGregorianCalendar(checksumLastCheck);
    }

    public void setDateForLastChecksumCheck(Instant dateForLastChecksumCheck) {
        this.checksumLastCheck = dateForLastChecksumCheck;
    }

    /**
     * @deprecated Use {@link #setDateForLastChecksumCheck(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setDateForLastChecksumCheck(XMLGregorianCalendar dateForLastChecksumCheck) {
        this.checksumLastCheck = CalendarUtils.convertFromXMLGregorianCalendarToInstant(dateForLastChecksumCheck);
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

    /**
     * @deprecated Use {@link #getLastSeenGetFileIDsInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public XMLGregorianCalendar getLastSeenGetFileIDs() {
        return lastSeenGetFileIDs != null ? CalendarUtils.getXmlGregorianCalendar(lastSeenGetFileIDs) : null;
    }

    public void setLastSeenGetFileIDs(Instant lastSeenGetFileIDs) {
        this.lastSeenGetFileIDs = lastSeenGetFileIDs;
    }

    /**
     * @deprecated Use {@link #setLastSeenGetFileIDs(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setLastSeenGetFileIDs(XMLGregorianCalendar lastSeenGetFileIDs) {
        this.lastSeenGetFileIDs = CalendarUtils.convertFromXMLGregorianCalendarToInstant(lastSeenGetFileIDs);
    }

    public Instant getLastSeenGetChecksumsInstant() {
        return lastSeenGetChecksums;
    }

    /**
     * @deprecated Use {@link #getLastSeenGetChecksumsInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public XMLGregorianCalendar getLastSeenGetChecksums() {
        return lastSeenGetChecksums != null ? CalendarUtils.getXmlGregorianCalendar(lastSeenGetChecksums) : null;
    }

    public void setLastSeenGetChecksums(Instant lastSeenGetChecksums) {
        this.lastSeenGetChecksums = lastSeenGetChecksums;
    }

    /**
     * @deprecated Use {@link #setLastSeenGetChecksums(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setLastSeenGetChecksums(XMLGregorianCalendar lastSeenGetChecksums) {
        this.lastSeenGetChecksums = CalendarUtils.convertFromXMLGregorianCalendarToInstant(lastSeenGetChecksums);
    }

    /**
     * @deprecated Use {@link #setLastSeenGetChecksums(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setLastSeenGetChecksums(Date lastSeenGetChecksums) {
        this.lastSeenGetChecksums = lastSeenGetChecksums != null ? lastSeenGetChecksums.toInstant() : null;
    }
}
