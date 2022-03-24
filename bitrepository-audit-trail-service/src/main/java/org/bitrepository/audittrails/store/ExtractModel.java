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

import org.bitrepository.bitrepositoryelements.FileAction;

import java.util.Date;

/**
 * Container for the extraction of data from the audit trail database.
 */
class ExtractModel {
    /**
     * @see #getFileId().
     */
    private String fileID;
    /**
     * @see #getCollectionId().
     */
    private String collectionID;
    /**
     * @see #getContributorId().
     */
    private String contributorId;
    /**
     * @see #getMinSeqNumber().
     */
    private Long minSeqNumber;
    /**
     * @see #getMaxSeqNumber().
     */
    private Long maxSeqNumber;
    /**
     * @see #getActorName().
     */
    private String actorName;
    /**
     * @see #getOperation().
     */
    private FileAction operation;
    /**
     * @see #getStartDate().
     */
    private Date startDate;
    /**
     * @see #getEndDate().
     */
    private Date endDate;
    /**
     * @see #getFingerprint().
     */
    private String fingerprint;
    /**
     * @see #getOperationID().
     */
    private String operationID;

    public ExtractModel() {
    }

    /**
     * @return The fileID;
     */
    public String getFileId() {
        return fileID;
    }

    /**
     * @param fileID The new file id.
     */
    public void setFileId(String fileID) {
        this.fileID = fileID;
    }

    /**
     * @return The collectionID;
     */
    public String getCollectionId() {
        return collectionID;
    }

    /**
     * @param collectionID The new collection id.
     */
    public void setCollectionId(String collectionID) {
        this.collectionID = collectionID;
    }

    /**
     * @return The contributorId;
     */
    public String getContributorId() {
        return contributorId;
    }

    /**
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
     * @param endDate The endDate.
     * @see #getEndDate();
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return The fingerprint of the certificate
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * @param fingerprint the fingerprint
     * @see #getFingerprint();
     */
    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * @return The ID of the operation
     */
    public String getOperationID() {
        return operationID;
    }

    /**
     * @param operationID The ID of the operation
     * @see #getOperationID();
     */
    public void setOperationID(String operationID) {
        this.operationID = operationID;
    }
}
