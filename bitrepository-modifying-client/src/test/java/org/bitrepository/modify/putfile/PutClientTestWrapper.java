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
package org.bitrepository.modify.putfile;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.jaccept.TestEventManager;

/**
 * Wrapper class for a PutFileClient.
 */
public class PutClientTestWrapper implements PutFileClient {
    /** The PutClient to wrap. */
    private PutFileClient wrappedPutClient;
    /** The manager to monitor the operations.*/
    private TestEventManager testEventManager;

    /**
     * Constructor.
     * @param putClientInstance The instance to wrap and monitor.
     * @param eventManager The manager to monitor the operations.
     */
    public PutClientTestWrapper(PutFileClient putClientInstance, TestEventManager eventManager) {
        this.wrappedPutClient = putClientInstance;
        this.testEventManager = eventManager;
    }

    @Override
    public void putFileWithId(URL url, String fileId, long fileSize, EventHandler eventHandler) {
        testEventManager.addStimuli("Calling PutFileWithId(" + url + ", " + fileId + ", " + fileSize + ", eventHandler)");
        wrappedPutClient.putFileWithId(url, fileId, fileSize, eventHandler);
    }
    
    @Override
    public void putFileWithId(URL url, String fileId, long fileSize) throws OperationFailedException {
        testEventManager.addStimuli("Calling PutFileWithId(" + url + ", " + fileId + ", " + fileSize + ", eventHandler)");
        wrappedPutClient.putFileWithId(url, fileId, fileSize);
    }

    @Override
    public void putFile(URL url, String fileId, long sizeOfFile, ChecksumDataForFileTYPE checksumForValidationAtPillar,
            ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation)
            throws OperationFailedException {
        testEventManager.addStimuli("Calling PutFileWithId(" + url + ", " + fileId + ", " + sizeOfFile + ", " 
            + checksumForValidationAtPillar + ", " + checksumRequestsForValidation + ", " + eventHandler + ", "
            + auditTrailInformation + ")");
        wrappedPutClient.putFile(url, fileId, sizeOfFile, checksumForValidationAtPillar, checksumRequestsForValidation, 
                eventHandler, auditTrailInformation);
    }
    
    @Override
    public void shutdown() {
        wrappedPutClient.shutdown();
    }
}
