/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.cache;

import org.bitrepository.common.utils.TimeUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Statistics class for holding the statistics data going in and out of the database
 */
public class PillarCollectionStat {
    private final String pillarID;
    private final String collectionID;
    private final String pillarName;
    private final String pillarType;
    private Long fileCount = 0L;
    private Long dataSize = 0L;
    private Long missingFiles = 0L;
    private Long obsoleteChecksums = 0L;
    private Long missingChecksums = 0L;
    private Long checksumErrors = 0L;
    private String maxAgeForChecksums;
    private Instant oldestChecksumTimestamp;
    private Date statsTime;
    private Date updateTime;

    public PillarCollectionStat(String pillarID, String collectionID, String pillarName, String pillarType) {
        this.pillarID = pillarID;
        this.collectionID = collectionID;
        this.pillarName = pillarName;
        this.pillarType = pillarType;
    }

    public PillarCollectionStat(String pillarID, String collectionID, String pillarHostname, String pillarType,
                                Long fileCount, Long dataSize,
                                Long missingFiles, Long checksumErrors, Long missingChecksums, Long obsoleteChecksum,
                                String maxAgeForChecksums,
                                Instant oldestChecksumTimestamp, Date statsTime, Date updateTime) {
        this.pillarID = pillarID;
        this.collectionID = collectionID;
        this.pillarName = pillarName;
        this.pillarType = pillarType;
        this.fileCount = fileCount;
        this.dataSize = dataSize;
        this.missingFiles = missingFiles;
        this.checksumErrors = checksumErrors;
        this.missingChecksums = missingChecksums;
        this.obsoleteChecksums = obsoleteChecksum;
        this.statsTime = statsTime;
        this.updateTime = updateTime;
        this.maxAgeForChecksums = maxAgeForChecksums;
        this.oldestChecksumTimestamp = oldestChecksumTimestamp;
    }

    public String getPillarID() {
        return pillarID;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getPillarName() {
        return pillarName;
    }

    public String getPillarType() {
        return pillarType;
    }

    public Long getFileCount() {
        return fileCount;
    }

    public void setFileCount(Long fileCount) {
        this.fileCount = fileCount;
    }

    public Long getDataSize() {
        return dataSize;
    }

    public void setDataSize(Long dataSize) {
        this.dataSize = dataSize;
    }

    public Long getMissingFiles() {
        return missingFiles;
    }

    public void setMissingFiles(Long missingFiles) {
        this.missingFiles = missingFiles;
    }

    public Long getChecksumErrors() {
        return checksumErrors;
    }

    public void setChecksumErrors(Long checksumErrors) {
        this.checksumErrors = checksumErrors;
    }

    public Date getStatsTime() {
        return statsTime;
    }

    public void setStatsTime(Date statsTime) {
        this.statsTime = statsTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getObsoleteChecksums() {
        return obsoleteChecksums;
    }

    public void setObsoleteChecksums(Long obsoleteChecksums) {
        this.obsoleteChecksums = obsoleteChecksums;
    }

    public Long getMissingChecksums() {
        return missingChecksums;
    }

    public void setMissingChecksums(Long missingChecksums) {
        this.missingChecksums = missingChecksums;
    }

    /** @return Human-readable age of the oldest checksum, for example "3m 46s" */
    @NotNull
    public String getAgeOfOldestChecksum() {
        if (oldestChecksumTimestamp == null) {
            return "N/A";
        }
        ZoneId zone = ZoneId.systemDefault();
        return TimeUtils.humanDifference(oldestChecksumTimestamp.atZone(zone), ZonedDateTime.now(zone));
    }

    public boolean hasOldestChecksumTimestamp() {
        return oldestChecksumTimestamp != null;
    }

    /** @throws NullPointerException if hasOldestChecksumTimestamp() does not return true */
    public long getOldestChecksumTimestampMillis() {
        return oldestChecksumTimestamp.toEpochMilli();
    }

    public void setOldestChecksumTimestamp(Instant oldestChecksumTimestamp) {
        this.oldestChecksumTimestamp = oldestChecksumTimestamp;
    }

    public String getMaxAgeForChecksums() {
        return maxAgeForChecksums;
    }

}
