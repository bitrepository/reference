/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

package org.bitrepository.pillar.integration.func.putfile;

import java.lang.reflect.Method;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.func.PillarRobustnessTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;

public class PutFileRequestRobustnessTest extends PillarRobustnessTest {
    protected PutFileMessageFactory msgFactory;
    private String pillarDestination;
    private static final Long DEFAULT_FILE_SIZE = 10L;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        pillarDestination = lookupPutFileDestination();
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
    }


    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createPutFileRequest(TestFileHelper.getDefaultFileChecksum(), null,
                DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, DEFAULT_FILE_SIZE);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(PutFileFinalResponse.class);
    }

    public String lookupPutFileDestination() {
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                TestFileHelper.DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class).getReplyTo();
    }
}
