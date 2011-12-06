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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;

/**
 * Interface for the delete client.
 */
public interface DeleteFileClient {
    /**
     * Starts the conversation for deleting a file on a given pillar.
     * Takes checksum and checksum specification as argument to validate the file to delete.
     * @param fileId The id of the file to delete.
     * @param pillarId The id of the pillar, where the file should be deleted.
     * @param checksum The checksum of the file.
     * @param checksumForPillar The specifications for the checksum of the file.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation call.
     * 
     * @throws OperationFailedException If the operation cannot be instantiated.
     */
    void deleteFile(String fileId, String pillarId, String checksum, ChecksumSpecTYPE checksumForPillar, 
            ChecksumSpecTYPE checksumRequested, EventHandler eventHandler, String auditTrailInformation) 
                    throws OperationFailedException;
    
    /**
     * Method to perform a graceful shutdown of the client.
     */
    void shutdown();
}
