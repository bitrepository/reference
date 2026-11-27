package org.bitrepository.pillar.messagehandler;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.filestore.DefaultFileInfo;
import org.bitrepository.pillar.common.FileInfoStub;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.service.exception.InvalidMessageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.CLIENT_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.CORRELATION_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.FILE_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.PILLAR_ID;
import static org.bitrepository.pillar.messagehandler.MessageHandlerFixture.assertResponseCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetFileRequestHandlerTest {
    private static final String FILE_CONTENT = "abcdefgh";

    @TempDir
    Path tempDir;

    private MessageHandlerFixture fixture;
    private StorageModel model;
    private String collectionID;
    private GetFileRequestHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        fixture = new MessageHandlerFixture(tempDir);
        model = fixture.model();
        collectionID = fixture.collectionID();
        handler = new GetFileRequestHandler(fixture.context(), model);
        byte[] content = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        when(model.getFileInfoForActualFile(FILE_ID, collectionID)).thenAnswer(invocation ->
                new FileInfoStub(FILE_ID, 0L, (long) content.length, new ByteArrayInputStream(content)));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void uploadsFileAndSendsProgressAndFinalResponse() throws Exception {
        GetFileRequest request = createRequest(fixture.deliveryAddress("delivered"));

        handler.processRequest(request, fixture.messageContext());

        Assertions.assertEquals(FILE_CONTENT, Files.readString(tempDir.resolve("delivered")));
        List<MessageResponse> responses = fixture.dispatchedResponses();
        Assertions.assertEquals(2, responses.size());
        GetFileProgressResponse progress = (GetFileProgressResponse) responses.get(0);
        Assertions.assertEquals(ResponseCode.OPERATION_ACCEPTED_PROGRESS, progress.getResponseInfo().getResponseCode());
        Assertions.assertEquals(BigInteger.valueOf(FILE_CONTENT.length()), progress.getFileSize());
        Assertions.assertEquals(PILLAR_ID, progress.getPillarID());
        GetFileFinalResponse finalResponse = (GetFileFinalResponse) responses.get(1);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(FILE_ID, finalResponse.getFileID());
        Assertions.assertEquals(request.getFileAddress(), finalResponse.getFileAddress());
        verify(fixture.auditManager()).addAuditEvent(eq(collectionID), eq(FILE_ID), eq(CLIENT_ID), any(), any(),
                eq(FileAction.GET_FILE), eq(CORRELATION_ID), any());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void uploadsOnlyTheRequestedFilePart() throws Exception {
        GetFileRequest request = createRequest(fixture.deliveryAddress("part"));
        FilePart filePart = createFilePart(2, 3);
        request.setFilePart(filePart);

        handler.processRequest(request, fixture.messageContext());

        Assertions.assertEquals("cde", Files.readString(tempDir.resolve("part")));
        GetFileFinalResponse finalResponse = (GetFileFinalResponse) fixture.dispatchedResponses().get(1);
        Assertions.assertEquals(filePart, finalResponse.getFilePart());
    }

    @ParameterizedTest
    @CsvSource({"5, 3, fgh", "8, 0, ''", "0, 8, abcdefgh"})
    @Tag(TestGroups.REGRESSIONTEST)
    void uploadsFilePartAtTheBoundsOfTheFile(long offset, long length, String expected) throws Exception {
        GetFileRequest request = createRequest(fixture.deliveryAddress("part"));
        request.setFilePart(createFilePart(offset, length));

        handler.processRequest(request, fixture.messageContext());

        Assertions.assertEquals(expected, Files.readString(tempDir.resolve("part")));
    }

    @ParameterizedTest
    @CsvSource({"6, 3", "9, 0", "-1, 2", "0, -1", "9223372036854775807, 1"})
    @Tag(TestGroups.REGRESSIONTEST)
    void filePartOutsideTheFileIsRejectedBeforeAnyResponse(long offset, long length) {
        GetFileRequest request = createRequest(fixture.deliveryAddress("part"));
        request.setFilePart(createFilePart(offset, length));

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(request, fixture.messageContext()));

        assertResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, e);
        Assertions.assertTrue(e.getResponseInfo().getResponseText().contains(FILE_ID));
        Assertions.assertFalse(Files.exists(tempDir.resolve("part")));
        verifyNoInteractions(fixture.responseDispatcher());
    }

    @ParameterizedTest
    @ValueSource(strings = {"file:///has space", "relative/path", "ftp://host/file"})
    @Tag(TestGroups.REGRESSIONTEST)
    void invalidFileAddressIsRejectedBeforeAnyResponse(String fileAddress) {
        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(createRequest(fileAddress), fixture.messageContext()));

        assertResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, e);
        verifyNoInteractions(fixture.responseDispatcher());
    }

    @ParameterizedTest
    @CsvSource({"6, 5", "10, 1"})
    @Tag(TestGroups.REGRESSIONTEST)
    void filePartOfFileThatEndsEarlyFailsInsteadOfBeingShort(long offset, long length) throws Exception {
        Path storedFile = Files.writeString(tempDir.resolve("stored"), FILE_CONTENT);
        DefaultFileInfo fileInfo = new DefaultFileInfo(storedFile.toFile());

        // Whether skipping past the end fails at once or on read depends on the stream, so allow both.
        Assertions.assertThrows(EOFException.class, () -> {
            try (InputStream part = GetFileRequestHandler.extractFilePart(fileInfo, createFilePart(offset, length))) {
                part.readAllBytes();
            }
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void filePartOfFileShorterThanItsSizeGivesFileTransferFailure() throws Exception {
        byte[] content = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        when(model.getFileInfoForActualFile(FILE_ID, collectionID)).thenAnswer(invocation ->
                new FileInfoStub(FILE_ID, 0L, content.length + 2L, new ByteArrayInputStream(content)));
        GetFileRequest request = createRequest(fixture.deliveryAddress("part"));
        request.setFilePart(createFilePart(6, 4));

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(request, fixture.messageContext()));

        assertResponseCode(ResponseCode.FILE_TRANSFER_FAILURE, e);
        Assertions.assertTrue(e.getResponseInfo().getResponseText().contains("ended before the end"));
        List<MessageResponse> responses = fixture.dispatchedResponses();
        Assertions.assertEquals(1, responses.size(), "Only the progress response should have been sent");
        Assertions.assertInstanceOf(GetFileProgressResponse.class, responses.get(0));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void filePartIsReadInFullFromStreamReturningSmallChunks() throws Exception {
        byte[] content = FILE_CONTENT.getBytes(StandardCharsets.UTF_8);
        FileInfoStub fileInfo = new FileInfoStub(FILE_ID, 0L, (long) content.length,
                new OneByteAtATimeInputStream(content));

        try (InputStream part = GetFileRequestHandler.extractFilePart(fileInfo, createFilePart(2, 3))) {
            Assertions.assertEquals("cde", new String(part.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void filePartBeyondIntegerRangeIsExtracted() throws Exception {
        long offset = Integer.MAX_VALUE + 2L;
        FileInfoStub fileInfo = new FileInfoStub(FILE_ID, 0L, offset + 3,
                new PositionInputStream(offset + 3));

        try (InputStream part = GetFileRequestHandler.extractFilePart(fileInfo, createFilePart(offset, 3))) {
            Assertions.assertArrayEquals(new byte[]{1, 2, 3}, part.readAllBytes());
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void unknownFileIsRejectedBeforeAnyResponse() throws Exception {
        doThrow(new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE, "No such file"))
                .when(model).verifyFileExists(FILE_ID, collectionID);

        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(createRequest(fixture.deliveryAddress("unused")), fixture.messageContext()));

        assertResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE, e);
        verifyNoInteractions(fixture.responseDispatcher(), fixture.auditManager());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void failedUploadGivesFileTransferFailure() {
        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(createRequest(fixture.undeliverableAddress()), fixture.messageContext()));

        assertResponseCode(ResponseCode.FILE_TRANSFER_FAILURE, e);
        List<MessageResponse> responses = fixture.dispatchedResponses();
        Assertions.assertEquals(1, responses.size(), "Only the progress response should have been sent");
        Assertions.assertInstanceOf(GetFileProgressResponse.class, responses.get(0));
        verify(fixture.auditManager(), never()).addAuditEvent(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void requestForAnotherPillarIsRejected() {
        GetFileRequest request = createRequest(fixture.deliveryAddress("unused"));
        request.setPillarID("other-pillar");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> handler.processRequest(request, fixture.messageContext()));
        verifyNoInteractions(fixture.responseDispatcher());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void failedResponseDescribesTheRequest() {
        GetFileRequest request = createRequest(fixture.deliveryAddress("unused"));

        GetFileFinalResponse response = (GetFileFinalResponse) handler.generateFailedResponse(request);

        Assertions.assertEquals(FILE_ID, response.getFileID());
        Assertions.assertEquals(request.getFileAddress(), response.getFileAddress());
        Assertions.assertEquals(PILLAR_ID, response.getPillarID());
    }

    private GetFileRequest createRequest(String fileAddress) {
        GetFileRequest request = new GetFileRequest();
        request.setCollectionID(collectionID);
        request.setPillarID(PILLAR_ID);
        request.setFileID(FILE_ID);
        request.setFileAddress(fileAddress);
        request.setCorrelationID(CORRELATION_ID);
        request.setFrom(CLIENT_ID);
        return request;
    }

    private static FilePart createFilePart(long offset, long length) {
        FilePart filePart = new FilePart();
        filePart.setPartOffSet(BigInteger.valueOf(offset));
        filePart.setPartLength(BigInteger.valueOf(length));
        return filePart;
    }

    /**
     * Reads and skips at most one byte per call, as network and buffered streams are allowed to.
     */
    private static final class OneByteAtATimeInputStream extends FilterInputStream {
        OneByteAtATimeInputStream(byte[] content) {
            super(new ByteArrayInputStream(content));
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, Math.min(len, 1));
        }

        @Override
        public long skip(long n) throws IOException {
            return super.skip(Math.min(n, 1));
        }
    }

    /**
     * A stream of the given size where each byte is its position modulo 256. Skipping is cheap, so large
     * offsets can be tested without reading through them.
     */
    private static final class PositionInputStream extends InputStream {
        private final long size;
        private long position;

        PositionInputStream(long size) {
            this.size = size;
        }

        @Override
        public int read() {
            return position < size ? (int) (position++ & 0xFF) : -1;
        }

        @Override
        public long skip(long n) {
            long skipped = Math.max(0, Math.min(n, size - position));
            position += skipped;
            return skipped;
        }
    }
}
