/*
 * #%L
 * Bitrepository Reference Pillar
 *
 * $Id: ReplaceFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/ReplaceFileOnReferencePillarTest.java $
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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.integration.SuiteInfoParameterResolver;
import org.bitrepository.pillar.messagefactories.ReplaceFileMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

/**
 * Tests the ReplaceFile functionality on the ReferencePillar.
 */
@ExtendWith(SuiteInfoParameterResolver.class)
public class ReplaceFileTest extends MockedPillarTest {
    ReplaceFileMessageFactory msgFactory;
    Long FILE_SIZE = 1L;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new ReplaceFileMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void goodCaseIdentification() {
        addDescription("Tests the identification for a ReplaceFile operation on the pillar for the successful " +
                "scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;

        addStep("Setup for having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> true).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForReplaceFileRequest(FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, receivedIdentifyResponse.getFileID());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void badCaseIdentification() {
        addDescription("Tests the identification for a ReplaceFile operation on the pillar for the failure scenario, " +
                "when the file does not exist.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;

        addStep("Setup for not having the file and delivering pillar id",
                "Should return false, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> false).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForReplaceFileRequest(FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, receivedIdentifyResponse.getFileID());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void badCaseOperationMissingFile() {
        addDescription("Tests the ReplaceFile operation on the pillar for the failure scenario, when the file is " +
                "missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;

        addStep("Setup for not having the file and delivering pillar id",
                "Should return false, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> false).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        ReplaceFileRequest request = msgFactory.createReplaceFileRequest(csData, csData, null, null,
                defaultDownloadFileAddress, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the FinalResponse for the ReplaceFile request",
                "The final response should give file not found failure.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void badCaseOperationNoDestructiveChecksum() {
        addDescription("Tests the ReplaceFile operation on the pillar for the failure scenario, when no validation "
                + "checksum is given for the destructive action, but though is required.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;
        settingsForCUT.getRepositorySettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(true);

        addStep("Setup for having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> true).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        ReplaceFileRequest request = msgFactory.createReplaceFileRequest(null, csData, null, null,
                defaultDownloadFileAddress, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the FinalResponse for the ReplaceFile request",
                "The final response should give existing checksum failure.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());

        addStep("Pillar should have sent an alarm", "Alarm contains information about the missing verification " +
                "checksum");
        AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assertions.assertEquals(FILE_ID, alarm.getAlarm().getFileID());
        Assertions.assertEquals(getPillarID(), alarm.getAlarm().getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, alarm.getAlarm().getAlarmCode());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void badCaseOperationNoValidationChecksum() {
        addDescription("Tests the ReplaceFile operation on the pillar for the failure scenario, when no validation "
                + "checksum is given for the new file, but though is required.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;
        settingsForCUT.getRepositorySettings().getProtocolSettings().setRequireChecksumForNewFileRequests(true);

        addStep("Setup for having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> true).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        ReplaceFileRequest request = msgFactory.createReplaceFileRequest(csData, null, null, null,
                defaultDownloadFileAddress, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the FinalResponse for the ReplaceFile request",
                "The final response should give new file checksum failure.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.NEW_FILE_CHECKSUM_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());

        addStep("Pillar should have sent an alarm", "Alarm contains information about the missing verification " +
                "checksum");
        AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assertions.assertEquals(FILE_ID, alarm.getAlarm().getFileID());
        Assertions.assertEquals(getPillarID(), alarm.getAlarm().getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, alarm.getAlarm().getAlarmCode());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void badCaseOperationWrongDestructiveChecksum() throws Exception {
        addDescription("Tests the ReplaceFile operation on the pillar for the failure scenario, when the checksum for "
                + "the destructive action is different from the one in the cache.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;

        addStep("Setup for having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> true).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();
        Mockito.doAnswer(invocation -> NON_DEFAULT_MD5_CHECKSUM).when(model).getChecksumForFile(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class));

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        ReplaceFileRequest request = msgFactory.createReplaceFileRequest(csData, csData, null, null,
                defaultDownloadFileAddress, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the FinalResponse for the ReplaceFile request",
                "The final response should give existing file checksum failure.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());

        addStep("Pillar should have sent an alarm", "Alarm contains information about the missing verification " +
                "checksum");
        AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assertions.assertEquals(FILE_ID, alarm.getAlarm().getFileID());
        Assertions.assertEquals(getPillarID(), alarm.getAlarm().getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, alarm.getAlarm().getAlarmCode());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void goodCaseOperation() throws Exception {
        addDescription("Tests the ReplaceFile operation on the pillar for the success scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;

        addStep("Setup for already having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> true).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();
        Mockito.doAnswer(invocation -> DEFAULT_MD5_CHECKSUM).when(model).getChecksumForFile(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class));

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        ReplaceFileRequest request = msgFactory.createReplaceFileRequest(csData, csData, null, null,
                defaultDownloadFileAddress, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse = clientReceiver.waitForMessage(ReplaceFileProgressResponse.class);
        Assertions.assertEquals(FILE_ID, progressResponse.getFileID());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());

        addStep("Retrieve the FinalResponse for the ReplaceFile request",
                "The final response should say 'operation_complete', and give the requested data.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());
        Assertions.assertNull(finalResponse.getChecksumDataForNewFile());
        Assertions.assertNull(finalResponse.getChecksumDataForExistingFile());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(1, audits.getCallsForAuditEvent(), "Should make 1 put-file audit trail");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void goodCaseOperationWithChecksumsReturn() throws Exception {
        addDescription("Tests the ReplaceFile operation on the pillar for the success scenario, when requesting both " +
                "the cheksums of the file returned.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = defaultFileId;

        ChecksumSpecTYPE existingRequestChecksumSpec = otherCsSpec;
        ChecksumSpecTYPE newRequestChecksumSpec = csSpec;

        addStep("Setup for already having the file and delivering pillar id",
                "Should return true, when requesting file-id existence.");
        Mockito.doAnswer(invocation -> true).when(model).hasFileID(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();
        Mockito.doAnswer(invocation -> DEFAULT_MD5_CHECKSUM).when(model).getChecksumForFile(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class));
        Mockito.doAnswer(invocation -> {
            ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
            res.setChecksumSpec(otherCsSpec);
            res.setCalculationTimestamp(CalendarUtils.getNow());
            try {
                res.setChecksumValue(Base16Utils.encodeBase16(NON_DEFAULT_MD5_CHECKSUM));
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            return res;
        }).when(model).getChecksumDataForFile(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.eq(otherCsSpec));
        Mockito.doAnswer(invocation -> {
            ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
            res.setChecksumSpec(csSpec);
            res.setCalculationTimestamp(CalendarUtils.getNow());
            res.setChecksumValue(Base16Utils.encodeBase16(DEFAULT_MD5_CHECKSUM));
            return res;
        }).when(model).getChecksumDataForFile(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.eq(csSpec));

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        ReplaceFileRequest request = msgFactory.createReplaceFileRequest(csData, csData, existingRequestChecksumSpec,
                newRequestChecksumSpec, defaultDownloadFileAddress, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(request);

        addStep("Retrieve the ProgressResponse for the GetFileIDs request",
                "The GetFileIDs progress response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse = clientReceiver.waitForMessage(ReplaceFileProgressResponse.class);
        Assertions.assertEquals(FILE_ID, progressResponse.getFileID());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());

        addStep("Retrieve the FinalResponse for the ReplaceFile request",
                "The final response should say 'operation_complete', and give the requested data.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());
        Assertions.assertNotNull(finalResponse.getChecksumDataForNewFile());
        Assertions.assertEquals(newRequestChecksumSpec, finalResponse.getChecksumDataForNewFile().getChecksumSpec());
        Assertions.assertNotNull(finalResponse.getChecksumDataForExistingFile());
        Assertions.assertEquals(existingRequestChecksumSpec, finalResponse.getChecksumDataForExistingFile().getChecksumSpec());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(1, audits.getCallsForAuditEvent(), "Should make 1 put-file audit trail");
    }
}
