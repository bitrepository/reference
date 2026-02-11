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

import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class DeleteFileTest extends MockedPillarTest {
    private DeleteFileMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(),
                pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void goodCaseIdentification() throws Exception {
        addDescription("Tests the identification for a DeleteFile operation on the pillar for the successful scenario" +
                ".");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;

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
        IdentifyPillarsForDeleteFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForDeleteFileRequest(FILE_ID);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
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
    public void badCaseIdentification() throws Exception {
        addDescription("Tests the identification for a DeleteFile operation on the checksum pillar for the failure " +
                "scenario, when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;

        addStep("Setup for delivering pillar id and not having the file ",
                "Returns false, when requesting file-id existence.");
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
        IdentifyPillarsForDeleteFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForDeleteFileRequest(FILE_ID);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
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
    public void badCaseOperationNoFile() throws Exception {
        addDescription("Tests the DeleteFile functionality of the pillar for the failure scenario, where it does not " +
                "have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;

        addStep("Setup for delivering pillar id and not having the file ",
                "Returns false, when requesting file-id existence.");
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

        addStep("Create and send the actual DeleteFile message to the pillar.",
                "Should be received and handled by the pillar.");
        DeleteFileRequest DeleteFileRequest = msgFactory.createDeleteFileRequest(csData, csSpec, FILE_ID);
        messageBus.sendMessage(DeleteFileRequest);

        // No response, since failure
        addStep("Retrieve the FinalResponse for the DeleteFile request",
                "The final response should tell about the error, and not contain the file.");
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
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
    public void badCaseOperationMissingVerification() throws Exception {
        addDescription("Tests the DeleteFile functionality of the pillar for the failure scenario, where it does not " +
                "have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;

        addStep("Setup for delivering pillar id and having the file ",
                "Returns true, when requesting file-id existence.");
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

        addStep("Create and send the actual DeleteFile message to the pillar.",
                "Should be received and handled by the pillar.");
        DeleteFileRequest DeleteFileRequest = msgFactory.createDeleteFileRequest(null, csSpec, FILE_ID);
        messageBus.sendMessage(DeleteFileRequest);

        // No response, since failure
        addStep("Retrieve the FinalResponse for the DeleteFile request",
                "The final response should tell about the error, and not contain the file.");
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());

        addStep("Pillar should have sent an alarm", "Alarm contains information about the missing verification " +
                "checksum");
        AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assertions.assertEquals(FILE_ID, alarm.getAlarm().getFileID());
        Assertions.assertEquals(getPillarID(), alarm.getAlarm().getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, alarm.getAlarm().getAlarmCode());

        Assertions.assertEquals(0, audits.getCallsForAuditEvent(), "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    //@Test
//    @Tag("regressiontest", "pillartest"})
    // FAILS, when combined with other tests...
    public void goodCaseOperation() throws Exception {
        addDescription("Tests the DeleteFile functionality of the pillar for the success scenario, where the file is " +
                "uploaded.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = defaultFileId + testMethodName;

        addStep("Setup for delivering pillar id, having the file with expected checksum, and no errors when deleting " +
                        "file.",
                "Returns true, when requesting file-id existence.");
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
            public String answer(InvocationOnMock invocation) {
                return DEFAULT_MD5_CHECKSUM;
            }
        }).when(model).getChecksumForFile(ArgumentMatchers.eq(FILE_ID), ArgumentMatchers.anyString(), ArgumentMatchers.any(ChecksumSpecTYPE.class));

        addStep("Create and send the actual DeleteFile message to the pillar.",
                "Should be received and handled by the pillar.");
        DeleteFileRequest DeleteFileRequest = msgFactory.createDeleteFileRequest(csData, csSpec, FILE_ID);
        messageBus.sendMessage(DeleteFileRequest);

        addStep("Retrieve the ProgressResponse for the DeleteFile request",
                "The DeleteFile progress response should be sent by the pillar.");
        DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class);
        Assertions.assertEquals(FILE_ID, progressResponse.getFileID());
        Assertions.assertEquals(getPillarID(), progressResponse.getPillarID());

        addStep("Retrieve the FinalResponse for the DeleteFile request",
                "The final response should tell about the error, and not contain the file.");
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(getPillarID(), finalResponse.getPillarID());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assertions.assertEquals(1, audits.getCallsForAuditEvent(), "Should create one audit trail for the DeleteFile operation");
    }
}
