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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.FileInfoStub;
import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetFileTest extends MockedPillarTest {
    private GetFileMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestinationId);
    }

    @SuppressWarnings("rawtypes")
    @Test( groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentification() throws Exception {
        addDescription("Tests the identification for a GetFile operation on the pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for having the file and delivering pillar id", 
                "Not throw an exception when calling the verifyFileExists method.");
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(FILE_ID);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
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
        addDescription("Tests the identification for a GetFile operation on the checksum pillar for the failure scenario, when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for throwing an exception when asked to verify file existence", 
                "Should cause the FILE_NOT_FOUND_FAILURE later.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws RequestHandlerException {
                throw new IdentifyContributorException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.");
            }
        }).when(model).verifyFileExists(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(FILE_ID);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileID(), FILE_ID);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    //@Test( groups = {"regressiontest", "pillartest"})
    // FAILS, when combined with other tests...
    public void badCaseOperationNoFile() throws Exception {
        addDescription("Tests the GetFile functionality of the pillar for the failure scenario, where it does not have the file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for throwing an exception when asked to verify file existence", 
                "Should cause the FILE_NOT_FOUND_FAILURE later.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) throws RequestHandlerException {
                throw new IdentifyContributorException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.");
            }
        }).when(model).verifyFileExists(eq(FILE_ID), anyString());
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return settingsForCUT.getComponentID();
            }
        }).when(model).getPillarID();

        addStep("Create and send the actual GetFile message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileRequest getFileRequest = msgFactory.createGetFileRequest(DEFAULT_DOWNLOAD_FILE_ADDRESS, FILE_ID);
        messageBus.sendMessage(getFileRequest);

        // No response, since failure
        addStep("Retrieve the FinalResponse for the GetFile request",
                "The final response should tell about the error, and not contain the file.");
        GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileID(), FILE_ID);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    //@Test( groups = {"regressiontest", "pillartest"})
    // FAILS, when combined with other tests...
    public void goodCaseOperation() throws Exception {
        addDescription("Tests the GetFile functionality of the pillar for the success scenario, where the file is uploaded.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;

        addStep("Setup for having the file and delevering a mock file.", 
                "Should make it possible to perform the whole operation without any exceptions.");
        doAnswer(invocation -> new FileInfoStub(FILE_ID, 0L, 0L, new ByteArrayInputStream(new byte[0]))).when(model).getFileInfoForActualFile(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        addStep("Create and send the actual GetFile message to the pillar.",
                "Should be received and handled by the pillar.");
        GetFileRequest getFileRequest = msgFactory.createGetFileRequest(DEFAULT_DOWNLOAD_FILE_ADDRESS, FILE_ID);
        messageBus.sendMessage(getFileRequest);

        addStep("Retrieve the ProgressResponse for the GetFile request",
                "The GetFile progress response should be sent by the pillar.");
        GetFileProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileProgressResponse.class);
        assertEquals(progressResponse.getFileID(), FILE_ID);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertEquals(progressResponse.getFileSize().longValue(), 0L);

        addStep("Retrieve the FinalResponse for the GetFile request",
                "The final response should tell about the error, and not contain the file.");
        GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileID(), FILE_ID);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 1, "Should create one audit trail for the GetFile operation");
    }
}
