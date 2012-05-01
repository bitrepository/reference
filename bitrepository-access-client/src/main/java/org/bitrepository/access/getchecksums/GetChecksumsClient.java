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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.BitrepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.exceptions.OperationFailedException;

/**
 * The <code>GetChecksumsClient</code> is used as a handle for the Bit Repository getChecksums operation.
 */
public interface GetChecksumsClient extends BitrepositoryClient {
    /**
     * Method for retrieving a checksums for a set of files from a specific pillar. The method will not block until 
     * the conversation has finished. It will only initiate the conversation.
     * <br/>
     * Since every pillar cannot upload their checksums to the same URL, it is extended with the pillarId for the given
     * pillar, e.g.: 'http://upload.url/mypath' + '-pillarId'.
     * <br/>
     * The results are returned through as an special event through the eventHandler, 
     * the ChecksumsCompletePillarCompete. 
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
