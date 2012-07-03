/*
 * #%L
 * Bitrepository Integrity Client
 * *
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
package org.bitrepository.integrityservice.collector;

import java.util.Collection;

import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.service.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrity information collector that delegates collecting information to the clients.
 * TODO split into two different collectors. One for collecting the file ids and one for the checksums.
 */
public class DelegatingIntegrityInformationCollector implements IntegrityInformationCollector {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The client for retrieving file IDs. */
    private final GetFileIDsClient getFileIDsClient;
    /** The client for retrieving checksums. */
    private final GetChecksumsClient getChecksumsClient;
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    
    /**
     * Constructor.
     * @param getFileIDsClient The client for retrieving file IDs
     * @param getChecksumsClient The client for retrieving checksums
     */
    public DelegatingIntegrityInformationCollector(GetFileIDsClient getFileIDsClient, 
            GetChecksumsClient getChecksumsClient, AuditTrailManager auditManager) {
        this.getFileIDsClient = getFileIDsClient;
        this.getChecksumsClient = getChecksumsClient;
        this.auditManager = auditManager;
    }

    @Override
    public synchronized void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation, 
            EventHandler eventHandler) {
        try {
            auditManager.addAuditEvent("" + fileIDs.getFileID(), "IntegrityService", 
                    "Collecting file ids from '" + pillarIDs + "'", auditTrailInformation, FileAction.INTEGRITY_CHECK);
            getFileIDsClient.getFileIDs(pillarIDs, fileIDs, null, eventHandler, auditTrailInformation);
        } catch (Exception e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }

    @Override
    public synchronized void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType, 
            String auditTrailInformation, EventHandler eventHandler) {
        try {
            auditManager.addAuditEvent("" + fileIDs.getFileID(), "IntegrityService", 
                    "Collecting checksums from '" + pillarIDs + "'", auditTrailInformation, FileAction.INTEGRITY_CHECK);
            getChecksumsClient.getChecksums(pillarIDs, fileIDs, checksumType, null, eventHandler, 
                    auditTrailInformation);
        } catch (Exception e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }
}
