/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.access;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfile.GetFileClientTestWrapper;
import org.bitrepository.access.getfile.SimpleGetFileClient;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.clienttest.ClientTest;
import org.bitrepository.common.sla.MutableSLAConfiguration;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetFileClientTest extends ClientTest {
    private GetFileClient getFileClient;
    private TestGetFileMessageFactory testMessageFactory;

    @BeforeMethod (alwaysRun=true)
    public void setupTest() {
        // In case of multiple types of GetFileClients, the concrete class should be configurable (perhaps specialize 
        // this test with an overload of this method)
        getFileClient = 
            new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(slaConfiguration), 
                    testEventManager);
              
        File oldFile = new File(slaConfiguration.getLocalFileStorage(), DEFAULT_FILE_ID);
        if(oldFile.exists()) {
            Assert.assertTrue(oldFile.delete(), "The previously downloaded file should be deleted.");
        }
        testMessageFactory = new TestGetFileMessageFactory(slaConfiguration.getSlaId(), clientTopicId, slaTopicId);
    }
    
    @Test(groups = {"regressiontest"})
    public void verifyGetFileClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileClient(slaConfiguration) 
                instanceof SimpleGetFileClient, 
                "The default GetFileClient from the Access factory should be of the type '" + 
                SimpleGetFileClient.class.getName() + "'.");
    }

    @Test(groups = {"test-first"})
    public void identifyAndGetSinglePillar() throws Exception {
        addDescription("Tests whether a specific message is sent by the GetClient, when only a single pillar " +
        		"participates");
        addStep("Initialising", "");
        ((MutableSLAConfiguration)slaConfiguration).setNumberOfPillars(1);
   
        addStep("Register a callback listener in the GetFileClient", "");
        //ToDo implement this when the listener registration method has been added to the GetFIleClient interface
        
        addStep("Request the fastest delivery of file '" + DEFAULT_FILE_ID + "' from SLA '" + slaConfiguration.getSlaId() + 
                "', and the knowledge, that only one pillar should reply. Note that this call should block, so the " +
                "call needs to be performed in a seperat thread", 
                "A IdentifyPillarsForGetFileRequest should be received on the pillar.");
        // Todo the blocking functionality isn't implemented yet. Awaits the refactoring of the client to Kåres 
        // general client design 
        getFileClient.retrieveFastest(DEFAULT_FILE_ID);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = 
            slaTopic.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        Assert.assertEquals(receivedIdentifyRequestMessage, 
                testMessageFactory.createIdentifyPillarsForGetFileIDsRequest(receivedIdentifyRequestMessage));
        
        Assert.assertNotNull(receivedIdentifyRequestMessage, "No IdentifyPillarsForGetFileRequest received.");
        Assert.assertEquals(receivedIdentifyRequestMessage.getFileID(), DEFAULT_FILE_ID);
        
        addStep("Sending a response to the identify message.", 
                "The callback listener IdentifyPillarsForGetFileResponse object should be returned from the client"); 
        IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1TopicId);
        messageBus.sendMessage(clientTopicId, identifyResponse);
        //Todo Verify the result when the GetFileClient callback interface has been defined 
        
        addStep("Verify that the GetClient sends a getFile request.", 
                "A GetFileRequest should be received by the pillar.");
        GetFileRequest receivedGetFileRequest = pillar1Topic.waitForMessage(GetFileRequest.class);
        Assert.assertEquals(receivedGetFileRequest, testMessageFactory.createGetFileRequest(receivedGetFileRequest));
        
        addStep("Send a getFile response to the GetClient.", 
                "The GetClient should notify about the response through the callback interface."); 
        GetFileProgressResponse getFileProgressResponse = testMessageFactory.createGetFileProgressResponse(
                receivedGetFileRequest, PILLAR1_ID, pillar1TopicId);
        messageBus.sendMessage(clientTopicId, getFileProgressResponse);

        addStep("Uploading the file to the default HTTPServer.", "");
        File uploadFile = new File("src/test/resources/test.txt");
        URL url = ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(uploadFile);
        
        addStep("Send a final response upload message with the URL", 
                "The GetFileClient on reception of the final response message, notify that the file is ready " +
                "through the callback listener and return from the blocking getFile method.");
        GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                receivedGetFileRequest, PILLAR1_ID, pillar1TopicId);
        messageBus.sendMessage(clientTopicId, completeMsg);
        
        //Need some kind of generic test functionality similar to the Message Receiver to handle the callbacks and 
        //blocking call operation stuff(without a fixed wait period).
        synchronized(this) {
            try {
                wait(1000);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        addStep("Verify that the file is downloaded in by the GetClient and placed within the GetClient's fileDir.", 
                "Should be fine!");
        File outputFile = new File(slaConfiguration.getLocalFileStorage(), DEFAULT_FILE_ID);
        Assert.assertTrue(outputFile.isFile());
    }
    
    @Test(groups = {"regressiontest"})
    public void chooseFastestPillarGetFileClient() throws Exception {
        addDescription("Set the GetClient to retrieve a file as fast as "
                + "possible, where it has to choose between to pillars with "
                + "different times. The messages should be delivered at the "
                + "same time.");
        addStep("Defining the test variables.", "Nothing should be able to go wrong here!");
        String fileId = "fileId";
        String fastPillar = "THE-FAST-PILLAR";
        String slowPillar = "THE-SLOW-PILLAR";
        ((MutableSLAConfiguration)slaConfiguration).setNumberOfPillars(2);
        
        addStep("Defining the varibles for the GetFileClient and defining them in the configuration", 
                "It should be possible to change the values of the configurations.");
        
        addStep("Make the GetClient ask for fastest pillar.", "It should send message to identify which pillars.");

        getFileClient.retrieveFastest(fileId);
        
        IdentifyPillarsForGetFileRequest identifyRequestMessage = 
            slaTopic.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        
        TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
        fastTime.setTimeMeasureUnit("milliseconds");
        fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
        TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
        slowTime.setTimeMeasureValue(BigInteger.valueOf(20000L));
        slowTime.setTimeMeasureUnit("hours");
        
        IdentifyPillarsForGetFileResponse fastReply = new IdentifyPillarsForGetFileResponse();
        fastReply.setCorrelationID(identifyRequestMessage.getCorrelationID());
        fastReply.setFileID(identifyRequestMessage.getFileID());
        fastReply.setMinVersion(BigInteger.valueOf(1L));
        fastReply.setVersion(BigInteger.valueOf(1L));
        fastReply.setBitrepositoryContextID(identifyRequestMessage.getBitrepositoryContextID());
        fastReply.setTimeToDeliver(fastTime);
        fastReply.setPillarID(fastPillar);  
        fastReply.setReplyTo(pillar1TopicId);
        messageBus.sendMessage(identifyRequestMessage.getReplyTo(), fastReply);
      
        fastReply.setReplyTo(identifyRequestMessage.getReplyTo());
        IdentifyPillarsForGetFileResponse slowReply = new IdentifyPillarsForGetFileResponse();
        slowReply.setCorrelationID(identifyRequestMessage.getCorrelationID());
        slowReply.setFileID(identifyRequestMessage.getFileID());
        slowReply.setMinVersion(BigInteger.valueOf(1L));
        slowReply.setVersion(BigInteger.valueOf(1L));
        slowReply.setBitrepositoryContextID(identifyRequestMessage.getBitrepositoryContextID());
        slowReply.setTimeToDeliver(slowTime);
        slowReply.setPillarID(slowPillar);  
        slowReply.setReplyTo(pillar2TopicId);
        messageBus.sendMessage(identifyRequestMessage.getReplyTo(), slowReply);
    
        addStep("Verify that it has chosen the fast pillar.", "");
        GetFileRequest getFileRequestMessage = pillar1Topic.waitForMessage(GetFileRequest.class);
        Assert.assertNotNull(getFileRequestMessage, "No GetFileResponse received.");
        Assert.assertEquals(getFileRequestMessage.getFileID(), identifyRequestMessage.getFileID());
        Assert.assertEquals(getFileRequestMessage.getPillarID(), fastPillar);     
    }
}
