package org.bitrepository.integrityservice.cache;

import java.util.Date;

public class PillarStat {

    /** The ID of the pillar */
    private String pillarID;
    /** The ID of the collection */
    private String collectionID;
    /** The number of files in the collection */
    private Long fileCount;
    /** The size of the collection */
    private Long dataSize; 
    /** The number of missing files on the pillar */
    private Long missingFiles; 
    /** The number of checksum errors */
    private Long checksumErrors;
    /** The date that the statistics were collected */
    private Date statsTime;
    /** The date that the statistics were updated */
    private Date updateTime;
    
    public PillarStat(String pillarID, String collectionID, Long fileCount, Long dataSize, Long missingFiles, 
            Long checksumErrors, Date statsTime, Date updateTime) {
        this.pillarID = pillarID;
        this.collectionID = collectionID;
        this.fileCount = fileCount;
        this.dataSize = dataSize;
        this.missingFiles = missingFiles;
        this.checksumErrors = checksumErrors;
        this.statsTime = statsTime;
        this.updateTime = updateTime;
    }
    
    public String getPillarID() {
        return pillarID;
    }
    public void setPillarID(String pillarID) {
        this.pillarID = pillarID;
    }
    public String getCollectionID() {
        return collectionID;
    }
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
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
    
}
