/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id: GetFileClientTestWrapper.java 240 2011-07-28 07:55:25Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/test/java/org/bitrepository/access/getfile/GetFileClientTestWrapper.java $
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
import java.util.Arrays;

import org.bitrepository.bitrepositoryelements.ChecksumSpecs;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;
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
    public void getChecksums(String[] pillarIDs, FileIDs fileIDs,
            ChecksumSpecs checksumSpec, EventHandler eventHandler) {
        testEventManager.addStimuli("Calling getChecksums(" + Arrays.asList(pillarIDs) + ", " + fileIDs.getFileID() 
                + ", " + checksumSpec + ")");
        getChecksumsClientInstance.getChecksums(pillarIDs, fileIDs, checksumSpec, eventHandler);
    }

    @Override
    public ResultingChecksums getChecksums(String[] pillarIDs, FileIDs fileIDs,
            ChecksumSpecs checksumSpec) throws NoPillarFoundException,
            OperationTimeOutException, OperationFailedException {
        testEventManager.addStimuli("Calling getChecksums(" + Arrays.asList(pillarIDs) + ", " + fileIDs.getFileID() 
                + ", " + checksumSpec + ")");
        return getChecksumsClientInstance.getChecksums(pillarIDs, fileIDs, checksumSpec);
    }

    @Override
    public void getChecksums(String[] pillarIDs, FileIDs fileIDs,
            ChecksumSpecs checksumSpec, URL addressForResult,
            EventHandler eventHandler) {
        testEventManager.addStimuli("Calling getChecksums(" + Arrays.asList(pillarIDs) + ", " + fileIDs.getFileID() 
                + ", " + checksumSpec + ", " + addressForResult + ")");
        getChecksumsClientInstance.getChecksums(pillarIDs, fileIDs, checksumSpec, addressForResult, eventHandler);
    }

    @Override
    public void getChecksums(String[] pillarIDs, FileIDs fileIDs,
            ChecksumSpecs checksumSpec, URL addressForResult)
            throws NoPillarFoundException, OperationTimeOutException,
            OperationFailedException {
        testEventManager.addStimuli("Calling getChecksums(" + Arrays.asList(pillarIDs) + ", " + fileIDs.getFileID() 
                + ", " + checksumSpec + ", " + addressForResult + ")");
        getChecksumsClientInstance.getChecksums(pillarIDs, fileIDs, checksumSpec, addressForResult);        
    }
}
