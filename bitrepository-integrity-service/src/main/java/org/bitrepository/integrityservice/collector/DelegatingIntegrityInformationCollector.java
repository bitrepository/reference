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

import java.net.URL;
import java.util.Collection;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.putfile.PutFileClient;
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
    /** The client for performing GetFile operations.*/
    private final GetFileClient getFileClient;
    /** The client for putting files.*/
    private final PutFileClient putFileClient;
    /** The audit trail manager.*/
    
    /**
     * @param getFileIDsClient The client for retrieving file IDs
     * @param getChecksumsClient The client for retrieving checksums.
     */
    public DelegatingIntegrityInformationCollector(
            GetFileIDsClient getFileIDsClient,
            GetChecksumsClient getChecksumsClient,
            GetFileClient getFileClient,
            PutFileClient putFileClient) {
        this.getFileIDsClient = getFileIDsClient;
        this.getChecksumsClient = getChecksumsClient;
        this.getFileClient = getFileClient;
        this.putFileClient = putFileClient;
    }

    @Override
    public synchronized void getFileIDs(String collectionID, Collection<String> pillarIDs, String auditTrailInformation, 
            ContributorQuery[] queries, EventHandler eventHandler) {
        try {
            getFileIDsClient.getFileIDs(collectionID, queries, null, null, eventHandler);
        } catch (Exception e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }

    @Override
    public synchronized void getChecksums(String collectionID, Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, 
            String auditTrailInformation, ContributorQuery[] queries, EventHandler eventHandler) {
        try {
            getChecksumsClient.getChecksums(collectionID, queries, null, checksumType, null, eventHandler,
                    auditTrailInformation);
        } catch (Exception e) {
            log.error("Unexpected failure!", e);
        }
    }
    
    @Override
    public synchronized void getFile(String collectionID, String fileId, URL uploadUrl, EventHandler eventHandler, 
            String auditTrailInformation) {
        try {
            getFileClient.getFileFromFastestPillar(collectionID, fileId, null, uploadUrl, eventHandler, 
                    auditTrailInformation);
        } catch (Exception e) {
            log.error("Unexpected failure!", e);
        }
    }
    
    @Override
    public synchronized void putFile(String collectionID, String fileId, URL uploadUrl, 
            ChecksumDataForFileTYPE checksumValidationData, EventHandler eventHandler, String auditTrailInformation) {
        try {
            putFileClient.putFile(collectionID, uploadUrl, fileId, 0, checksumValidationData, null, eventHandler, 
                    auditTrailInformation);
        } catch (Exception e) {
            log.error("Unexpected failure!", e);
        }
    }
}
