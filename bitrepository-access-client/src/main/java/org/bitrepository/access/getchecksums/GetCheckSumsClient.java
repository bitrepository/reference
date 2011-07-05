/*
 * #%L
 * Bitrepository Access
 * 
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
package org.bitrepository.access.getchecksums;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;

public interface GetCheckSumsClient {

    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The progress of the operation can be 
     * followed though the supplied event handler.
     *
     * @param pillarId The id of pillar, where the file should be retrieved from.
     * @param fileIDs Defines the set of files.
     * @param checksumSpec Specification of how the checksums should be calculated.
     * @param eventHandler The handler which should receive notifications of the events occurring in connection with 
     * the pillar communication. The result of this operation can be retrieved from the last complete event, which will 
     * be of type <code>GetChecksumsCompleteEvent</code>.
     */
    public void getChecksums(String pillarID, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, 
            EventHandler eventHandler);
    
    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The method will block until the 
     * result has been returned by the pillar or a timeout has occurred.
     *
     * @param pillarId The id of pillar, where the file should be retrieved from.
     * @param fileIDs Defines the set of files.
     * @param checksumSpec Specification of how the checksums should be calculated.
     * 
     * @throws NoPillarFoundException The identify request didn't cause the pillar to respond.  
     * @throws OperationTimeOutException The get checksum request timeout.  
     * @throws OperationFailedException The operation failed.
     */
    public ResultingChecksums getChecksums(String pillarID, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec)
    throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;
    
    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The progress of the operation can be 
     * followed though the supplied event handler.
     *
     * @param pillarId The id of pillar, where the file should be retrieved from.
     * @param fileIDs Defines the set of files.
     * @param checksumSpec Specification of how the checksums should be calculated.
     * @param addressForResult The address to upload the calculated checksums to.
     * @param eventHandler The handler which should receive notifications of the events occurring in connection with 
     * the pillar communication. 
     */
    public void getChecksums(String pillarID, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, 
            URL addressForResult, EventHandler eventHandler);
    
    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The method will block until the 
     * result has been uploaded by the pillar or a timeout has occurred.
     *
     * @param pillarId The id of pillar, where the file should be retrieved from.
     * @param fileIDs Defines the set of files.
     * @param checksumSpec Specification of how the checksums should be calculated.
     * @param addressForResult The address to upload the calculated checksums to.
     * 
     * @throws NoPillarFoundException The identify request didn't cause the pillar to respond.  
     * @throws OperationTimeOutException The get checksum request timeout.  
     * @throws OperationFailedException The operation failed.
     */
    public void getChecksums(String pillarID, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, 
            URL addressForResult)
    throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;
}
