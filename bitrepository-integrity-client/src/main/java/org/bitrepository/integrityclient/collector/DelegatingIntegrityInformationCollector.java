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
package org.bitrepository.integrityclient.collector;

import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.integrityclient.IntegrityInformationRetrievalException;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
import org.bitrepository.integrityclient.collector.eventhandler.GetChecksumsEventHandler;
import org.bitrepository.integrityclient.collector.eventhandler.GetFileIdsEventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Integrity information collector that delegates collecting information to the clients.
 */
public class DelegatingIntegrityInformationCollector implements IntegrityInformationCollector {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The storage to store data in. */
    private final CachedIntegrityInformationStorage storage;
    /** The client for retrieving file IDs. */
    private final GetFileIDsClient getFileIDsClient;
    /** The eventhandler for the GetFileIDs Operations.*/
    private GetFileIdsEventHandler fileIdsEventHandler;
    /** The client for retrieving checksums. */
    private final GetChecksumsClient getChecksumsClient;
    /** The eventhandler for the GetChecksums Operations.*/
    private GetChecksumsEventHandler checksumsEventHandler;
    
    /**
     * Initialise a delegating integrity information collector.
     *
     * @param storage The storage to store data in.
     * @param getFileIDsClient The client for retrieving file IDs
     * @param getChecksumsClient The client for retrieving checksums
     */
    public DelegatingIntegrityInformationCollector(CachedIntegrityInformationStorage storage,
            GetFileIDsClient getFileIDsClient, GetChecksumsClient getChecksumsClient) {
        this.storage = storage;
        this.getFileIDsClient = getFileIDsClient;
        fileIdsEventHandler = new GetFileIdsEventHandler(storage);
        this.getChecksumsClient = getChecksumsClient;
        checksumsEventHandler = new GetChecksumsEventHandler(storage);
    }

    @Override
    public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation) {
        try {
            getFileIDsClient.getFileIDs(pillarIDs, fileIDs, null, fileIdsEventHandler, auditTrailInformation);
        } catch (OperationFailedException e) {
            log.warn("Could not retrieve the file ids '" + fileIDs + "' from '" + pillarIDs + "'", e);
        } catch (Throwable e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType, 
            String auditTrailInformation) {
        try {
            getChecksumsClient.getChecksums(pillarIDs, fileIDs, checksumType, null, null, auditTrailInformation);
        } catch (OperationFailedException e) {
            log.warn("Could not retrieve the checksum '" + fileIDs + "' from '" + pillarIDs + "' with spec '" 
                    + checksumType + "'", e);
        } catch (Throwable e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }
}
