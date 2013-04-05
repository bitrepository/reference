/*
 * #%L
 * Bitrepository Reference Pillar
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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PutFileRequestIT extends DefaultPillarOperationTest {
    protected PutFileMessageFactory msgFactory;
    private String pillarDestination;
    private String testSpecificFileID;
    private static final Long DEFAULT_FILE_SIZE = 10L;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        pillarDestination = lookupPutFileDestination();
        testSpecificFileID = method.getName() + "File-" + createDate();
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void normalPutFileTest() {
        addDescription("Tests a normal PutFile sequence");
        addStep("Send a putFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should generate a OPERATION_ACCEPTED_PROGRESS progress response followed by a " +
                "OPERATION_COMPLETED final response");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(putRequest);

        PutFileProgressResponse progressResponse = clientReceiver.waitForMessage(PutFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), putRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);

        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getCorrelationID(), putRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
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

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(PutFileFinalResponse.class);
    }

    public String lookupPutFileDestination() {
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                TestFileHelper.DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class).getReplyTo();
    }
}
