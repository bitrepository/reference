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

import java.io.File;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.messagefactories.GetAuditTrailsMessageFactory;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.service.contributor.ContributorContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetAuditTrailsOnReferencePillarTest extends DefaultFixturePillarTest {
    GetAuditTrailsMessageFactory msgFactory;
    
    ReferenceArchive archive;
    ReferencePillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        msgFactory = new GetAuditTrailsMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, settings, 
                settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        alarmDispatcher = new MockAlarmDispatcher(contributorContext);
        PillarContext context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
        mediator = new ReferencePillarMediator(context, archive);
        mediator.start();
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        if(mediator != null) {
            mediator.close();
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void referencePillarGetAuditTrailsSuccessful() {
        addDescription("Tests the GetAuditTrails functionality of the reference pillar for the successful scenario, "
                + "where all audit trails are requested.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String contributorId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = "";
        audits.addAuditEvent("fileid", "actor", "info", "auditTrail", FileAction.OTHER);

        addStep("Send the identification request", "Should be caught and handled by the pillar.");
        IdentifyContributorsForGetAuditTrailsRequest identifyRequest = msgFactory.createIdentifyContributorsForGetAuditTrailsRequest(
                auditTrail, FROM, clientDestinationId);
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
                identifyRequest.getCorrelationID(), null, FROM, null, null, null, null, clientDestinationId, null, 
                pillarDestinationId);
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
        String contributorId = settings.getReferenceSettings().getPillarSettings().getPillarID();
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
                auditTrail, FROM, clientDestinationId);
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
                identifyRequest.getCorrelationID(), FILE_ID, FROM, BigInteger.ONE, maxDate, BigInteger.ONE, 
                minDate, clientDestinationId, null, pillarDestinationId);
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
                identifyRequest.getCorrelationID(), null, FROM, null, null, null, null, clientDestinationId, null, pillarDestinationId);
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
    }
}
