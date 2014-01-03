/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.service.audit;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.FileAction;

/**
 * The interface for the audit trail handler.
 */
public interface AuditTrailManager {
    /**
     * Adds an audit trail event to the manager.
     * @param collectionId The id of the collection for whom the audit applies.
     * @param fileId The id of the file, where the operation has been performed.
     * Use the argument null for indicating all file ids. 
     * @param actor The name of the actor.
     * @param info Information about the reason for the audit trail to be logged.
     * @param auditTrail The string for the audit trail information from the message performing the operation.
     * @param operation The performed operation.
     * @param operationID The conversationID of the operation
     * @param certificateID The certificate fingerprint for the message.
     */
    void addAuditEvent(String collectionId, String fileId, String actor, String info, String auditTrail,
                       FileAction operation, String operationID, String certificateID);
    
    /**
     * Method for extracting all the audit trails.
     * @param collectionId The id of the collection for whom the audit applies.
     * @param fileId [OPTIONAL] The id of the file to request audits for.
     * @param minSeqNumber [OPTIONAL] The lower sequence number requested. 
     * @param maxSeqNumber [OPTIONAL] The upper sequence number requested.
     * @param minDate [OPTIONAL] The earliest date requested.
     * @param maxDate [OPTIONAL] The newest date requested.
     * @param maxNumberOfResults [OPTIONAL] The maximum number of results.
     * @return The audit trails corresponding to the requested arguments.
     */
    AuditTrailDatabaseResults getAudits(String collectionId, String fileId, Long minSeqNumber, Long maxSeqNumber, Date minDate, 
            Date maxDate, Long maxNumberOfResults);
}
