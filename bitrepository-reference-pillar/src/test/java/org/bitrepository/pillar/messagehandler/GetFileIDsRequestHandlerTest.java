package org.bitrepository.pillar.messagehandler;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.service.exception.InvalidMessageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.CLIENT_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.CORRELATION_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.FILE_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.PILLAR_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.assertResponseCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetFileIDsRequestHandlerTest {
    private static final String OTHER_FILE_ID = "other-test-file";

    @TempDir
    Path tempDir;

    private MessageHandlerFixture fixture;
    private StorageModel model;
    private String collectionID;
    private GetFileIDsRequestHandler handler;

    @BeforeEach
    void setUp() {
        fixture = new MessageHandlerFixture(tempDir);
        model = fixture.model();
        collectionID = fixture.collectionID();
        handler = new GetFileIDsRequestHandler(fixture.context(), model);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void returnsAllFileIDsInTheFinalResponse() throws Exception {
        when(model.getFileIDsResultSet(null, (Instant) null, null, null, collectionID))
                .thenReturn(resultSet(FILE_ID, OTHER_FILE_ID));

        handler.processRequest(createRequest(allFiles()), fixture.messageContext());

        List<MessageResponse> responses = fixture.dispatchedResponses();
        Assertions.assertEquals(2, responses.size());
        Assertions.assertEquals(ResponseCode.OPERATION_ACCEPTED_PROGRESS,
                ((GetFileIDsProgressResponse) responses.get(0)).getResponseInfo().getResponseCode());
        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) responses.get(1);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(List.of(FILE_ID, OTHER_FILE_ID), deliveredFileIDs(finalResponse));
        Assertions.assertNull(finalResponse.isPartialResult());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void returnsTheSingleRequestedFileID() throws Exception {
        when(model.hasFileID(FILE_ID, collectionID)).thenReturn(true);
        when(model.getFileIDsResultSet(FILE_ID, (Instant) null, null, null, collectionID)).thenReturn(resultSet(FILE_ID));

        handler.processRequest(createRequest(singleFile()), fixture.messageContext());

        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) fixture.dispatchedResponses().get(1);
        Assertions.assertEquals(List.of(FILE_ID), deliveredFileIDs(finalResponse));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void passesRestrictionsToTheModelAndMarksPartialResults() throws Exception {
        Instant minTime = Instant.parse("2020-01-01T00:00:00Z");
        Instant maxTime = Instant.parse("2021-01-01T00:00:00Z");
        ExtractedFileIDsResultSet results = resultSet(FILE_ID);
        results.reportMoreEntriesFound();
        when(model.getFileIDsResultSet(null, minTime, maxTime, 1L, collectionID)).thenReturn(results);
        GetFileIDsRequest request = createRequest(allFiles());
        request.setMinTimestamp(CalendarUtils.getXmlGregorianCalendar(minTime));
        request.setMaxTimestamp(CalendarUtils.getXmlGregorianCalendar(maxTime));
        request.setMaxNumberOfResults(BigInteger.ONE);

        handler.processRequest(request, fixture.messageContext());

        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) fixture.dispatchedResponses().get(1);
        Assertions.assertEquals(Boolean.TRUE, finalResponse.isPartialResult());
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
        when(model.getFileIDsResultSet(null, (Instant) null, null, null, collectionID))
                .thenReturn(resultSet(FILE_ID, OTHER_FILE_ID));
        GetFileIDsRequest request = createRequest(allFiles());
        request.setResultAddress(fixture.deliveryAddress("fileids-result"));

        handler.processRequest(request, fixture.messageContext());

        GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) fixture.dispatchedResponses().get(1);
        Assertions.assertEquals(request.getResultAddress(),
                finalResponse.getResultingFileIDs().getResultAddress());
        Assertions.assertNull(finalResponse.getResultingFileIDs().getFileIDsData());
        String delivered = Files.readString(tempDir.resolve("fileids-result"));
        Assertions.assertTrue(delivered.contains(FILE_ID) && delivered.contains(OTHER_FILE_ID),
                "Delivered result should contain both file IDs: " + delivered);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void failedResultDeliveryGivesFileTransferFailure() {
        when(model.getFileIDsResultSet(null, (Instant) null, null, null, collectionID)).thenReturn(resultSet(FILE_ID));
        GetFileIDsRequest request = createRequest(allFiles());
        request.setResultAddress(fixture.undeliverableAddress());

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(request, fixture.messageContext()));

        assertResponseCode(ResponseCode.FILE_TRANSFER_FAILURE, e);
        Assertions.assertEquals(1, fixture.dispatchedResponses().size(), "Only the progress response should have been sent");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void failedResponseDescribesTheRequest() {
        GetFileIDsRequest request = createRequest(singleFile());

        GetFileIDsFinalResponse response = (GetFileIDsFinalResponse) handler.generateFailedResponse(request);

        Assertions.assertEquals(request.getFileIDs(), response.getFileIDs());
        Assertions.assertEquals(PILLAR_ID, response.getPillarID());
        verify(model).getPillarID();
        verifyNoInteractions(fixture.responseDispatcher());
    }

    private GetFileIDsRequest createRequest(FileIDs fileIDs) {
        GetFileIDsRequest request = new GetFileIDsRequest();
        request.setCollectionID(collectionID);
        request.setPillarID(PILLAR_ID);
        request.setFileIDs(fileIDs);
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

    private static ExtractedFileIDsResultSet resultSet(String... fileIDs) {
        ExtractedFileIDsResultSet results = new ExtractedFileIDsResultSet();
        for (String fileID : fileIDs) {
            results.insertFileID(fileID, BigInteger.TEN, CalendarUtils.getEpoch());
        }
        return results;
    }

    private static List<String> deliveredFileIDs(GetFileIDsFinalResponse response) {
        return response.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().stream()
                .map(FileIDsDataItem::getFileID)
                .toList();
    }
}
