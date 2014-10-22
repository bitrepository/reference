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
package org.bitrepository.pillar.checksumpillar;

import static org.mockito.Mockito.*;

import java.math.BigInteger;
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
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.database.ExtractedFileIDsResultSet;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.bitrepository.protocol.CoordinationLayerException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetFileIDsOnChecksumPillarTest extends ChecksumPillarTest {
    private GetFileIDsMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUTwithMockCache();
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetFileIDs operation on the checksum pillar for the successful scenario.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file", "Should return true, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(cache).hasFile(eq(FILE_ID), anyString());
        
        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }


    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetFileIDs operation on the checksum pillar for the failure scenario, when the file is missing.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for not having the file", "Should return false, when requesting file-id existence.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(cache).hasFile(eq(FILE_ID), anyString());
        
        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationSingleFile() throws Exception {
        addDescription("Tests the GetFileIDs operation on the checksum pillar for the successful scenario when requesting one specific file.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(cache).hasFile(eq(FILE_ID), anyString());
        
        doAnswer(new Answer() {
            public ChecksumEntry answer(InvocationOnMock invocation) {
                ChecksumEntry res = new ChecksumEntry(DEFAULT_FILE_ID, DEFAULT_MD5_CHECKSUM, new Date(0));
                return res;                
            }
        }).when(cache).getEntry(anyString(), anyString());

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        Assert.assertEquals(progressResponse.getFileIDs(), fileids);
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        Assert.assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(), 1);
        Assert.assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0).getFileID(), FILE_ID);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationAllFiles() throws Exception {
        addDescription("Tests the GetFileIDs operation on the checksum pillar for the successful scenario, when requesting all files.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(cache).hasFile(eq(FILE_ID), anyString());
        
        doAnswer(new Answer() {
            public ExtractedFileIDsResultSet answer(InvocationOnMock invocation) {
                ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
                res.insertFileID(DEFAULT_FILE_ID, new Date(0));
                res.insertFileID(NON_DEFAULT_FILE_ID, new Date());
                return res;                
            }
        }).when(cache).getFileIDs(any(XMLGregorianCalendar.class), any(XMLGregorianCalendar.class), anyLong(), anyString());

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        Assert.assertEquals(progressResponse.getFileIDs(), fileids);
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertNull(finalResponse.getFileIDs().getFileID());
        Assert.assertEquals(finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size(), 2);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void badCaseOperationNoFile() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the checksum pillar for the failure scenario, where it does not have the file.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for not having the file", "Should cause the FILE_NOT_FOUND_FAILURE later.");
        doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(cache).hasFile(eq(FILE_ID), anyString());
        
        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        // No response, since failure
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request", 
                "The final response should tell about the error, and not contain the file.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        Assert.assertNull(finalResponse.getResultingFileIDs());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

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
        }).when(cache).hasFile(eq(FILE_ID), anyString());

        addStep("Setup for only delivering result-set when the correct restrictions are given.", "No failure here");
        doAnswer(new Answer() {
            public ExtractedFileIDsResultSet answer(InvocationOnMock invocation) {
                ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
                res.insertFileID(DEFAULT_FILE_ID, new Date(0));
                return res;                
            }
        }).when(cache).getFileIDs(eq(MIN_DATE), eq(MAX_DATE), eq(MAX_RESULTS), anyString());

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null, MAX_RESULTS, MAX_DATE, MIN_DATE);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        Assert.assertEquals(progressResponse.getFileIDs(), fileids);
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertNull(finalResponse.getFileIDs().getFileID());
    }
}
