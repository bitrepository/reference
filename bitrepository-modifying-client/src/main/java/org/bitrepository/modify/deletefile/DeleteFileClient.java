/*
 * #%L
 * Bitmagasin modify client
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
package org.bitrepository.modify.deletefile;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.BitrepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.exceptions.OperationFailedException;

/**
 * Interface for the delete client.
 */
public interface DeleteFileClient extends BitrepositoryClient {
    /**
     * Starts the conversation for deleting a file on a given pillar.
     * Takes checksum and checksum specification as argument to validate the file to delete.
     * @param fileId The id of the file to delete.
     * @param pillarId The id of the pillar, where the file should be deleted.
     * @param checksumForPillar The specifications for the checksum of the file.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * In a good case scenario this will give the events: <br/> 
     * IdentifyPillarsRequestSent, PillarIdentified, PillarSelected, RequestSent, Progress, PillarComplete, Complete
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation 
     * call.
     * 
     * @throws OperationFailedException If the operation cannot be instantiated.
     */
    void deleteFile(String fileId, String pillarId, ChecksumDataForFileTYPE checksumForPillar, 
            ChecksumSpecTYPE checksumRequested, EventHandler eventHandler, String auditTrailInformation) 
                    throws OperationFailedException;
    
    /**
     * Starts the conversation for deleting a file on a all pillars.
     * Takes checksum and checksum specification as argument to validate the file to delete.
     * @param fileId The id of the file to delete.
     * @param checksumForPillar The specifications for the checksum of the file for the pillar to validate.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * In a good case scenario this will give the events: <br/> 
     * IdentifyPillarsRequestSent, PillarIdentified (for each pillar), PillarSelected, RequestSent (for each pillar), 
     * Progress (for each pillar), PillarComplete (for each pillar), Complete
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation 
     * call.
     * 
     * @throws OperationFailedException If the operation cannot be instantiated.
     */
    void deleteFileAtAllPillars(String fileId, ChecksumDataForFileTYPE checksumForPillar, 
            ChecksumSpecTYPE checksumRequested, EventHandler eventHandler, String auditTrailInformation) 
                    throws OperationFailedException;
    
}
