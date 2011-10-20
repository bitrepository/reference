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
import java.util.Collection;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;

/**
 * The <code>GetChecksumsClient</code> is used as a handle for the Bit Repository getChecksums operation.
 */
public interface GetChecksumsClient {
    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The method will block until the 
     * result has been uploaded by the pillar or a timeout has occurred.
     *
     * @param pillarIDs The list of IDs for the pillars, where the checksum should be retrieved from.
     * @param fileIDs Defines the set of files.
     * @param checksumSpec Specification of how the checksums should be calculated.
     * @param addressForResult [OPTIONAL] The address to upload the calculated checksums to. If this is null, then the
     * results will be retrieved through the message.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * @return The map between the pillars and their results.
     * 
     * @throws NoPillarFoundException The identify request didn't cause the pillar to respond.  
     * @throws OperationTimeOutException The get checksum request timeout.  
     * @throws OperationFailedException The operation failed.
     */
    public Map<String, ResultingChecksums> getChecksumsBlocking(Collection<String> pillarIDs, FileIDs fileIDs, 
            ChecksumSpecTYPE checksumSpec, URL addressForResult, EventHandler eventHandler, 
            String auditTrailInformation) throws NoPillarFoundException, OperationTimeOutException, 
            OperationFailedException;
    
    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The method will not block until 
     * the conversation has finished. It will only initiate the conversation.
     *
     * @param pillarIDs The list of IDs for the pillars, where the checksum should be retrieved from.
     * @param fileIDs Defines the set of files.
     * @param checksumSpec Specification of how the checksums should be calculated.
     * @param addressForResult [OPTIONAL] The address to upload the calculated checksums to. If this is null, then the
     * results will be retrieved through the message.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation call. 
     * 
     * @throws OperationFailedException If the conversation cannot be initiated.
     */
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, 
            URL addressForResult, EventHandler eventHandler, String auditTrailInformation)
            throws OperationFailedException;
}
