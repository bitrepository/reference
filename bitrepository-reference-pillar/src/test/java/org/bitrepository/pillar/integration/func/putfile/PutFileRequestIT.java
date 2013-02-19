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
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PutFileRequestIT extends PillarFunctionTest {
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

    @Test( groups = {"pillar-integration-test"})
    public void normalPutFile() {
        addDescription("Tests a normal PutFile sequence");
        addStep("Send a putFile request to " + testConfiguration.getPillarUnderTestID(), "");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(putRequest);

        addStep("Await the FinalResponse", "Check all the parameteres are correct.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getCorrelationID(), putRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
    }

    @Test( groups = {"pillar-integration-test"})
    public void operationAcceptedProgressResponse() {
        addDescription("Tests that a pillar sends a progress response after receiving a request");
        addStep("Send a putFile request to " + testConfiguration.getPillarUnderTestID(), "");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                TestFileHelper.getDefaultFileChecksum(), null, DEFAULT_DOWNLOAD_FILE_ADDRESS, testSpecificFileID, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(putRequest);

        addStep("Await the ProgressResponse", "Check all the parameteres are correct.");
        PutFileProgressResponse progressResponse = clientReceiver.waitForMessage(PutFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), putRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);

        addStep("Await the FinalResponse", "");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
    }


    public String lookupPutFileDestination() {
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                TestFileHelper.DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class).getReplyTo();
    }
}
