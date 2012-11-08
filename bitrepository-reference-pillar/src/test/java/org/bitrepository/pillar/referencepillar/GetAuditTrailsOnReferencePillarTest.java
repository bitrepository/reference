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
package org.bitrepository.pillar.referencepillar;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.messagefactories.GetAuditTrailsMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Date;

public class GetAuditTrailsOnReferencePillarTest extends ReferencePillarTest {
    GetAuditTrailsMessageFactory msgFactory;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetAuditTrailsOnReferencePillarTest() throws Exception {
        msgFactory = new GetAuditTrailsMessageFactory(settingsForCUT);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void referencePillarGetAuditTrailsSuccessful() {
        addDescription("Tests the GetAuditTrails functionality of the reference pillar for the successful scenario, "
                + "where all audit trails are requested.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String contributorId = settingsForCUT.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = "";
        audits.addAuditEvent("fileid", "actor", "info", "auditTrail", FileAction.OTHER);

        addStep("Send the identification request", "Should be caught and handled by the pillar.");
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest = msgFactory.createIdentifyContributorsForGetAuditTrailsRequest(
                auditTrail, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response.", "Should be a positive response.");
        IdentifyContributorsForGetAuditTrailsResponse identifyResponse = clientTopic.waitForMessage(
                IdentifyContributorsForGetAuditTrailsResponse.class);
        Assert.assertEquals(identifyResponse, msgFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                identifyRequest.getCorrelationID(), contributorId, pillarDestinationId, 
                identifyResponse.getResponseInfo(), clientDestinationId));
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);

        addStep("Make and send the request for the actual GetAuditTrails operation", 
                "Should be caught and handled by the pillar.");
        GetAuditTrailsRequest request = msgFactory.createGetAuditTrailsRequest(auditTrail, contributorId, 
                identifyRequest.getCorrelationID(), null, getPillarID(), null, null, null, null, null, 
                clientDestinationId, null, pillarDestinationId);
        messageBus.sendMessage(request);
        
        addStep("Receive and validate the progress response.", "Should be sent by the pillar.");
        GetAuditTrailsProgressResponse progressResponse = clientTopic.waitForMessage(GetAuditTrailsProgressResponse.class);
        Assert.assertEquals(progressResponse, msgFactory.createGetAuditTrailsProgressResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, progressResponse.getResponseInfo(), 
                null, clientDestinationId));
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Receive and validate the final response", "Should be sent by the pillar.");
        GetAuditTrailsFinalResponse finalResponse = clientTopic.waitForMessage(GetAuditTrailsFinalResponse.class);
        Assert.assertEquals(finalResponse, msgFactory.createGetAuditTrailsFinalResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, finalResponse.getResponseInfo(), 
                finalResponse.getResultingAuditTrails(), clientDestinationId));
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingAuditTrails().getAuditTrailEvents().getAuditTrailEvent().size(), 1);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void referencePillarGetAuditTrailsSpecificRequests() {
        addDescription("Tests the GetAuditTrails functionality of the reference pillar for the successful scenario, "
                + "where a specific audit trail are requested.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String contributorId = settingsForCUT.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = "";
        String FILE_ID = "fileId" + new Date().getTime();
        String ACTOR = "ACTOR";
        String INFO = "InFo";
        String AUDITTRAIL = "auditTrails";
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.OTHER);
        audits.addAuditEvent("notThisFile", "UnknownActor", "badInfo", "WrongAuditTrail", FileAction.FAILURE);
        XMLGregorianCalendar minDate = CalendarUtils.getFromMillis(System.currentTimeMillis() - 10000);
        XMLGregorianCalendar maxDate = CalendarUtils.getFromMillis(System.currentTimeMillis() + 10000);

        addStep("Send the identification request", "Should be caught and handled by the pillar.");
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest = msgFactory.createIdentifyContributorsForGetAuditTrailsRequest(
                auditTrail, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response.", "Should be a positive response.");
        IdentifyContributorsForGetAuditTrailsResponse identifyResponse = clientTopic.waitForMessage(
                IdentifyContributorsForGetAuditTrailsResponse.class);
        Assert.assertEquals(identifyResponse, msgFactory.createIdentifyContributorsForGetAuditTrailsResponse(
                identifyRequest.getCorrelationID(), contributorId, pillarDestinationId, 
                identifyResponse.getResponseInfo(), clientDestinationId));
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);

        addStep("Make and send the request for the actual GetAuditTrails operation", 
                "Should be caught and handled by the pillar.");
        GetAuditTrailsRequest request = msgFactory.createGetAuditTrailsRequest(auditTrail, contributorId, 
                identifyRequest.getCorrelationID(), FILE_ID, getPillarID(), null, BigInteger.ONE, maxDate, 
                BigInteger.ONE, minDate, clientDestinationId, null, pillarDestinationId);
        messageBus.sendMessage(request);
        
        addStep("Receive and validate the progress response.", "Should be sent by the pillar.");
        GetAuditTrailsProgressResponse progressResponse = clientTopic.waitForMessage(GetAuditTrailsProgressResponse.class);
        Assert.assertEquals(progressResponse, msgFactory.createGetAuditTrailsProgressResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, progressResponse.getResponseInfo(), 
                null, clientDestinationId));
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Receive and validate the final response", "Should be sent by the pillar.");
        GetAuditTrailsFinalResponse finalResponse = clientTopic.waitForMessage(GetAuditTrailsFinalResponse.class);
        Assert.assertEquals(finalResponse, msgFactory.createGetAuditTrailsFinalResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, finalResponse.getResponseInfo(), 
                finalResponse.getResultingAuditTrails(), clientDestinationId));
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingAuditTrails().getAuditTrailEvents().getAuditTrailEvent().size(), 1);
        AuditTrailEvent event = finalResponse.getResultingAuditTrails().getAuditTrailEvents().getAuditTrailEvent().get(0);
        Assert.assertEquals(event.getActorOnFile(), ACTOR);
        Assert.assertEquals(event.getAuditTrailInformation(), AUDITTRAIL);
        Assert.assertEquals(event.getFileID(), FILE_ID);
        Assert.assertEquals(event.getInfo(), INFO);
        Assert.assertEquals(event.getSequenceNumber(), BigInteger.ONE);
        
        addStep("Make another request, where both ingested audit trails is requested", 
                "Should be handled by the pillar.");
        request = msgFactory.createGetAuditTrailsRequest(auditTrail, contributorId, 
                identifyRequest.getCorrelationID(), null, getPillarID(), null, null, null, null, null, 
                clientDestinationId, null, pillarDestinationId);
        messageBus.sendMessage(request);
        
        addStep("Receive and validate the progress response.", "Should be sent by the pillar.");
        progressResponse = clientTopic.waitForMessage(GetAuditTrailsProgressResponse.class);
        Assert.assertEquals(progressResponse, msgFactory.createGetAuditTrailsProgressResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, progressResponse.getResponseInfo(), 
                null, clientDestinationId));
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Receive and validate the final response", "Should be sent by the pillar.");
        finalResponse = clientTopic.waitForMessage(GetAuditTrailsFinalResponse.class);
        Assert.assertEquals(finalResponse, msgFactory.createGetAuditTrailsFinalResponse(contributorId, 
                request.getCorrelationID(), pillarDestinationId, finalResponse.getResponseInfo(), 
                finalResponse.getResultingAuditTrails(), clientDestinationId));
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingAuditTrails().getAuditTrailEvents().getAuditTrailEvent().size(), 2);    
        Assert.assertFalse(finalResponse.isPartialResult());
    }
    
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void referencePillarGetAuditTrailsMaximumNumberOfResults() {
        addDescription("Tests the GetAuditTrails functionality of the reference pillar for the successful scenario, "
                + "where a limited number of audit trails are requested.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String auditTrail = "";
        String FILE_ID = "fileId" + new Date().getTime();
        String ACTOR = "ACTOR";
        String INFO = "InFo";
        String AUDITTRAIL = "auditTrails";
        long maxNumberOfResults = 5L;
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.CHECKSUM_CALCULATED);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.DELETE_FILE);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.FAILURE);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.FILE_MOVED);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.GET_CHECKSUMS);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.GET_FILE);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.GET_FILEID);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.INCONSISTENCY);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.INTEGRITY_CHECK);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.OTHER);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.PUT_FILE);
        audits.addAuditEvent(FILE_ID, ACTOR, INFO, AUDITTRAIL, FileAction.REPLACE_FILE);

        addStep("Send the request for the limited amount of audit trails to the pillar.", 
                "The pillar handles the request and responds.");
        GetAuditTrailsRequest request = msgFactory.createGetAuditTrailsRequest(auditTrail, getComponentID(), 
                msgFactory.getNewCorrelationID(), FILE_ID, getPillarID(), BigInteger.valueOf(maxNumberOfResults), 
                null, null, null, null, clientDestinationId, null, pillarDestinationId);
        messageBus.sendMessage(request);
        GetAuditTrailsFinalResponse finalResponse = clientTopic.waitForMessage(GetAuditTrailsFinalResponse.class);
        
        addStep("Validate the final response", "Contains OPERATION_COMPLETE, with only the requested amount of audits, "
                + "and acknowledges that it is only a partial result set.");
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingAuditTrails().getAuditTrailEvents().getAuditTrailEvent().size(), maxNumberOfResults);
        Assert.assertTrue(finalResponse.isSetPartialResult());
    }
}
