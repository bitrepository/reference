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

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetFileInfosFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileInfosResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.FileInfoStub;
import org.bitrepository.pillar.messagefactories.GetFileInfosMessageFactory;
import org.bitrepository.pillar.store.FileStorageModel;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the GetFileInfos functionality on the ReferencePillar.
 */
public class GetFileInfosTest extends MockedPillarTest {
    private ChecksumSpecTYPE defaultChecksumSpec;
    private GetFileInfosMessageFactory msgFactory;
    private FileStorageModel fileStorage;
    private StorageModel storageModel;

    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        storageModel = model;
        fileStorage = mock(FileStorageModel.class);
        defaultChecksumSpec = ChecksumUtils.getDefault(settingsForCUT);
        msgFactory = new GetFileInfosMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestinationId);
    }

    @BeforeMethod
    public void resetMock() {
        reset(model, fileStorage, fileExchangeMock);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentification() {
        addDescription("Tests the identification for a GetFileInfos operation on the pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file and delivering pillar id", "Should return true, when requesting file-id existence.");
        doAnswer(invocation -> true).when(storageModel).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();

        addStep("Create and send the identify request message.", "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileInfosRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileInfosRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForGetFileInfosResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseIdentificationAllFiles() {
        addDescription(
                "Tests the identification for a GetFileInfos operation on the pillar for the successful scenario when requesting all " +
                        "files.");
        addStep("Set up constants and variables.", "Should not fail here!");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        addStep("Setup for having the file and delivering pillar id", "Should return true, when requesting file-id existence.");
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();

        addStep("Create and send the identify request message.", "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileInfosRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileInfosRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForGetFileInfosResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosResponse.class);
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
                "Tests the identification for a GetFileInfos operation on the pillar for the failure scenario, when the file is missing.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for delivering pillar id and not having the file ", "Should return false, when requesting file-id existence.");
        doAnswer(invocation -> false).when(storageModel).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();

        addStep("Create and send the identify request message.", "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileInfosRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileInfosRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForGetFileInfosResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void badCsSpecIdentification() throws Exception {
        addDescription("Tests the identification for a GetFileInfos operation on the pillar for the failure scenario, when the checksum " +
                "specification is not supported.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for delivering pillar id and not having the file ", "Should return true, when requesting file-id existence.");
        doAnswer(invocation -> true).when(storageModel).hasFileID(eq(FILE_ID), nullable(String.class));
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();
        doThrow(new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED, "Test failure")).when(storageModel)
                .verifyChecksumAlgorithm(eq(csSpec));

        addStep("Create and send the identify request message.", "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileInfosRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileInfosRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForGetFileInfosResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileInfosResponse.class);
        assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.REQUEST_NOT_SUPPORTED);
        assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationSingleFileInfos() throws RequestHandlerException {
        addDescription("Tests the GetFileInfos operation on the pillar for the successful scenario when requesting one specific file.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Setup for having the file", "No failure here");
        doAnswer(invocation -> true).when(storageModel).verifyChecksumAlgorithm(any(ChecksumSpecTYPE.class));
        doAnswer(invocation -> true).when(storageModel).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();
        doNothing().when(fileStorage).verifyFileExists(eq(FILE_ID), eq(collectionID));


        addStep("Setup for SingleChecksumResultSet", "No failure here");
        doAnswer(invocation -> {
            String fileID = invocation.getArgument(0);
            if (invocation.getArgument(2).equals(defaultChecksumSpec)) {
                return new ChecksumEntry(fileID, DEFAULT_MD5_CHECKSUM, new Date());
            } else {
                String checksum = ChecksumUtils.generateChecksum(new FileInfoStub(fileID, null, 1L, null),
                        defaultChecksumSpec);
                return new ChecksumEntry(fileID, checksum, new Date());
            }
        }).when(storageModel).getChecksumEntryForFile(eq(FILE_ID), nullable(String.class), nullable(ChecksumSpecTYPE.class));

        doAnswer(invocation -> {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            res.insertChecksumEntry(
                    storageModel.getChecksumEntryForFile(invocation.getArgument(0), invocation.getArgument(1),
                            invocation.getArgument(4)));
            return res;
        }).when(storageModel).getSingleChecksumResultSet(eq(FILE_ID), anyString(), nullable(XMLGregorianCalendar.class),
                nullable(XMLGregorianCalendar.class), nullable(ChecksumSpecTYPE.class));

        doAnswer(arguments -> {
            ChecksumDataForChecksumSpecTYPE cs = (ChecksumDataForChecksumSpecTYPE) arguments.getArguments()[0];
            FileInfosDataItem res = new FileInfosDataItem();
            res.setCalculationTimestamp(cs.getCalculationTimestamp());
            res.setChecksumValue(cs.getChecksumValue());
            res.setFileID(cs.getFileID());
            res.setLastModificationTime(cs.getCalculationTimestamp());
            res.setFileSize(BigInteger.ONE);

            return res;
        }).when(storageModel).getFileInfosDataItemFromChecksumDataItem(any(ChecksumDataForChecksumSpecTYPE.class), anyString());

        addStep("Create and send the actual GetFileInfos message to the pillar.", "Should be received and handled by the pillar.");
        GetFileInfosRequest getFileInfosRequest = msgFactory.createGetFileInfosRequest(csSpec, fileids, null);
        messageBus.sendMessage(getFileInfosRequest);

        addStep("Retrieve the ProgressResponse for the GetFileInfos request",
                "The GetFileInfos progress response should be sent by the pillar.");
        GetFileInfosProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileInfosProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileInfos request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileInfosFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileInfosFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        assertEquals(finalResponse.getResultingFileInfos().getFileInfosDataItem().size(), 1);
        assertEquals(finalResponse.getResultingFileInfos().getFileInfosDataItem().get(0).getFileID(), FILE_ID);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationFileDateTooEarly() throws Exception {
        addDescription(
                "Tests the GetFileInfos operation on the pillar for the successful scenario when requesting one specific file, but the " +
                        "file date is too early.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(invocation -> true).when(storageModel).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();
        doAnswer(invocation -> {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            res.insertChecksumEntry(new ChecksumEntry(FILE_ID, DEFAULT_MD5_CHECKSUM, new Date()));
            return res;
        }).when(storageModel).getSingleChecksumResultSet(eq(FILE_ID), anyString(), nullable(XMLGregorianCalendar.class),
                nullable(XMLGregorianCalendar.class), nullable(ChecksumSpecTYPE.class));
        doAnswer(invocation -> new FileInfoStub(FILE_ID, System.currentTimeMillis(), 1L, null)).when(storageModel)
                .getFileInfoForActualFile(eq(FILE_ID), anyString());

        addStep("Create and send the actual GetFileInfos message to the pillar.", "Should be received and handled by the pillar.");
        GetFileInfosRequest getFileInfosRequest = msgFactory.createGetFileInfosRequest(csSpec, fileids, null, 100L, null, null, null,
                CalendarUtils.getFromMillis(System.currentTimeMillis() + 123456789));
        messageBus.sendMessage(getFileInfosRequest);

        addStep("Retrieve the ProgressResponse for the GetFileInfos request",
                "The GetFileInfos progress response should be sent by the pillar.");
        GetFileInfosProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileInfosProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileInfos request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileInfosFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileInfosFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        assertEquals(finalResponse.getResultingFileInfos().getFileInfosDataItem().size(), 0);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void goodCaseOperationFileDateTooLate() throws Exception {
        addDescription(
                "Tests the GetFileInfos operation on the pillar for the successful scenario when requesting one specific file, but the " +
                        "file date is too late.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(invocation -> true).when(storageModel).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();

        doAnswer(invocation -> {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            res.insertChecksumEntry(new ChecksumEntry(FILE_ID, DEFAULT_MD5_CHECKSUM, new Date()));
            return res;
        }).when(storageModel).getSingleChecksumResultSet(eq(FILE_ID), anyString(), nullable(XMLGregorianCalendar.class),
                nullable(XMLGregorianCalendar.class), nullable(ChecksumSpecTYPE.class));

        doAnswer(invocation -> new FileInfoStub(FILE_ID, System.currentTimeMillis(), 1L, null)).when(storageModel)
                .getFileInfoForActualFile(eq(FILE_ID), anyString());
        addStep("Create and send the actual GetFileInfos message to the pillar.", "Should be received and handled by the pillar.");
        GetFileInfosRequest getFileInfosRequest = msgFactory.createGetFileInfosRequest(csSpec, fileids, null, 100L, null, null,
                CalendarUtils.getFromMillis(System.currentTimeMillis() - 123456789), null);
        messageBus.sendMessage(getFileInfosRequest);

        addStep("Retrieve the ProgressResponse for the GetFileInfos request",
                "The GetFileInfos progress response should be sent by the pillar.");
        GetFileInfosProgressResponse progressResponse = clientReceiver.waitForMessage(GetFileInfosProgressResponse.class);
        assertEquals(progressResponse.getFileIDs(), fileids);
        assertEquals(progressResponse.getPillarID(), getPillarID());
        assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetFileInfos request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileInfosFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileInfosFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
        assertEquals(finalResponse.getResultingFileInfos().getFileInfosDataItem().size(), 0);
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = {"regressiontest", "pillartest"})
    public void badCaseOperationFileMissing() {
        addDescription(
                "Tests the GetFileInfos operation on the pillar for the failure scenario when requesting one specific file, which does " +
                        "not exist.");
        addStep("Set up constants and variables.", "Should not fail here!");
        final String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        addStep("Setup for having the file and delivering result-set", "No failure here");
        doAnswer(invocation -> false).when(storageModel).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(storageModel).getPillarID();

        addStep("Create and send the actual GetFileInfos message to the pillar.", "Should be received and handled by the pillar.");
        GetFileInfosRequest getFileInfosRequest = msgFactory.createGetFileInfosRequest(csSpec, fileids, null, 100L, null, null,
                CalendarUtils.getFromMillis(System.currentTimeMillis() - 123456789), null);
        messageBus.sendMessage(getFileInfosRequest);

        addStep("Retrieve the FinalResponse for the GetFileInfos request",
                "The final response should say 'operation_complete', and give the requested data.");
        GetFileInfosFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileInfosFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        assertEquals(finalResponse.getPillarID(), getPillarID());
        assertEquals(finalResponse.getFileIDs().getFileID(), FILE_ID);
    }
}