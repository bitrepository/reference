/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class PutFileOnReferencePillarTest extends DefaultFixturePillarTest {
    PillarTestMessageFactory msgFactory;
    
    @BeforeMethod(alwaysRun=true)
    public void initialise() throws Exception {
        msgFactory = new PillarTestMessageFactory(settings);
    }
    
    @Test( groups = {"regressiontest"})
    public void pillarPutTest() throws Exception {
        addDescription("Tests the put functionality of the reference pillar.");
        addStep("Set up constants and variables.", "Should not fail here!");
        ReferencePillarComponentFactory.getInstance().getPillar(settings);
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        Long FILE_SIZE = 27L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String FILE_CHECKSUM = "356fbb1d1d6d15814bd5d2855f6808e51823a24e";
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
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        
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
                        progressResponse.getResponseInfo(), 
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
                        finalResponse.getResponseInfo(), 
                        finalResponse.getPillarID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        finalResponse.getReplyTo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), FILE_ID, "The FileID of this test.");
        Assert.assertNotNull(finalResponse.getChecksumsDataForNewFile(), "The results should contain a ");
        ChecksumDataForFileTYPE receivedChecksumData = finalResponse.getChecksumsDataForNewFile().getChecksumDataItem();
        Assert.assertNotNull(receivedChecksumData.getChecksumSpec());
        Assert.assertEquals(receivedChecksumData.getChecksumSpec(), putRequest.getFileChecksumSpec(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(receivedChecksumData.getChecksumValue(), FILE_CHECKSUM);
        Assert.assertTrue(receivedChecksumData.getCalculationTimestamp().toGregorianCalendar().getTimeInMillis() > startDate.getTime(), 
                "The received timestamp should be after the start of this test '" + startDate + "', but was "
                + receivedChecksumData.getCalculationTimestamp().toGregorianCalendar().getTime() + "'");
    }

}
