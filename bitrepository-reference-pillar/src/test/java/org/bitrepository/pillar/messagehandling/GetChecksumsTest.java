/*
 * #%L
 * Bitrepository Reference Pillar
 *
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src
 * /test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
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

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.messagefactories.GetChecksumsMessageFactory;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
@ExtendWith(SuiteInfoParameterResolver.class)
public class GetChecksumsTest extends MockedPillarTest {
    private GetChecksumsMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new GetChecksumsMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void goodCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetChecksums operation on the pillar for the successful " +
                "scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
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
    public void badCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetChecksums operation on the pillar for the failure scenario," +
                " when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for delivering pillar id and not having the file ",
                "Should return false, when requesting file-id existence.");
        Mockito.doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
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
    public void goodCaseOperationSingleFile() throws Exception {
        addDescription("Tests the GetChecksums operation on the pillar for the successful scenario when requesting " +
                "one specific file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        addStep("Setup for having the file and delivering result-set", "No failure here");
        Mockito.doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return true;
            }
        }).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();
        Mockito.doAnswer(new Answer() {
            public ExtractedChecksumResultSet answer(InvocationOnMock invocation) {
                ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
                res.insertChecksumEntry(new ChecksumEntry(FILE_ID, DEFAULT_MD5_CHECKSUM, new Date()));
                return res;

            }
        }).when(model).getSingleChecksumResultSet(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(ChecksumSpecTYPE.class));

        addStep("Create and send the actual GetChecksums message to the pillar.",
                "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request",
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse =
                clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        Assertions.assertEquals(fileids, progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(1, finalResponse.getResultingChecksums().getChecksumDataItems().size());
        Assertions.assertEquals(FILE_ID,
                finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getFileID());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void goodCaseOperationAllFiles() throws Exception {
        addDescription("Tests the GetChecksums operation on the pillar for the successful scenario, " +
                "when requesting all files.");
        addStep("Set up constants and variables.", "Should not fail here!");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        addStep("Setup for having the file and delivering result-set", "No failure here");
        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();
        Mockito.doAnswer(new Answer() {
            public ExtractedChecksumResultSet answer(InvocationOnMock invocation) {
                ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
                res.insertChecksumEntry(new ChecksumEntry(defaultFileId, DEFAULT_MD5_CHECKSUM, new Date()));
                res.insertChecksumEntry(new ChecksumEntry(nonDefaultFileId, NON_DEFAULT_MD5_CHECKSUM, new Date(0)));
                return res;
            }
        }).when(model).getChecksumResultSet(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(ChecksumSpecTYPE.class));

        addStep("Create and send the actual GetChecksums message to the pillar.",
                "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request",
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse =
                clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        Assertions.assertEquals(fileids, progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(2, finalResponse.getResultingChecksums().getChecksumDataItems().size());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void badCaseOperationNoFile() throws Exception {
        addDescription("Tests the GetChecksums functionality of the pillar for the failure scenario, where it does " +
                "not have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for not having the file", "Should cause the FILE_NOT_FOUND_FAILURE later.");
        Mockito.doAnswer(new Answer() {
            public Boolean answer(InvocationOnMock invocation) {
                return false;
            }
        }).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the actual GetChecksums message to the pillar.",
                "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        // No response, since failure
        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should tell about the error, and not contain the file.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertNull(finalResponse.getResultingChecksums());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testRestrictions() throws Exception {
        addDescription("Tests that the restrictions are correctly passed on to the cache.");

        addStep("Set up constants and variables.", "Should not fail here!");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        final XMLGregorianCalendar MIN_DATE = CalendarUtils.getXmlGregorianCalendar(new Date(12345));
        final XMLGregorianCalendar MAX_DATE = CalendarUtils.getXmlGregorianCalendar(new Date());
        final Long MAX_RESULTS = 12345L;

        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();
        addStep("Setup for only delivering result-set when the correct restrictions are given.",
                "No failure here");
        Mockito.doAnswer(new Answer() {
            public ExtractedChecksumResultSet answer(InvocationOnMock invocation) {
                ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
                res.insertChecksumEntry(new ChecksumEntry(defaultFileId, DEFAULT_MD5_CHECKSUM, new Date()));
                return res;
            }
        }).when(model).getChecksumResultSet(ArgumentMatchers.eq(MIN_DATE), ArgumentMatchers.eq(MAX_DATE),
                ArgumentMatchers.eq(MAX_RESULTS), ArgumentMatchers.eq(collectionID), ArgumentMatchers.eq(csSpec));

        addStep("Create and send the actual GetChecksums message to the pillar.",
                "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(csSpec, fileids, null,
                MIN_DATE, MAX_DATE, MAX_RESULTS);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request",
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse =
                clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        Assertions.assertEquals(fileids, progressResponse.getFileIDs());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());
        Assertions.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(1, finalResponse.getResultingChecksums().getChecksumDataItems().size());
        Assertions.assertEquals(defaultFileId,
                finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getFileID());
    }
}
