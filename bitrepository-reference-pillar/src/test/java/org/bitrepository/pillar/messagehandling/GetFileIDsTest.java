/*
 * #%L
 * Bitrepository Reference Pillar
 *
 * $Id: GetFileIDsTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src
 * /test/java/org/bitrepository/pillar/messagehandling/GetFileIDsTest.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.pillar.messagehandling;

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.utils.AllureTestUtils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Tests the GetFileIDs functionality on the ReferencePillar.
 */
@ExtendWith(SuiteInfoParameterResolver.class)
class GetFileIDsTest extends MockedPillarTest {
    private GetFileIDsMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void goodCaseIdentification() throws Exception {
        addDescription(
                "Tests the identification for a GetFileIDs operation on the pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.when(model.hasFileID(eq(FILE_ID), anyString())).thenReturn(true);
        Mockito.when(model.getPillarID()).thenReturn(settingsForCUT.getComponentID());

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(fileids, receivedIdentifyResponse.getFileIDs());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void badCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetFileIDs operation on the pillar for the failure scenario, " +
                "when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for delivering pillar id and not having the file ",
                "Should return false, when requesting file-id existence.");
        Mockito.when(model.hasFileID(eq(FILE_ID), anyString())).thenReturn(false);
        Mockito.when(model.getPillarID()).thenReturn(settingsForCUT.getComponentID());

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(fileids, receivedIdentifyResponse.getFileIDs());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void goodCaseOperationSingleFile() throws Exception {
        AllureTestUtils.addDescription("Tests the GetFileIDs operation on the pillar for the successful scenario when requesting one " +
                "specific file.");
        AllureTestUtils.addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        AllureTestUtils.addStep("Setup for having the file and delivering result-set", "No failure here");
        Mockito.when(model.hasFileID(eq(FILE_ID), anyString())).thenReturn(true);
        Mockito.when(model.getPillarID()).thenReturn(settingsForCUT.getComponentID());
        Mockito.doAnswer(invocation -> {
            ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
            res.insertFileID(FILE_ID, Instant.EPOCH);
            return res;
        }).when(model).getFileIDsResultSet(any(), (Instant) any(), (Instant) any(), any(), anyString());

        AllureTestUtils.addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        AllureTestUtils.addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        Assertions.assertEquals(fileids, progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertNull(progressResponse.getResultAddress());

        AllureTestUtils.addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileIDs().getFileID());
        Assertions.assertEquals(1,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(FILE_ID,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0)
                        .getFileID());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void goodCaseOperationAllFiles() throws Exception {
        addDescription("Tests the GetFileIDs operation on the pillar for the successful scenario, " +
                "when requesting all files.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        addStep("Setup for having the file and delivering result-set", "No failure here");
        Mockito.when(model.hasFileID(eq(FILE_ID), anyString())).thenReturn(true);
        Mockito.when(model.getPillarID()).thenReturn(settingsForCUT.getComponentID());
        Mockito.doAnswer(invocation -> {
            ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
            res.insertFileID(defaultFileId, Instant.EPOCH);
            res.insertFileID(nonDefaultFileId, Instant.now());
            return res;
        }).when(model).getFileIDsResultSet(any(), (Instant) any(),
                (Instant) any(), any(), anyString());

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        Assertions.assertEquals(fileids, progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertNull(finalResponse.getFileIDs().getFileID());
        Assertions.assertEquals(2,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void badCaseOperationNoFile() {
        addDescription("Tests the GetFileIDs functionality of the pillar for the failure scenario, where it does not " +
                "have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for not having the file", "Should cause the FILE_NOT_FOUND_FAILURE later.");
        Mockito.when(model.hasFileID(eq(FILE_ID), anyString())).thenReturn(false);
        Mockito.when(model.getPillarID()).thenReturn(settingsForCUT.getComponentID());

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null);
        messageBus.sendMessage(getFileIDsRequest);

        // No response, since failure
        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should tell about the error, and not contain the file.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileIDs().getFileID());
        Assertions.assertNull(finalResponse.getResultingFileIDs());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testRestrictions() throws Exception {
        addDescription("Tests that the restrictions are correctly passed on to the cache.");

        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        final Instant minInstant = Instant.ofEpochMilli(12345);
        final Instant maxInstant = Instant.now();
        final Long MAX_RESULTS = 12345L;

        Mockito.when(model.hasFileID(eq(FILE_ID), eq(collectionID))).thenReturn(true);
        Mockito.when(model.getPillarID()).thenReturn(settingsForCUT.getComponentID());
        addStep("Setup for only delivering result-set when the correct restrictions are given.",
                "No failure here");
        Mockito.doAnswer(invocation -> {
            ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
            res.insertFileID(FILE_ID, Instant.ofEpochMilli(1234567890));
            return res;
        }).when(model).getFileIDsResultSet(any(), (Instant) any(),
                (Instant) any(), any(), anyString());

        addStep("Create and send the actual GetFileIDs message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(fileids, null, MAX_RESULTS,
                CalendarUtils.getXmlGregorianCalendar(maxInstant),
                CalendarUtils.getXmlGregorianCalendar(minInstant));
        messageBus.sendMessage(getFileIDsRequest);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileIDsProgressResponse.class);
        Assertions.assertEquals(fileids, progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileIDs request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileIDsFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileIDsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertNull(finalResponse.getFileIDs().getFileID());
        Assertions.assertEquals(1,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
        Assertions.assertEquals(FILE_ID,
                finalResponse.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().get(0)
                        .getFileID());
    }
}
