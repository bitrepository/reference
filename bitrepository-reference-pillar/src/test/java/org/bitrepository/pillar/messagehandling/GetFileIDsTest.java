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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetFileIDsTest extends MockedPillarTest {
    private GetFileIDsMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetFileIDs operation on the pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        
        addStep("Setup for having the file and delivering pillar id", 
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
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);
        
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetFileIDs operation on the pillar for the failure scenario, when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        
        addStep("Setup for delivering pillar id and not having the file ", 
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
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);
        
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationSingleFile() throws Exception {
        addDescription("Tests the GetFileIDs operation on the pillar for the successful scenario when requesting one specific file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        addStep("Setup for having the file and delivering result-set", "No failure here");
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
        doAnswer(new Answer() {
            public ExtractedFileIDsResultSet answer(InvocationOnMock invocation) {
                ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
                res.insertFileID(DEFAULT_FILE_ID, new Date(0));
                return res;
                
            }
        }).when(model).getFileIDsResultSet(anyString(), any(XMLGregorianCalendar.class), any(XMLGregorianCalendar.class), anyLong(), anyString());
        
        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);
        
        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(), 1);
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID(), FILE_ID);
    }
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationAllFiles() throws Exception {
        addDescription("Tests the GetFileIDs operation on the pillar for the successful scenario, when requesting all files.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getAllFileIDs();
        
        addStep("Setup for having the file and delivering result-set", "No failure here");
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
        doAnswer(new Answer() {
            public ExtractedFileIDsResultSet answer(InvocationOnMock invocation) {
                ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
                res.insertFileID(DEFAULT_FILE_ID, new Date(0));
                res.insertFileID(NON_DEFAULT_FILE_ID, new Date());
                return res;
            }
        }).when(model).getFileIDsResultSet(isNull(String.class), any(XMLGregorianCalendar.class), any(XMLGregorianCalendar.class), anyLong(), anyString());
        
        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);
        
        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertNull(finalResponse.getFileIDs().getFileID());
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(), 2);
    }
    
    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseOperationNoFile() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the pillar for the failure scenario, where it does not have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        
        addStep("Setup for not having the file", "Should cause the FILE_NOT_FOUND_FAILURE later.");
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

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);
        
        // No response, since failure
        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should tell about the error, and not contain the file.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        assertNull(finalResponse.getResultingFileIDs());
        
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void testRestrictions() throws Exception {
        addDescription("Tests that the restrictions are correctly passed on to the cache.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        final XMLGregorianCalendar MIN_DATE = CalendarUtils.getXmlGregorianCalendar(new Date(12345));
        final XMLGregorianCalendar MAX_DATE = CalendarUtils.getXmlGregorianCalendar(new Date());
        final Long MAX_RESULTS = 12345L;

        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(model).hasFileID(eq(FILE_ID), eq(collectionID));
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();
        addStep("Setup for only delivering result-set when the correct restrictions are given.", "No failure here");
        doAnswer(new Answer() {
            public ExtractedFileIDsResultSet answer(InvocationOnMock invocation) {
                ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
                res.insertFileID(DEFAULT_FILE_ID, new Date(1234567890));
                return res;                
            }
        }).when(model).getFileIDsResultSet(isNull(String.class), eq(MIN_DATE), eq(MAX_DATE), eq(MAX_RESULTS), eq(collectionID));

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null, MAX_RESULTS, MAX_DATE, MIN_DATE);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertNull(finalResponse.getFileIDs().getFileID());
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(), 1);
        assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID(), DEFAULT_FILE_ID);
    }
}
