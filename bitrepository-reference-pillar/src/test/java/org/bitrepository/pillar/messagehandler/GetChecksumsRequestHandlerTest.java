package org.bitrepository.pillar.messagehandler;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.service.exception.InvalidMessageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.CLIENT_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.CORRELATION_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.FILE_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.PILLAR_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.assertResponseCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetChecksumsRequestHandlerTest {
    private static final String OTHER_FILE_ID = "other-test-file";

    @TempDir
    Path tempDir;

    private MessageHandlerFixture fixture;
    private StorageModel model;
    private String collectionID;
    private GetChecksumsRequestHandler handler;
    private ChecksumSpecTYPE checksumSpec;

    @BeforeEach
    void setUp() {
        fixture = new MessageHandlerFixture(tempDir);
        model = fixture.model();
        collectionID = fixture.collectionID();
        handler = new GetChecksumsRequestHandler(fixture.context(), model);
        checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void returnsChecksumsForAllFilesInTheFinalResponse() throws Exception {
        when(model.getChecksumResultSet((Instant) null, null, null, collectionID, checksumSpec))
                .thenReturn(resultSet(FILE_ID, OTHER_FILE_ID));

        handler.processRequest(createRequest(allFiles()), fixture.messageContext());

        List<MessageResponse> responses = fixture.dispatchedResponses();
        Assertions.assertEquals(2, responses.size());
        Assertions.assertEquals(ResponseCode.OPERATION_ACCEPTED_PROGRESS,
                ((GetChecksumsProgressResponse) responses.get(0)).getResponseInfo().getResponseCode());
        GetChecksumsFinalResponse finalResponse = (GetChecksumsFinalResponse) responses.get(1);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(List.of(FILE_ID, OTHER_FILE_ID), deliveredFileIDs(finalResponse));
        Assertions.assertFalse(finalResponse.isPartialResult());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void returnsChecksumForTheSingleRequestedFile() throws Exception {
        when(model.hasFileID(FILE_ID, collectionID)).thenReturn(true);
        when(model.getSingleChecksumResultSet(FILE_ID, collectionID, (Instant) null, null, checksumSpec))
                .thenReturn(resultSet(FILE_ID));

        handler.processRequest(createRequest(singleFile()), fixture.messageContext());

        GetChecksumsFinalResponse finalResponse = (GetChecksumsFinalResponse) fixture.dispatchedResponses().get(1);
        Assertions.assertEquals(List.of(FILE_ID), deliveredFileIDs(finalResponse));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void passesRestrictionsToTheModelAndMarksPartialResults() throws Exception {
        Instant minTime = Instant.parse("2020-01-01T00:00:00Z");
        Instant maxTime = Instant.parse("2021-01-01T00:00:00Z");
        ExtractedChecksumResultSet results = resultSet(FILE_ID);
        results.reportMoreEntriesFound();
        when(model.getChecksumResultSet(minTime, maxTime, 1L, collectionID, checksumSpec)).thenReturn(results);
        GetChecksumsRequest request = createRequest(allFiles());
        request.setMinTimestamp(CalendarUtils.getXmlGregorianCalendar(minTime));
        request.setMaxTimestamp(CalendarUtils.getXmlGregorianCalendar(maxTime));
        request.setMaxNumberOfResults(BigInteger.ONE);

        handler.processRequest(request, fixture.messageContext());

        GetChecksumsFinalResponse finalResponse = (GetChecksumsFinalResponse) fixture.dispatchedResponses().get(1);
        Assertions.assertTrue(finalResponse.isPartialResult());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void unsupportedChecksumSpecIsRejectedBeforeAnyResponse() throws Exception {
        doThrow(new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED, "Unsupported checksum"))
                .when(model).verifyChecksumAlgorithm(checksumSpec);

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(createRequest(allFiles()), fixture.messageContext()));

        assertResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED, e);
        verifyNoInteractions(fixture.responseDispatcher());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void unknownFileIsRejectedBeforeAnyResponse() {
        when(model.hasFileID(FILE_ID, collectionID)).thenReturn(false);

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(createRequest(singleFile()), fixture.messageContext()));

        assertResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE, e);
        verifyNoInteractions(fixture.responseDispatcher());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void deliversResultsToTheResultAddress() throws Exception {
        when(model.getChecksumResultSet((Instant) null, null, null, collectionID, checksumSpec))
                .thenReturn(resultSet(FILE_ID, OTHER_FILE_ID));
        GetChecksumsRequest request = createRequest(allFiles());
        request.setResultAddress(fixture.deliveryAddress("checksums-result"));

        handler.processRequest(request, fixture.messageContext());

        Assertions.assertEquals(2, fixture.dispatchedResponses().size());
        GetChecksumsFinalResponse finalResponse = (GetChecksumsFinalResponse) fixture.dispatchedResponses().getLast();
        Assertions.assertEquals(request.getResultAddress(),
                finalResponse.getResultingChecksums().getResultAddress());
        Assertions.assertTrue(finalResponse.getResultingChecksums().getChecksumDataItems().isEmpty(),
                "Actual: " +  finalResponse.getResultingChecksums().getChecksumDataItems());
        String delivered = Files.readString(tempDir.resolve("checksums-result"));
        Assertions.assertTrue(delivered.contains(FILE_ID) && delivered.contains(OTHER_FILE_ID),
                "Delivered result should contain both file IDs: " + delivered);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void failedResultDeliveryGivesFileTransferFailure() throws Exception {
        when(model.getChecksumResultSet((Instant) null, null, null, collectionID, checksumSpec))
                .thenReturn(resultSet(FILE_ID));
        GetChecksumsRequest request = createRequest(allFiles());
        request.setResultAddress(fixture.undeliverableAddress());

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(request, fixture.messageContext()));

        assertResponseCode(ResponseCode.FILE_TRANSFER_FAILURE, e);
        Assertions.assertEquals(1, fixture.dispatchedResponses().size(),
                "Only the progress response should have been sent");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void failedResponseDescribesTheRequest() {
        GetChecksumsRequest request = createRequest(singleFile());

        GetChecksumsFinalResponse response = (GetChecksumsFinalResponse) handler.generateFailedResponse(request);

        Assertions.assertEquals(checksumSpec, response.getChecksumRequestForExistingFile());
        Assertions.assertEquals(PILLAR_ID, response.getPillarID());
    }

    private GetChecksumsRequest createRequest(FileIDs fileIDs) {
        GetChecksumsRequest request = new GetChecksumsRequest();
        request.setCollectionID(collectionID);
        request.setPillarID(PILLAR_ID);
        request.setFileIDs(fileIDs);
        request.setChecksumRequestForExistingFile(checksumSpec);
        request.setCorrelationID(CORRELATION_ID);
        request.setFrom(CLIENT_ID);
        return request;
    }

    private static FileIDs allFiles() {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        return fileIDs;
    }

    private static FileIDs singleFile() {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(FILE_ID);
        return fileIDs;
    }

    private static ExtractedChecksumResultSet resultSet(String... fileIDs) {
        ExtractedChecksumResultSet results = new ExtractedChecksumResultSet();
        for (String fileID : fileIDs) {
            ChecksumDataForChecksumSpecTYPE entry = new ChecksumDataForChecksumSpecTYPE();
            entry.setFileID(fileID);
            entry.setChecksumValue("checksum".getBytes(StandardCharsets.UTF_8));
            entry.setCalculationTimestamp(CalendarUtils.getEpoch());
            results.insertChecksumEntry(entry);
        }
        return results;
    }

    private static List<String> deliveredFileIDs(GetChecksumsFinalResponse response) {
        return response.getResultingChecksums().getChecksumDataItems().stream()
                .map(ChecksumDataForChecksumSpecTYPE::getFileID)
                .toList();
    }
}
