/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id: GetFileClientTestWrapper.java 209 2011-07-04 19:38:34Z mss $
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
package org.bitrepository.modify.putfile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.put.MutablePutFileClientSettings;
import org.bitrepository.modify.put.PutClient;
import org.bitrepository.modify.put.SimplePutClient;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the components of the PutFileClient.
 */
public class PutFileClientComponentTest extends DefaultFixtureClientTest {
    /** The constant for the pillar id.*/
//    private static final String PILLAR_ID = "PutStore";
    
    /** The settings. */
    MutablePutFileClientSettings putSettings;
    /** The instance to store the test files.*/
    private TestFileStore filestore;
    
    private TestPutFileMessageFactory messageFactory;
    
    private File testFile;
    
    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        putSettings = new MutablePutFileClientSettings(settings);
        putSettings.setAuditTrailInformation(null);
        putSettings.setPutFileDefaultTimeout(1000L);
        putSettings.setMessageBusConfiguration(messageBusConfigurations);
        
        if(useMockupPillar()) {
            messageFactory = new TestPutFileMessageFactory(putSettings.getBitRepositoryCollectionID());
            filestore = new TestFileStore(PILLAR1_ID);
        }

        testFile = new File("src/test/resources/test-files/", DEFAULT_FILE_ID);
    }
    
    @Test(groups={"regressiontest"})
    public void verifyPutClientFromFactory() {
        addDescription("Testing the initialization through the ModifyComponentFactory.");
        addStep("Use the ModifyComponentFactory to instantiate a PutFileClient.", 
                "It should be an instance of SimplePutFileClient");
        PutClient pfc = ModifyComponentFactory.getInstance().retrievePutClient(putSettings);
        Assert.assertTrue(pfc instanceof SimplePutClient, "The PutFileClient '" + pfc + "' should be instance of '" 
                + SimplePutClient.class.getName() + "'");
    }
    
    @Test(groups={"regressiontest"})
    public void putClientTester() throws Exception {
        addDescription("Tests the PutClient. Makes a whole conversation for the put client, though only "
                + "with one pillar.");
        addStep("Initialise the number of pillars and the PutClient.", "Should be OK.");
        
        putSettings.setPillarIDs(new String[]{PILLAR1_ID});
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        PutClient putClient = new PutClientTestWrapper(
                ModifyComponentFactory.getInstance().retrievePutClient(putSettings), 
                testEventManager);

        addStep("Ensure that the test-file is placed on the HTTP server.", "Should be removed an reuploaded.");
        URL downloadUrl = new URL("http://sandkasse-01.kb.dk/dav/test.txt");
        
        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
                "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
        putClient.putFileWithId(downloadUrl, DEFAULT_FILE_ID, new Long(testFile.length()), testEventHandler);
        
        IdentifyPillarsForPutFileRequest receivedIdentifyRequestMessage = null;
        if(useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForPutFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    messageFactory.createIdentifyPillarsForPutFileRequest(
                            receivedIdentifyRequestMessage.getCorrelationID(),
                            receivedIdentifyRequestMessage.getReplyTo(),
                            receivedIdentifyRequestMessage.getTo()
                            ));
        }

        addStep("Make response for the pillar.", "The client should then send the actual PutFileRequest.");
        
        PutFileRequest receivedPutFileRequest = null;
        if(useMockupPillar()) {
            IdentifyPillarsForPutFileResponse identifyResponse = messageFactory
                    .createIdentifyPillarsForPutFileResponse(
                            receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedPutFileRequest = pillar1Destination.waitForMessage(PutFileRequest.class);
            Assert.assertEquals(receivedPutFileRequest, 
                    messageFactory.createPutFileRequest(
                            PILLAR1_ID, pillar1DestinationId,
                            receivedPutFileRequest.getReplyTo(),
                            receivedPutFileRequest.getCorrelationID(),
                            receivedPutFileRequest.getFileAddress(),
                            receivedPutFileRequest.getFileSize()
                            ));
        }
        
        addStep("Validate the steps of the PutClient by going through the events.", "Should be 'PillarIdentified', "
                + "'PillarSelected' and 'RequestSent'");
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a progress response to the PutClient.", "Should be caught by the event handler.");
        if(useMockupPillar()) {
            PutFileProgressResponse putFileProgressResponse = messageFactory.createPutFileProgressResponse(
                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileProgressResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("Send a final response message to the PutClient.", 
                "Should be caught by the event handler. First a PartiallyComplete, then a Complete.");
        if(useMockupPillar()) {
            PutFileFinalResponse putFileFinalResponse = messageFactory.createPutFileFinalResponse(
                    receivedPutFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(putFileFinalResponse);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PartiallyComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
    }
}
