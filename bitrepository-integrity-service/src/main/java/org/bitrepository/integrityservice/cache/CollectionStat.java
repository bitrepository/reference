package org.bitrepository.integrityservice.cache;

import java.util.Date;

public class CollectionStat {
    private final String collectionID;
    private final Long fileCount;
    private final Long dataSize;
    private final Long checksumErrors;
    private final Date statsTime;
    /** The date that the statistics were updated */
    private final Date updateTime;
    
    public CollectionStat(String collectionID, Long fileCount, Long dataSize, Long checksumErrors, 
            Date statsTime, Date updateTime) {
        this.collectionID = collectionID;
        this.fileCount = fileCount;
        this.dataSize = dataSize;
        this.checksumErrors = checksumErrors;
        this.statsTime = statsTime;
        this.updateTime = updateTime;
    }


    /** The ID of the collection */
    public String getCollectionID() {
        return collectionID;
    }
    /** The number of files in the collection */
    public Long getFileCount() {
        return fileCount;
    }
    /** The size of the collection */
    public Long getDataSize() {
        return dataSize;
    }
    /** The number of checksum errors */
    public Long getChecksumErrors() {
        return checksumErrors;
    }
    /** The date that the statistics were collected */
    public Date getStatsTime() {
        return statsTime;
    }
    /** The date that the statistics were updated */
    public Date getUpdateTime() {
        return updateTime;
    }
}
