/*
 * #%L
 * Bitrepository Access Client
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
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.exceptions.OperationFailedException;
import org.jaccept.TestEventManager;

/**
 * Wraps the <code>GetFileClient</code> adding test event logging and functionality for handling blocking calls.
 */
public class GetChecksumsClientTestWrapper implements GetChecksumsClient {
    private GetChecksumsClient getChecksumsClientInstance;
    private TestEventManager testEventManager;

    public GetChecksumsClientTestWrapper(GetChecksumsClient createGetChecksumsClient,
            TestEventManager testEventManager) {
        this.getChecksumsClientInstance = createGetChecksumsClient;
        this.testEventManager = testEventManager;
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, 
            URL addressForResult, EventHandler eventHandler, String auditTrailInformation)
            throws OperationFailedException {
        testEventManager.addStimuli("Calling getChecksums(" + pillarIDs + ", " + fileIDs.getFileID() 
                + ", " + checksumSpec + ", " + addressForResult + ", " + eventHandler + ", " 
                + auditTrailInformation + ")");
        getChecksumsClientInstance.getChecksums(pillarIDs, fileIDs, checksumSpec, addressForResult, eventHandler, 
                auditTrailInformation);        
    }
    
    @Override
    public void shutdown() {
        // Empty
    }
}
