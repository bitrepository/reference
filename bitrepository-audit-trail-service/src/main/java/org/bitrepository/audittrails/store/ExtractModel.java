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

import java.time.Instant;
import java.util.Date;

/**
 * Container for the extraction of data from the audit trail database.
 */
class ExtractModel {
    /**
     * @see #getFileID().
     */
    private String fileID;
    /**
     * @see #getCollectionID().
     */
    private String collectionID;
    /**
     * @see #getContributorID().
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
    private Instant startDate;
    /**
     * @see #getEndDate().
     */
    private Instant endDate;
    /**
     * @see #getFingerprint().
     */
    private String fingerprint;
    /**
     * @see #getOperationID().
     */
    private String operationID;

    private Integer maxAuditTrails;

    /**
     * @return The fileID;
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * @param fileID The new file id.
     */
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    /**
     * @return The collectionID;
     */
    public String getCollectionID() {
        return collectionID;
    }

    /**
     * @param collectionID The new collection id.
     */
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }

    /**
     * @return The contributorId;
     */
    public String getContributorID() {
        return contributorId;
    }

    /**
     * @param contributorId The new id of the contributor.
     */
    public void setContributorID(String contributorId) {
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
    public Instant getStartInstant() {
        return startDate;
    }

    /**
     * @return The startDate;
     * @deprecated Use {@link #getStartInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getStartDate() {
        return startDate != null ? Date.from(startDate) : null;
    }

    /**
     * @param startDate The startDate.
     */
    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    /**
     * @param startDate The startDate.
     * @deprecated Use {@link #setStartDate(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? startDate.toInstant() : null;
    }

    /**
     * @return The endDate;
     */
    public Instant getEndInstant() {
        return endDate;
    }

    /**
     * @return The endDate;
     * @deprecated Use {@link #getEndInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getEndDate() {
        return endDate != null ? Date.from(endDate) : null;
    }

    /**
     * @param endDate The endDate.
     * @see #getEndDate();
     */
    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    /**
     * @param endDate The endDate.
     * @see #getEndDate();
     * @deprecated Use {@link #setEndDate(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? endDate.toInstant() : null;
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

    /**
     * @return The max number of audit trails to fetch from database or null for unlimited
     */
    public Integer getMaxAuditTrails() {
        return maxAuditTrails;
    }

    /**
     * @param maxAuditTrails The max number of audit trails to fetch from database or null for unlimited
     */
    public void setMaxAuditTrails(Integer maxAuditTrails) {
        this.maxAuditTrails = maxAuditTrails;
    }

}
