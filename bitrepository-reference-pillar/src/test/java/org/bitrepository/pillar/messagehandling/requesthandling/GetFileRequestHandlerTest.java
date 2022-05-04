package org.bitrepository.pillar.messagehandling.requesthandling;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.FileInfoStub;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.messagehandler.GetFileRequestHandler;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertThrows;

public class GetFileRequestHandlerTest extends MockedPillarTest {
    MockedGetFileRequestHandler requestHandler;
    GetFileRequest getFileRequest;
    FileIDs fileIDs;
    String fileID;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        addFixture("Setup of test class.");
        addStep("Initializing getFileRequest, fileIDs, etc.", "Should never fail.");
        this.requestHandler = new MockedGetFileRequestHandler(context, model);
        this.getFileRequest = new GetFileRequest();
        this.fileIDs = new FileIDs();
        this.fileID = DEFAULT_FILE_ID + testMethodName;
        fileIDs.setFileID(fileID);
        getFileRequest.setFileID(fileID);
        getFileRequest.setFilePart(null);
        getFileRequest.setFileAddress(null);
        getFileRequest.setPillarID(settingsForCUT.getCollectionDestination());
        getFileRequest.setCollectionID(collectionID);
        getFileRequest.setPillarID(settingsForCUT.getComponentID());
        getFileRequest.setCorrelationID("CorrelationID_" + testMethodName);

        try {
            doAnswer(invocationOnMock -> new FileInfoStub(fileID, 0L, 1L, new ByteArrayInputStream(new byte[0]))).when(model)
                    .getFileInfoForActualFile(eq(fileID), eq(collectionID));
        } catch (RequestHandlerException e) {
            e.printStackTrace();
        }
//        addStep("Setting up the mock of pillar.", "Should never fail.");
//        doAnswer(invocationOnMock -> {
//            ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
//            FileInfo info = new FileInfoStub(fileID, 0L, 0L, new ByteArrayInputStream(new byte[0]));
//            res.insertFileInfo(info);
//            res.reportMoreEntriesFound();
//            return res;
//        }).when(model).getFileIDsResultSet(eq(fileID), nullable(XMLGregorianCalendar.class), nullable(XMLGregorianCalendar.class),
//                nullable(Long.class), eq(collectionID));
    }

    // Test #validateRequest
    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileRequestHandlerValidateRequestExistingFileID() throws RequestHandlerException {
        addDescription("Test GetFileRequestHandler#validateRequest with mocked file ID.");
        addStep("Setup of mock.", "Should be okay.");

        doAnswer(invocation -> true).when(model).hasFileID(eq(fileID), eq(collectionID));

        addStep("Perform the call to validateRequest", "Should not fail as the file ID is mocked to exist");
        requestHandler.validateRequest(getFileRequest, null);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileRequestHandlerValidateRequestFileIDIsNull() {
        addDescription("Test GetFileRequestHandler#validateRequest with null as File ID");
        addStep("Setup of mock.", "Should be okay.");

        GetFileRequest newGetFileRequest = getFileRequest;
        newGetFileRequest.setFileID(null);

        addStep("Perform the call to validateRequest", "Should not fail as the file ID is mocked to exist");
        assertThrows(NullPointerException.class, () -> requestHandler.validateRequest(newGetFileRequest, null));
    }

    // Test #sendProgressResponse
    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileRequestHandlerSendProgressResponse() throws RequestHandlerException {
        addDescription("Test GetFileRequestHandler#sendProgressResponse.");
        addStep("Perform the call to sendProgressResponse", "Should not fail.");
        requestHandler.sendProgressResponse(getFileRequest, null);
    }


    private static class MockedGetFileRequestHandler extends GetFileRequestHandler {

        /**
         * @param context The context for the message handling.
         * @param model   The storage model for the pillar.
         */
        protected MockedGetFileRequestHandler(MessageHandlerContext context, StorageModel model) {
            super(context, model);
        }

        @Override
        public Class<GetFileRequest> getRequestClass() {
            return super.getRequestClass();
        }

        @Override
        public MessageResponse generateFailedResponse(GetFileRequest message) {
            return super.generateFailedResponse(message);
        }

        @Override
        protected void validateRequest(GetFileRequest request, MessageContext requestContext) throws RequestHandlerException {
            super.validateRequest(request, requestContext);
        }

        @Override
        protected void sendProgressResponse(GetFileRequest request, MessageContext requestContext) throws RequestHandlerException {
            super.sendProgressResponse(request, requestContext);
        }

        @Override
        protected void performOperation(GetFileRequest request, MessageContext requestContext) throws RequestHandlerException {
            super.performOperation(request, requestContext);
        }

        @Override
        protected void uploadToClient(GetFileRequest message) throws RequestHandlerException {
            super.uploadToClient(message);
        }

        @Override
        protected void sendFinalResponse(GetFileRequest request) {
            super.sendFinalResponse(request);
        }
    }
}
