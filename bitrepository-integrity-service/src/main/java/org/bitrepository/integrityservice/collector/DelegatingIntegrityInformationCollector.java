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
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrity information collector that delegates collecting information to the clients.
 */
public class DelegatingIntegrityInformationCollector implements IntegrityInformationCollector {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The client for retrieving file IDs. */
    private final GetFileIDsClient getFileIDsClient;
    /** The client for retrieving checksums. */
    private final GetChecksumsClient getChecksumsClient;
    
    /**
     * Constructor.
     * @param getFileIDsClient The client for retrieving file IDs
     * @param getChecksumsClient The client for retrieving checksums
     */
    public DelegatingIntegrityInformationCollector(GetFileIDsClient getFileIDsClient, 
            GetChecksumsClient getChecksumsClient) {
        this.getFileIDsClient = getFileIDsClient;
        this.getChecksumsClient = getChecksumsClient;
    }

    @Override
    public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation, 
            EventHandler eventHandler) {
        try {
            getFileIDsClient.getFileIDs(pillarIDs, fileIDs, null, eventHandler, auditTrailInformation);
        } catch (OperationFailedException e) {
            log.warn("Could not retrieve the file ids '" + fileIDs + "' from '" + pillarIDs + "'", e);
        } catch (Exception e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType, 
            String auditTrailInformation, EventHandler eventHandler) {
        try {
            getChecksumsClient.getChecksums(pillarIDs, fileIDs, checksumType, null, eventHandler, 
                    auditTrailInformation);
        } catch (OperationFailedException e) {
            log.warn("Could not retrieve the checksum '" + fileIDs + "' from '" + pillarIDs + "' with spec '" 
                    + checksumType + "'", e);
        } catch (Exception e) {
            // Barrier
            log.error("Unexpected failure!", e);
        }
    }
}
