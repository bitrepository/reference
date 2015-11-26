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
import org.bitrepository.client.eventhandler.EventHandler;
import org.jaccept.TestEventManager;

/**
 * Wrapper class for a PutFileClient adding test event logging.
 */
public class PutFileClientTestWrapper implements PutFileClient {
    private PutFileClient wrappedPutClient;
    private TestEventManager testEventManager;

    /**
     * Constructor.
     * @param putClientInstance The instance to wrap and monitor.
     * @param eventManager The manager to monitor the operations.
     */
    public PutFileClientTestWrapper(PutFileClient putClientInstance, TestEventManager eventManager) {
        this.wrappedPutClient = putClientInstance;
        this.testEventManager = eventManager;
    }

    @Override
    public void putFile(String collectionID, URL url, String fileID, long sizeOfFile,
                        ChecksumDataForFileTYPE checksumForValidationAtPillar,
            ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation) {
        testEventManager.addStimuli("Calling PutFileWithId(" + url + ", " + fileID + ", " + sizeOfFile + ", "
            + checksumForValidationAtPillar + ", " + checksumRequestsForValidation + ", " + eventHandler + ", "
            + auditTrailInformation + ")");
        wrappedPutClient.putFile(collectionID, url, fileID, sizeOfFile, checksumForValidationAtPillar,
                checksumRequestsForValidation,
                eventHandler, auditTrailInformation);
    }
}
