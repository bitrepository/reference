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
package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.pillar.messagefactories.GetStatusMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetStatusOnChecksumPillarTest extends ChecksumPillarTest {
    GetStatusMessageFactory msgFactory;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        msgFactory = new GetStatusMessageFactory(componentSettings);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetStatusSuccessful() {
        addDescription("Tests the GetStatus functionality of the checksum pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String contributorId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;

        addStep("Send the identification request", "Should be caught and handled by the pillar.");
        IdentifyContributorsForGetStatusRequest identifyRequest = msgFactory.createIdentifyContributorsForGetStatusRequest(
                auditTrail, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response.", "Should be a positive response.");
        IdentifyContributorsForGetStatusResponse identifyResponse = clientTopic.waitForMessage(
                IdentifyContributorsForGetStatusResponse.class);
        Assert.assertEquals(identifyResponse, msgFactory.createIdentifyContributorsForGetStatusResponse(contributorId, 
                identifyRequest.getCorrelationID(), pillarDestinationId, identifyResponse.getResponseInfo(), 
                identifyResponse.getTimeToDeliver(), clientDestinationId));
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);

        addStep("Make and send the request for the actual GetStatus operation", 
                "Should be caught and handled by the pillar.");
        GetStatusRequest request = msgFactory.createGetStatusRequest(auditTrail, contributorId, 
                identifyRequest.getCorrelationID(), getPillarID(), clientDestinationId, pillarDestinationId);
        messageBus.sendMessage(request);
        
        addStep("Receive and validate the progress response.", "Should be sent by the pillar.");
        GetStatusProgressResponse progressResponse = clientTopic.waitForMessage(GetStatusProgressResponse.class);
        Assert.assertEquals(progressResponse, msgFactory.createGetStatusProgressResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, progressResponse.getResponseInfo(), 
                clientDestinationId));
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Receive and validate the final response", "Should be sent by the pillar.");
        GetStatusFinalResponse finalResponse = clientTopic.waitForMessage(GetStatusFinalResponse.class);
        Assert.assertEquals(finalResponse, msgFactory.createGetStatusFinalResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, finalResponse.getResponseInfo(), 
                finalResponse.getResultingStatus(), clientDestinationId));
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingStatus().getStatusInfo().getStatusCode(), StatusCode.OK);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetStatusWrongContributor() {
        addDescription("Tests the GetStatus functionality of the checksum pillar for the bad scenario, where a wrong "
                + "contributor id is given.");
        addFixtureSetup("Set the alarm level to warning to enable the sending of invalid ComponentIDs.");
        String contributorId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        String wrongContributorId = "wrongContributor";
        String auditTrail = null;

        addStep("Send the identification request", "Should be caught and handled by the pillar.");
        IdentifyContributorsForGetStatusRequest identifyRequest = msgFactory.createIdentifyContributorsForGetStatusRequest(
                auditTrail, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response.", "Should be a positive response.");
        IdentifyContributorsForGetStatusResponse identifyResponse = clientTopic.waitForMessage(
                IdentifyContributorsForGetStatusResponse.class);
        Assert.assertEquals(identifyResponse, msgFactory.createIdentifyContributorsForGetStatusResponse(contributorId, 
                identifyRequest.getCorrelationID(), pillarDestinationId, identifyResponse.getResponseInfo(), 
                identifyResponse.getTimeToDeliver(), clientDestinationId));
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);

        addStep("Make and send the request for the actual GetStatus operation", 
                "Should be caught and handled by the pillar.");
        GetStatusRequest request = msgFactory.createGetStatusRequest(auditTrail, wrongContributorId, 
                identifyRequest.getCorrelationID(), getPillarID(), clientDestinationId, pillarDestinationId);
        messageBus.sendMessage(request);
        
        addStep("The pillar should send an alarm.", "");
        Assert.assertNotNull(alarmReceiver.waitForMessage(AlarmMessage.class));
    }
}
