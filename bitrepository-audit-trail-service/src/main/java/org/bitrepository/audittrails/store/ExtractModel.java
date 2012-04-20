/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.audittrails.store;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.FileAction;

/**
 * Container for the extraction of data from the audit trail database.
 */
class ExtractModel {
    /** @see getFileId(). */
    private String fileId;
    /** @see getContributorId(). */
    private String contributorId;
    /** @see getMinSeqNumber(). */
    private Long minSeqNumber;
    /** @see getMaxSeqNumber(). */
    private Long maxSeqNumber;
    /** @see getActorName(). */
    private String actorName;
    /** @see getOperation(). */
    private FileAction operation;
    /** @see getStartDate(). */
    private Date startDate;
    /** @see getEndDate(). */
    private Date endDate;
    
    /**
     * Constructor, with no arguments. All variables are set to null.
     */
    public ExtractModel() {}
    
    /**
     * Constructor.
     * @param fileId The id for the file to restrict the extraction.
     * @param contributorId The id for the contributor to restrict the extraction.
     * @param minSeqNumber The minimum sequence number to restrict the extraction.
     * @param maxSeqNumber The maximum sequence number to restrict the extraction.
     * @param actorName The name of the actor to restrict the extraction.
     * @param operation The operation to restrict the extraction.
     * @param startDate The earliest date to restrict the extraction.
     * @param endDate The latest date to restrict the extraction.
     */
    public ExtractModel(String fileId, String contributorId, Long minSeqNumber, Long maxSeqNumber,
        String actorName, FileAction operation, Date startDate, Date endDate) {
        this.fileId = fileId;
        this.contributorId = contributorId;
        this.minSeqNumber = minSeqNumber;
        this.maxSeqNumber = maxSeqNumber;
        this.actorName = actorName;
        this.operation = operation;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    /**
     * @return The fileId;
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @See getFileId();
     * @param fileId The new file id.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    /**
     * @return The contributorId;
     */
    public String getContributorId() {
        return contributorId;
    }
    
    /**
     * @See getContributorId();
     * @param contributorId The new id of the contributor.
     */
    public void setContributorId(String contributorId) {
        this.contributorId = contributorId;
    }
    
    /**
     * @return The minSeqNumber;
     */
    public Long getMinSeqNumber() {
        return minSeqNumber;
    }
    
    /**
     * @See getMinSeqNumberId();
     * @param minSeqNumber The new minimum sequence number.
     */
    public void setMinSeqNumber(Long minSeqNumber) {
        this.minSeqNumber = minSeqNumber;
    }
    
    /**
     * @return The maxSeqNumber;
     */
    public Long getMaxSeqNumber() {
        return maxSeqNumber;
    }
    
    /**
     * @See getMaxSeqNumberId();
     * @param maxSeqNumber The new maximum sequence number.
     */
    public void setMaxSeqNumber(Long maxSeqNumber) {
        this.maxSeqNumber = maxSeqNumber;
    }
    
    /**
     * @return The actorName;
     */
    public String getActorName() {
        return actorName;
    }
    
    /**
     * @See getActorName();
     * @param actorName The new name of the actor.
     */
    public void setActorName(String actorName) {
        this.actorName = actorName;
    }
    
    /**
     * @return The operation;
     */
    public FileAction getOperation() {
        return operation;
    }
    
    /**
     * @See getOperation();
     * @param operation The operation.
     */
    public void setOperation(FileAction operation) {
        this.operation = operation;
    }
    
    /**
     * @return The startDate;
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * @See getStartDate();
     * @param startDate The startDate.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    /**
     * @return The endDate;
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * @See getEndDate();
     * @param endDate The endDate.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
