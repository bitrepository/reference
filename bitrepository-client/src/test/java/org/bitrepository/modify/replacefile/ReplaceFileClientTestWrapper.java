/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id: PutClientTestWrapper.java 598 2011-12-02 17:55:13Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/test/java/org/bitrepository/modify/putfile/PutClientTestWrapper.java $
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
package org.bitrepository.modify.replacefile;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.jaccept.TestEventManager;

/**
 * Wrapper class for a PutFileClient.
 */
public class ReplaceFileClientTestWrapper implements ReplaceFileClient {
    /** The PutClient to wrap. */
    private ReplaceFileClient wrappedReplaceClient;
    /** The manager to monitor the operations.*/
    private TestEventManager testEventManager;

    /**
     * @param putClientInstance The instance to wrap and monitor.
     * @param eventManager The manager to monitor the operations.
     */
    public ReplaceFileClientTestWrapper(ReplaceFileClient putClientInstance, TestEventManager eventManager) {
        this.wrappedReplaceClient = putClientInstance;
        this.testEventManager = eventManager;
    }

    @Override
    public void replaceFile(String collectionID, String fileID, String pillarID,
                            ChecksumDataForFileTYPE checksumForDeleteAtPillar,
            ChecksumSpecTYPE checksumRequestedForDeletedFile, URL url, long sizeOfNewFile,
            ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar, ChecksumSpecTYPE checksumRequestsForNewFile,
            EventHandler eventHandler, String auditTrailInformation) {
        testEventManager.addStimuli("replaceFile(" + fileID + ", " + pillarID + ", " + checksumForDeleteAtPillar + ", "
                + checksumRequestedForDeletedFile + ", " + url + ", " + sizeOfNewFile + ", " 
                + checksumForNewFileValidationAtPillar + ", " + checksumRequestsForNewFile + ", " + eventHandler + ", "
                + auditTrailInformation);
        wrappedReplaceClient.replaceFile(collectionID, fileID, pillarID, checksumForDeleteAtPillar,
                checksumRequestedForDeletedFile,
                url, sizeOfNewFile, checksumForNewFileValidationAtPillar, checksumRequestsForNewFile, eventHandler, 
                auditTrailInformation);
    }
}
