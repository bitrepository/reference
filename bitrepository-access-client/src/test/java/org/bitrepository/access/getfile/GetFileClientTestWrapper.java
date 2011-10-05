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
package org.bitrepository.access.getfile;

import java.net.URL;

import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;
import org.jaccept.TestEventManager;

/**
 * Wraps the <code>GetFileClient</code> adding test event logging and functionality for handling blocking calls.
 */
public class GetFileClientTestWrapper implements GetFileClient {
    private GetFileClient createGetFileClient;
    private TestEventManager testEventManager;

    public GetFileClientTestWrapper(GetFileClient createGetFileClient,
            TestEventManager testEventManager) {
        this.createGetFileClient = createGetFileClient;
        this.testEventManager = testEventManager;
    }

    @Override
    public void getFileFromFastestPillar(String fileId, URL uploadUrl, EventHandler eventHandler) {
        testEventManager.addStimuli("Calling getFileFromFastestPillar(" + fileId + ", " + uploadUrl + ")");
        createGetFileClient.getFileFromFastestPillar(fileId, uploadUrl, eventHandler);
    }

    @Override
    public void getFileFromFastestPillar(String fileId, URL uploadUrl) 
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        testEventManager.addStimuli("Calling getFileFromFastestPillar(" + fileId + ", " + uploadUrl + ")");
        createGetFileClient.getFileFromFastestPillar(fileId, uploadUrl);
    }

    @Override
    public void getFileFromSpecificPillar(String fileId, URL uploadUrl, String pillarId, EventHandler eventHandler) {
        testEventManager.addStimuli("Calling getFileFromSpecificPillar(" + 
                fileId + ", " + uploadUrl + ", " + pillarId + ")");
        createGetFileClient.getFileFromSpecificPillar(fileId, uploadUrl, pillarId, eventHandler);
    }

    @Override
    public void getFileFromSpecificPillar(String fileId, URL uploadUrl, String pillarId) 
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        testEventManager.addStimuli("Calling getFileFromSpecificPillar(" + fileId + ", " + uploadUrl+ ", " + pillarId + ")");
        createGetFileClient.getFileFromSpecificPillar(fileId, uploadUrl, pillarId);
    }
}
