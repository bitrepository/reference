/*
 * #%L
 * Bitrepository Reference Pillar
 *
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org
 * /bitrepository/pillar/PutFileOnReferencePillarTest.java $
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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.FileInfoStub;
import org.bitrepository.pillar.messagefactories.GetChecksumsMessageFactory;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetChecksumsTest extends MockedPillarTest {
    private GetChecksumsMessageFactory msgFactory;
    private ChecksumSpecTYPE defaultChecksumSpec;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        defaultChecksumSpec = ChecksumUtils.getDefault(settingsForCUT);
        msgFactory = new GetChecksumsMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentification() {
        addDescription("Tests the identification for a GetChecksums operation on the pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file and delivering pillar id", "Should return true, when requesting file-id existence.");
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

        addStep("Create and send the identify request message.", "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void badCaseIdentification() {
        addDescription(
                "Tests the identification for a GetChecksums operation on the pillar for the failure scenario, when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for delivering pillar id and not having the file ", "Should return false, when requesting file-id existence.");
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

        addStep("Create and send the identify request message.", "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationSingleFile() throws Exception {
        addDescription("Tests the GetChecksums operation on the pillar for the successful scenario when requesting one specific file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(invocation -> true).when(model).verifyChecksumAlgorithm(any(ChecksumSpecTYPE.class));
        doAnswer(invocation -> true).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        doAnswer(invocation -> {
            String fileID = invocation.getArgument(0);
            if (invocation.getArgument(2).equals(defaultChecksumSpec)) {
                return new ChecksumEntry(fileID, DEFAULT_MD5_CHECKSUM, new Date());
            } else {
                String checksum = ChecksumUtils.generateChecksum(new FileInfoStub(fileID, null, 1L, null),
                        defaultChecksumSpec);
                return new ChecksumEntry(fileID, checksum, new Date());
            }
        }).when(model).getChecksumEntryForFile(eq(FILE_ID), nullable(String.class), any(ChecksumSpecTYPE.class));

        doAnswer(i -> {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            res.insertChecksumEntry(
                    model.getChecksumEntryForFile(i.getArgument(0), i.getArgument(1),
                            i.getArgument(4)));
            return res;
        }).when(model).getSingleChecksumResultSet(eq(FILE_ID), anyString(), nullable(XMLGregorianCalendar.class),
                nullable(XMLGregorianCalendar.class), any(ChecksumSpecTYPE.class));

        addStep("Create and send the actual GetChecksums message to the pillar.", "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request",
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse = clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 1);
        assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getFileID(), FILE_ID);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationAllFiles() throws Exception {
        addDescription("Tests the GetChecksums operation on the pillar for the successful scenario, when requesting all files.");
        addStep("Set up constants and variables.", "Should not fail here!");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();
        doAnswer(invocation -> {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            res.insertChecksumEntry(new ChecksumEntry(DEFAULT_FILE_ID, DEFAULT_MD5_CHECKSUM, new Date()));
            res.insertChecksumEntry(new ChecksumEntry(NON_DEFAULT_FILE_ID, NON_DEFAULT_MD5_CHECKSUM, new Date(0)));
            return res;
        }).when(model)
                .getChecksumResultSet(nullable(XMLGregorianCalendar.class), nullable(XMLGregorianCalendar.class), nullable(Long.class),
                        anyString(), nullable(ChecksumSpecTYPE.class));

        addStep("Create and send the actual GetChecksums message to the pillar.", "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request",
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse = clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 2);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void badCaseOperationNoFile() {
        addDescription("Tests the GetChecksums functionality of the pillar for the failure scenario, where it does not have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for not having the file", "Should cause the FILE_NOT_FOUND_FAILURE later.");
        doAnswer(invocation -> false).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        addStep("Create and send the actual GetChecksums message to the pillar.", "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        // No response, since failure
        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should tell about the error, and not contain the file.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertNull(finalResponse.getResultingChecksums());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void testRestrictions() throws Exception {
        addDescription("Tests that the restrictions are correctly passed on to the cache.");

        addStep("Set up constants and variables.", "Should not fail here!");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        final XMLGregorianCalendar MIN_DATE = CalendarUtils.getXmlGregorianCalendar(new Date(12345));
        final XMLGregorianCalendar MAX_DATE = CalendarUtils.getXmlGregorianCalendar(new Date());
        final Long MAX_RESULTS = 12345L;

        doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();
        addStep("Setup for only delivering result-set when the correct restrictions are given.", "No failure here");
        doAnswer(invocation -> {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            res.insertChecksumEntry(new ChecksumEntry(DEFAULT_FILE_ID, DEFAULT_MD5_CHECKSUM, new Date()));
            return res;
        }).when(model).getChecksumResultSet(eq(MIN_DATE), eq(MAX_DATE), eq(MAX_RESULTS), eq(collectionID), eq(csSpec));

        addStep("Create and send the actual GetChecksums message to the pillar.", "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null, MIN_DATE, MAX_DATE,
                MAX_RESULTS);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request",
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse = clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 1);
        assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getFileID(), DEFAULT_FILE_ID);
    }
}
