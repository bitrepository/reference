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

import java.util.Date;

/**
 * Statistics class for holding the statistics data going in and out of the database
 */
public class PillarCollectionStat {
    private final String pillarID;
    private final String collectionID;
    private final String pillarHostname;
    private Long fileCount = 0L;
    private Long dataSize = 0L;
    private Long missingFiles = 0L;
    private Long obsoleteChecksums = 0L;
    private Long missingChecksums = 0L;
    private Long checksumErrors = 0L;
    private Date statsTime;
    private Date updateTime;

    public PillarCollectionStat(String pillarID, String collectionID, String pillarHostname) {
        this.pillarID = pillarID;
        this.collectionID = collectionID;
        this.pillarHostname = pillarHostname;
    }

    public PillarCollectionStat(String pillarID, String collectionID, String pillarHostname, Long fileCount, Long dataSize,
                                Long missingFiles,
                                Long checksumErrors, Long missingChecksums, Long obsoleteChecksum, Date statsTime, Date updateTime) {
        this.pillarID = pillarID;
        this.collectionID = collectionID;
        this.pillarHostname = pillarHostname;
        this.fileCount = fileCount;
        this.dataSize = dataSize;
        this.missingFiles = missingFiles;
        this.checksumErrors = checksumErrors;
        this.missingChecksums = missingChecksums;
        this.obsoleteChecksums = obsoleteChecksum;
        this.statsTime = statsTime;
        this.updateTime = updateTime;
    }

    public String getPillarID() {
        return pillarID;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getPillarHostname() { return pillarHostname; }

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

}
