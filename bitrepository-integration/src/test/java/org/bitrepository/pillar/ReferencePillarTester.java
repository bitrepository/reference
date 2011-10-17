/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.pillar;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.integration.IntegrationComponentFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the reference pillar.
 */
public class ReferencePillarTester extends DefaultFixturePillarTest {
    PillarTestMessageFactory msgFactory;
    
    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        msgFactory = new PillarTestMessageFactory(settings);
    }

    @Test( groups = {"regressiontest"})
    public void pillarPutTest() throws Exception {
        addDescription("Tests the put functionality of the reference pillar.");
        addStep("Set up constants and variables.", "Should not fail here!");
        ReferencePillar pillar = IntegrationComponentFactory.getInstance().getPillar(settings);
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        Long FILE_SIZE = 27L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String FILE_CHECKSUM = "940a51b250e7aa82d8e8ea31217ff267";
        Date startDate = new Date();
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest 
                = msgFactory.createIdentifyPillarsForPutFileRequest(clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        receivedIdentifyResponse.getCorrelationID(),
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getPillarID(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
       
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, FILE_SIZE,
                receivedIdentifyResponse.getPillarID(), clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientTopic.waitForMessage(PutFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createPutFileProgressResponse(
                        progressResponse.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(), 
                        progressResponse.getPillarID(), 
                        progressResponse.getPillarChecksumSpec(), 
                        progressResponse.getProgressResponseInfo(), 
                        progressResponse.getReplyTo(), 
                        progressResponse.getTo()));

        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
                        finalResponse.getChecksumsDataForNewFile(),
                        finalResponse.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getFinalResponseInfo(), 
                        finalResponse.getPillarID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        finalResponse.getReplyTo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        ChecksumsDataForNewFile receivedChecksums = finalResponse.getChecksumsDataForNewFile();
//        Assert.assertEquals(receivedChecksums.getFileID(), FILE_ID, receivedChecksums.toString());
//        Assert.assertEquals(receivedChecksums.getNoOfItems(), BigInteger.valueOf(1L));
//        ChecksumDataItems checksumItems = receivedChecksums.getChecksumDataItems();
//        Assert.assertEquals(checksumItems.getChecksumDataForFile().size(), 1);
//        ChecksumDataForFileTYPE checksumdata = checksumItems.getChecksumDataForFile().get(0);
//        Assert.assertEquals(checksumdata.getChecksumValue(), FILE_CHECKSUM);
//        Assert.assertNull(checksumdata.getChecksumSpec().getChecksumSalt(), "should be no salt");
//        Assert.assertEquals(checksumdata.getChecksumSpec().getChecksumType(), "MD5");
//        Assert.assertTrue(checksumdata.getCalculationTimestamp().toGregorianCalendar().getTime().getTime() > startDate.getTime());
    }
}
