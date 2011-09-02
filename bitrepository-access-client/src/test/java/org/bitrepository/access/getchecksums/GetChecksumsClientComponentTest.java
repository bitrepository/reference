/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id: GetFileClientComponentTest.java 250 2011-08-03 08:44:19Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/test/java/org/bitrepository/access/getfile/GetFileClientComponentTest.java $
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs.ChecksumSpecsItems;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetChecksumsClientComponentTest extends DefaultFixtureClientTest {
    private TestGetChecksumsMessageFactory testMessageFactory;
    private TestFileStore pillar1FileStore;
    private TestFileStore pillar2FileStore;
    private MutableGetChecksumsClientSettings getChecksumsClientSettings;
    
    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        getChecksumsClientSettings = new MutableGetChecksumsClientSettings(settings);
        getChecksumsClientSettings.setGetChecksumsDefaultTimeout(1000);

        getChecksumsClientSettings.setPillarIDs(new String[]{PILLAR1_ID});
//        System.setProperty("useMockupPillar", "false");
//        getChecksumsClientSettings.setBitRepositoryCollectionID("JOLF-TEST");
//        getChecksumsClientSettings.setBitRepositoryCollectionID("I DO NOT KNOW YOUR COLLECTION ID!!!");
//        getChecksumsClientSettings.setBitRepositoryCollectionTopicID("JOLF-TESTING-TOPIC");
//        getChecksumsClientSettings.setPillarIDs(new String[]{"ReferencePillar_1", "ReferencePillar_2"});
//        getChecksumsClientSettings.setPillarIDs(new String[]{"ReferencePillar_1"});
//        MessageBusConfiguration busConf = new MessageBusConfiguration();
//        busConf.setId("distribueret-test-messagebus");
//        busConf.setUrl("failover:(tcp://sandkasse-01.kb.dk:60001,tcp://sandkasse-01.kb.dk:60002,tcp://sandkasse-01.kb.dk:60003)");
//        messageBusConfigurations.setPrimaryMessageBusConfiguration(busConf);

        getChecksumsClientSettings.setMessageBusConfiguration(messageBusConfigurations);

        if (useMockupPillar()) {
            testMessageFactory = new TestGetChecksumsMessageFactory(settings.getBitRepositoryCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1", TestFileStore.DEFAULT_TEST_FILE);
            pillar2FileStore = new TestFileStore("Pillar2", TestFileStore.DEFAULT_TEST_FILE);
        }
        // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
        // with content.
        httpServer.clearFiles();
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetChecksumsClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetChecksumsClient(getChecksumsClientSettings) 
                instanceof BasicGetChecksumsClient, 
                "The default GetFileClient from the Access factory should be of the type '" + 
                BasicGetChecksumsClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getChecksumsDeliveredAtUrl() throws Exception {
        addDescription("Tests the delivery of checksums from a pillar at a given URL.");
        addStep("Initailise the variables for this test.", 
                "EventManager and GetChecksumsClient should be instantiated.");

        String deliveryFilename = "TEST-CHECKSUM-DELIVERY.xml";
        FileIDs fileIDs = new FileIDs();
        fileIDs.getFileID().add(DEFAULT_FILE_ID);
        ChecksumSpecs checksumSpecs = new ChecksumSpecs();
        checksumSpecs.setNoOfItems(BigInteger.ONE);
        ChecksumSpecsItems csItems = new ChecksumSpecsItems();
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumSalt(null);
        csType.setChecksumType("MD5");
        csItems.getChecksumSpecsItem().add(csType);
        checksumSpecs.setChecksumSpecsItems(csItems);

        //        ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        GetChecksumsClient getChecksumsClient = new GetChecksumsClientTestWrapper(
                AccessComponentFactory.getInstance().createGetChecksumsClient(getChecksumsClientSettings), 
                testEventManager);

        addStep("Ensure the delivery file isn't already present on the http server", 
        "Should be remove if it already exists.");
        httpServer.removeFile(deliveryFilename);
        URL deliveryUrl = httpServer.getURL(deliveryFilename);

        addStep("Request the delivery of the checksum of a file from the pillar(s). A callback listener should be supplied.", 
        "A IdentifyPillarsForGetChecksumsRequest will be sent to the pillar(s).");
        getChecksumsClient.getChecksums(getChecksumsClientSettings.getPillarIDs(), fileIDs, checksumSpecs, 
                deliveryUrl, testEventHandler);

        IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(
                    IdentifyPillarsForGetChecksumsRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetChecksumsRequest(receivedIdentifyRequestMessage, 
                            bitRepositoryCollectionDestinationID));
        }

        addStep("The pillar sends a response to the identify message.", 
                "The callback listener should notify of the response and the client should send a GetChecksumsRequest "
                + "message to the pillar"); 

        GetChecksumsRequest receivedGetChecksumsRequest = null;
        if (useMockupPillar()) {
            IdentifyPillarsForGetChecksumsResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetChecksumsResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            System.out.println(receivedIdentifyRequestMessage);
            messageBus.sendMessage(identifyResponse);
            receivedGetChecksumsRequest = pillar1Destination.waitForMessage(GetChecksumsRequest.class);
            Assert.assertEquals(receivedGetChecksumsRequest, 
                    testMessageFactory.createGetChecksumsRequest(receivedGetChecksumsRequest,PILLAR1_ID, pillar1DestinationId));
        }

        for(int i = 0; i < getChecksumsClientSettings.getPillarIDs().length; i++) {
            Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        }
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);

        addStep("The pillar sends a getChecksumsProgressResponse to the GetChecksumsClient.", 
                "The GetChecksumsClient should notify about the response through the callback interface."); 
        if (useMockupPillar()) {
            GetChecksumsProgressResponse getChecksumsProgressResponse = testMessageFactory.createGetChecksumsProgressResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(getChecksumsProgressResponse);
        }
        
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);

        addStep("The resulting file is uploaded to the indicated url and the pillar sends a final response upload message", 
                "The GetChecksumsClient notifies that the file is ready through the callback listener and the uploaded file is present.");
        if (useMockupPillar()) {
//            httpServer.uploadFile(pillar1FileStore.getFileAsInputstream(receivedGetChecksumsRequest.getFileIDs()),
//                    new URL(receivedGetFileRequest.getFileAddress()));

            GetChecksumsFinalResponse completeMsg = testMessageFactory.createGetChecksumsFinalResponse(
                    receivedGetChecksumsRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(completeMsg);
        }

        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PartiallyComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
        //        File expectedUploadFile = pillar1FileStore.getFile(DEFAULT_FILE_ID);
//        httpServer.assertFileEquals(TestFileStore.DEFAULT_TEST_FILE, url.toExternalForm());
    }
    
    //    @Test(groups = {"regressiontest"})
    public void chooseFastestPillarGetFileClient() throws Exception {
        addDescription("Set the GetClient to retrieve a file as fast as "
                + "possible, where it has to choose between to pillars with "
                + "different times. The messages should be delivered at the "
                + "same time.");
        addStep("Create a GetFileClient configured to use a fast and a slow pillar.", "");

//        String averagePillarID = "THE-AVERAGE-PILLAR";
//        String fastPillarID = "THE-FAST-PILLAR";
//        String slowPillarID = "THE-SLOW-PILLAR";
//        ((MutableClientSettings)getFileClientSettings).setPillarIDs(
//                new String[] {averagePillarID, fastPillarID, slowPillarID});
//        GetFileClient getFileClient = 
//            new GetChecksumsClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
//                    testEventManager);
//        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
//
//        addStep("Defining the variables for the GetFileClient and defining them in the configuration", 
//        "It should be possible to change the values of the configurations.");
//
//        addStep("Make the GetClient ask for fastest pillar.", 
//        "It should send message to identify which pillars.");
//        getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
//        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
//        if (useMockupPillar()) {
//            receivedIdentifyRequestMessage = 
//                bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
//        }
//
//        addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
//                " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar. " +
//                "The event handler should receive the following events: " +
//        "3 x PillarIdentified, a PillarSelected and a RequestSent");
//
//        if (useMockupPillar()) {
//            IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
//                    receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
//            TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
//            averageTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.MILLISECONDS);
//            averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
//            averageReply.setTimeToDeliver(averageTime);
//            messageBus.sendMessage(averageReply);
//
//            IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
//                    receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
//            TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
//            fastTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.MILLISECONDS);
//            fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
//            fastReply.setTimeToDeliver(fastTime);
//            messageBus.sendMessage(fastReply);
//
//            IdentifyPillarsForGetFileResponse slowReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
//                    receivedIdentifyRequestMessage, slowPillarID, pillar2DestinationId);
//            TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
//            slowTime.setTimeMeasureValue(BigInteger.valueOf(1L));
//            slowTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.HOURS);
//            slowReply.setTimeToDeliver(slowTime);
//            messageBus.sendMessage(slowReply);
//
//            GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
//            Assert.assertEquals(receivedGetFileRequest, 
//                    testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, pillar1DestinationId));
//        }
//
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        PillarOperationEvent event = (PillarOperationEvent) testEventHandler.waitForEvent();
//        Assert.assertEquals(event.getType(), OperationEventType.PillarSelected);
//        Assert.assertEquals(event.getState(), fastPillarID);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
    }

    //    @Test(groups = {"regressiontest"})
    public void chooseFastestPillarGetFileClientWithIdentifyTimeout() throws Exception {
        addDescription("Verify that the FastestPillarGetFile works correct without receiving responses from all" +
        "pillars.");
        addStep("Create a GetFileClient configured to use 3 pillars and a 3 second timeout for identifying pillar.", "");

//        String averagePillarID = "THE-AVERAGE-PILLAR";
//        String fastPillarID = "THE-FAST-PILLAR";
//        String slowPillarID = "THE-SLOW-PILLAR";
//        ((MutableClientSettings)getFileClientSettings).setPillarIDs(
//                new String[] {averagePillarID, fastPillarID, slowPillarID});
//        ((MutableClientSettings)getFileClientSettings).setIdentifyPillarsTimeout(3000);
//        GetFileClient getFileClient = 
//            new GetChecksumsClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
//                    testEventManager);
//
//        addStep("Make the GetClient ask for fastest pillar.",  
//        "It should send message to identify which pillar can respond fastest.");
//        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
//        getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
//        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
//        if (useMockupPillar()) {
//            receivedIdentifyRequestMessage = 
//                bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
//        }
//
//        addStep("Two pillars send responses. First an average timeToDeliver, then a fast timeToDeliver.", 
//                "The client should send a getFileRequest to the fast pillar after 3 seconds. " +
//                "The event handler should receive the following events: " +
//        "2 x PillarIdentified, a identify timeout, a PillarSelected and a RequestSent event.");
//
//        if (useMockupPillar()) {
//            IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
//                    receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
//            TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
//            averageTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.MILLISECONDS);
//            averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
//            averageReply.setTimeToDeliver(averageTime);
//            messageBus.sendMessage(averageReply);
//
//            IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
//                    receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
//            TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
//            fastTime.setTimeMeasureUnit(TimeMeasureTYPE.TimeMeasureUnit.MILLISECONDS);
//            fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
//            fastReply.setTimeToDeliver(fastTime);
//            messageBus.sendMessage(fastReply);
//
//            GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(
//                    GetFileRequest.class, 5, TimeUnit.SECONDS );
//            Assert.assertEquals(receivedGetFileRequest, 
//                    testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, pillar1DestinationId));
//        }
//
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarTimeout);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
    }

    //    @Test(groups = {"regressiontest"})
    public void noIdentifyResponses() throws Exception {
        addDescription("Tests the th eGetFileClient handles lack of IdentifyPillarResponses gracefully  ");
        addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for identifying pillar.", "");

//        ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
//        ((MutableClientSettings)getFileClientSettings).setIdentifyPillarsTimeout(3000);
//        GetFileClient getFileClient = 
//            new GetChecksumsClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
//                    testEventManager);
//
//        addStep("Make the GetClient ask for fastest pillar.",  
//        "It should send message to identify which pillar can respond fastest.");
//        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
//        getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), testEventHandler);
//        if (useMockupPillar()) {
//            bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
//        }
//
//        addStep("Wait for at least 3 seconds", "An IdentifyPillarTimeout event should be received");
//
//        Assert.assertEquals(testEventHandler.waitForEvent( 4, TimeUnit.SECONDS).getType(), OperationEventType.NoPillarFound);
    }


    //    @Test(groups = {"testfirst"})
    public void conversationTimeout() throws Exception {
        addDescription("Tests the th eGetFileClient handles lack of IdentifyPillarResponses gracefully  ");
        addStep("Set the number of pillars for this SLA to 1 and a 3 second timeout for the conversation.", "");

//        ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
//        ((MutableClientSettings)getFileClientSettings).setConversationTimeout(3000);
//        GetFileClient getFileClient = 
//            new GetChecksumsClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
//                    testEventManager);
//
//        addStep("Request the delivery of a file from a specific pillar. A callback listener should be supplied.", 
//        "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
//        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
//        getFileClient.getFileFromSpecificPillar(
//                DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), PILLAR1_ID, testEventHandler);
//        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
//        if (useMockupPillar()) {
//            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
//            Assert.assertEquals(receivedIdentifyRequestMessage, 
//                    testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage, 
//                            bitRepositoryCollectionDestinationID));
//        }
//
//        addStep("The pillar sends a response to the identify message.", 
//                "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
//        "the pillar"); 
//
//        if (useMockupPillar()) {
//            IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
//                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
//            messageBus.sendMessage(identifyResponse);
//            pillar1Destination.waitForMessage(GetFileRequest.class);
//        }
//
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
//
//        addStep("Wait for at least 3 seconds", "An failed event should be received");  
//        Assert.assertEquals(testEventHandler.waitForEvent(4, TimeUnit.SECONDS).getType(), OperationEventType.Failed);
    }
    
//    @Test(groups = {"regressiontest"})
    public void testNoSuchFile() throws Exception {
        addDescription("Testing how a request for a non-existing file is handled.");
        addStep("Setting up variables and such.", "Should be OK.");
        
//        String fileName = "ERROR-NO-SUCH-FILE-ERROR";
//        GetFileClient gfc = new GetChecksumsClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
//                testEventManager);
//        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
//        URL url = httpServer.getURL(DEFAULT_FILE_ID);
//        
//        addStep("Perform Get operation for retrieving the non-existing file '" + fileName + "'", 
//                "All pillars should send errors.");
//        gfc.getFileFromFastestPillar(fileName, url, testEventHandler);
//        
//        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Failed);
    }
}
