/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
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
package org.bitrepository.pillar.messagehandling;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class PutFileTest extends MockedPillarTest {
    PutFileMessageFactory msgFactory;
    Long FILE_SIZE = 1L;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentification() throws Exception {
        addDescription("Tests the identification for a PutFile operation on the pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for not already having the file and delivering pillar id", 
                "Should return false, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileID(), FILE_ID);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseIdentification() throws Exception {
        addDescription("Tests the identification for a PutFile operation on the pillar for the failure scenario, when the file already exists.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for already having the file and delivering pillar id", 
                "Should return true, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.DUPLICATE_FILE_FAILURE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileID(), FILE_ID);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseOperationFileAlreadyExists() throws Exception {
        addDescription("Tests the PutFile operation on the pillar for the failure scenario, when the file already exists.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for already having the file and delivering pillar id", 
                "Should return true, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        PutFileRequest request = msgFactory.createPutFileRequest(csData, null, DEFAULT_DOWNLOAD_FILE_ADDRESS, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        // Does not send a progress response.
        
        addStep("Retrieve the FinalResponse for the PutFile request",
                "The final response should say 'operation_complete', and give the requested data.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.DUPLICATE_FILE_FAILURE);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileID(), FILE_ID);
        
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseOperationNoValidationChecksum() throws Exception {
        addDescription("Tests the PutFile operation on the pillar for the failure scenario, when no validation checksum is given but required.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        settingsForCUT.getRepositorySettings().getProtocolSettings().setRequireChecksumForNewFileRequests(true);

        addStep("Setup for not already having the file and delivering pillar id", 
                "Should return false, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        PutFileRequest request = msgFactory.createPutFileRequest(null, null, DEFAULT_DOWNLOAD_FILE_ADDRESS, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the FinalResponse for the PutFile request",
                "The final response should say 'operation_complete', and give the requested data.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileID(), FILE_ID);
        
        addStep("Pillar should have sent an alarm", "Alarm contains information about the missing verification checksum");
        AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
        assertEquals(alarm.getAlarm().getFileID(), FILE_ID);
        assertEquals(alarm.getAlarm().getAlarmRaiser(), getPillarID());
        assertEquals(alarm.getAlarm().getAlarmCode(), AlarmCode.CHECKSUM_ALARM);
    }
    
    @SuppressWarnings("rawtypes")
    //@Test( groups = {"regressiontest", "pillartest"})
    // FAILS, when combined with other tests...
    public void goodCaseOperation() throws Exception {
        addDescription("Tests the PutFile operation on the pillar for the success scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for not already having the file and delivering pillar id", 
                "Should return false, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        PutFileRequest request = msgFactory.createPutFileRequest(csData, null, DEFAULT_DOWNLOAD_FILE_ADDRESS, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientReceiver.waitForMessage(PutFileProgressResponse.class);
        assertEquals(progressResponse.getFileID(), FILE_ID);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        
        addStep("Retrieve the FinalResponse for the PutFile request",
                "The final response should say 'operation_complete', and give the requested data.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileID(), FILE_ID);
        assertNull(finalResponse.getChecksumDataForNewFile());
        assertNull(finalResponse.getChecksumDataForExistingFile());
        
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 1, "Should make 1 put-file audit trail");
    }
    
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationWithChecksumReturn() throws Exception {
        addDescription("Tests the PutFile operation on the pillar for the success scenario, when requesting the cheksum of the file returned.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for not already having the file and delivering pillar id, and delivering an answer for the checksum request", 
                "Should return false, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();
        doAnswer(new Answer() {
            public ChecksumDataForFileTYPE answer(InvocationOnMock invocation) {
                ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
                res.setChecksumSpec(csSpec);
                res.setCalculationTimestamp(CalendarUtils.getNow());
                res.setChecksumValue(Base16Utils.encodeBase16(DEFAULT_MD5_CHECKSUM));
                return res;
            }            
        }).when(model).getChecksumDataForFile(eq(FILE_ID), anyString(), any(ChecksumSpecTYPE.class));

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        PutFileRequest request = msgFactory.createPutFileRequest(csData, csSpec, DEFAULT_DOWNLOAD_FILE_ADDRESS, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientReceiver.waitForMessage(PutFileProgressResponse.class);
        assertEquals(progressResponse.getFileID(), FILE_ID);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        
        addStep("Retrieve the FinalResponse for the PutFile request",
                "The final response should say 'operation_complete', and give the requested data.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileID(), FILE_ID);
        assertNotNull(finalResponse.getChecksumDataForNewFile());
        assertEquals(finalResponse.getChecksumDataForNewFile().getChecksumSpec(), csSpec);
        
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 1, "Should make 1 put-file audit trail");
    }
}
