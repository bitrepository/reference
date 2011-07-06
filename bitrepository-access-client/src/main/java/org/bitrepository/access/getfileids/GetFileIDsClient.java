/*
 * #%L
 * Bitrepository Access Client
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
package org.bitrepository.access.getfileids;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Interface for GetFileIDs client for external use of GetFileIDs functionality.
 * Also the interface used internally by the Integrity Client.
 */
public interface GetFileIDsClient {
    /**
     * Get specified fileIds from the fastest pillar holding the specified bit repository collection.
     * The method will block until the result has been retrieved.
     * @param bitRepositoryCollectionID The ID of a collection.
     * @param fileIDs Requested file IDs.
     * @return resulting file IDs
     */
    ResultingFileIDs getFileIDs(String bitRepositoryCollectionID, FileIDs fileIDs)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;

    /**
     * Get specified fileIds from the fastest pillar holding the specified bit repository collection.
     * The method will block until the result has been uploaded to the requested URL.
     * @param bitRepositoryCollectionID The ID of a collection.
     * @param fileIDs Requested file IDs.
     * @param uploadUrl url where resulting file IDs should be uploaded
     */
    void getFileIDs(String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;

    /**
     * Get specified fileIds from the fastest pillar holding the specified bit repository collection.
     * The method will return as soon as the communication has been setup.
     * @param bitRepositoryCollectionID The ID of a collection.
     * @param fileIDs Requested file IDs.
     * @param uploadUrl url where resulting file IDs should be uploaded
     * @param eventHandler The handler which should receive notifications of the progress events.
     */
    void getFileIDs(String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl,
                                     EventHandler eventHandler);

    /**
     * Get specified fileIds from specified pillar holding the specified bit repository collection.
     * The method will block until the result has been retrieved.
     * @param pillarID ID of the pillar where file IDs are requested from
     * @param bitRepositoryCollectionID The ID of a collection.
     * @param fileIDs Requested file IDs.
     * @return resulting file IDs
     */
    ResultingFileIDs getFileIDsFromPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;

    /**
     * Get specified fileIds from specified pillar holding the specified bit repository collection.
     * The method will block until the result has been uploaded to the requested URL.
     * @param pillarID ID of the pillar where file IDs are requested from
     * @param bitRepositoryCollectionID The ID of a collection.
     * @param fileIDs Requested file IDs.
     * @param uploadUrl url where resulting file IDs should be uploaded
     */
    void getFileIDsFromPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;

    /**
     * Get specified fileIds from specified pillar holding the specified bit repository collection.
     * The method will return as soon as the communication has been setup.
     * @param pillarID ID of the pillar where file IDs are requested from
     * @param bitRepositoryCollectionID The ID of a collection.
     * @param fileIDs Requested file IDs.
     * @param uploadUrl url where resulting file IDs should be uploaded
     * @param eventHandler The handler which should receive notifications of the progress events.
     */
    void getFileIDsFromPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl,
                                     EventHandler eventHandler);

}
