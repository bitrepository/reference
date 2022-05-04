package org.bitrepository.pillar.messagehandling;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.FileInfoStub;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.messagehandler.GetFileIDsRequestHandler;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.testng.Assert.assertThrows;

public class GetFileIDsRequestHandlerTest extends MockedPillarTest {
    MockedGetFileIDsRequestHandler requestHandler;
    GetFileIDsRequest getFileIDsRequest;
    FileIDs fileIDs;
    String fileID;
    String extraFileID;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        addFixture("Setup of test class.");
        addStep("Initializing getFileIDsRequest, fileIDs, etc.", "Should never fail.");
        this.requestHandler = new MockedGetFileIDsRequestHandler(context, model);
        this.getFileIDsRequest = new GetFileIDsRequest();
        this.fileIDs = new FileIDs();
        this.fileID = DEFAULT_FILE_ID + testMethodName;
        this.extraFileID = DEFAULT_FILE_ID + testMethodName + "Extra";
        fileIDs.setFileID(fileID);
        getFileIDsRequest.setFileIDs(fileIDs);
        getFileIDsRequest.setCollectionID(collectionID);
        getFileIDsRequest.setPillarID(settingsForCUT.getComponentID());
        getFileIDsRequest.setCorrelationID("CorrelationID_" + testMethodName);

        addStep("Setting up the mock of pillar.", "Should never fail.");
        doAnswer(invocationOnMock -> {
            ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
            FileInfo info = new FileInfoStub(fileID, 0L, 0L, new ByteArrayInputStream(new byte[0]));
            FileInfo infoExtra = new FileInfoStub(extraFileID, 0L, 0L, new ByteArrayInputStream(new byte[0]));
            res.insertFileInfo(info);
            res.insertFileInfo(infoExtra);
            res.reportMoreEntriesFound();
            return res;
        }).when(model).getFileIDsResultSet(eq(fileID), nullable(XMLGregorianCalendar.class), nullable(XMLGregorianCalendar.class),
                nullable(Long.class), eq(collectionID));
    }

    // Test #validateRequest
    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerValidateRequestExistingFileID() throws RequestHandlerException {
        addDescription("Test GetFileIDsRequestHandler#validateRequest with mocked file ID.");
        addStep("Setup of mock.", "Should be okay.");

        doAnswer(invocation -> true).when(model).hasFileID(eq(fileID), eq(collectionID));

        addStep("Perform the call to validateRequest", "Should not fail as the file ID is mocked to exist");
        requestHandler.validateRequest(getFileIDsRequest, null);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerValidateRequestNullFileIDs() throws RequestHandlerException {
        addDescription("Test GetFileIDsRequestHandler#validateRequest with non-mocked file ID.");
        addStep("Setup of variables and mock.", "Should not fail.");

        GetFileIDsRequest getFileIDsRequestNewFileIDs = getFileIDsRequest;
        getFileIDsRequestNewFileIDs.getFileIDs().setFileID(null);

        addStep("Perform the call to validateRequest", "Should not fail.");
        requestHandler.validateRequest(getFileIDsRequestNewFileIDs, null);

        addStep("Perform the call to validateRequest", "Should not fail.");
        getFileIDsRequestNewFileIDs.setFileIDs(null);
        requestHandler.validateRequest(getFileIDsRequestNewFileIDs, null);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerValidateRequestFileIDsIsNull() {
        addDescription("Test GetFileIDsRequestHandler#validateRequest using FileIDs with null values.");
        addStep("Perform the call to validateRequest", "Should fail as the file IDs is null.");
        assertThrows(RequestHandlerException.class, () -> requestHandler.validateRequest(getFileIDsRequest, null));
    }

    // Test #sendProgressResponse
    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerSendProgressResponse() {
        addDescription("Test GetFileIDsRequestHandler#sendProgressResponse.");
        addStep("Perform the call to sendProgressResponse", "Should not fail.");
        requestHandler.sendProgressResponse(getFileIDsRequest, null);
    }

    // Test #performOperation
    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerPerformOperation() throws RequestHandlerException {
        addDescription("Test GetFileIDsRequestHandler#performOperation.");
        addStep("Perform the call to performOperation.", "Should not fail.");

        requestHandler.performOperation(getFileIDsRequest, null);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerPerformOperationWrongResultAddressThrowsException() {
        addDescription("Test GetFileIDsRequestHandler#performOperation with non-null result address.");
        addStep("Perform the call to performOperation using an invalid URL.", "Should fail.");

        GetFileIDsRequest newGetFileIDsRequest = getFileIDsRequest;
        newGetFileIDsRequest.setResultAddress("this-is-not-a-valid-address");
        assertThrows(InvalidMessageException.class, () -> requestHandler.performOperation(newGetFileIDsRequest, null));
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerPerformOperationCorrectResultAddressNoException() throws RequestHandlerException, IOException {
        addDescription("Test GetFileIDsRequestHandler#performOperation with non-null result address.");
        addStep("Mock the putFile call to the mocked FileExchange.", "Should never fail.");
        doNothing().when(fileExchangeMock).putFile(any(InputStream.class), any(URL.class));

        addStep("Perform the call to performOperation using a valid https url.", "Should not fail.");
        GetFileIDsRequest newGetFileIDsRequest = getFileIDsRequest;
        newGetFileIDsRequest.setResultAddress("https://www.valid-address-syntax.dk/");
        requestHandler.performOperation(newGetFileIDsRequest, null);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerPerformOperationMaxNumberOfResultsNotNull() {
        addDescription("Test GetFileIDsRequestHandler#performOperation with non-null result address.");
        addStep("Perform the call to performOperation w. maxNumberOfResults set to 10", "Should not fail.");

        GetFileIDsRequest newGetFileIDsRequest = getFileIDsRequest;
        newGetFileIDsRequest.setMaxNumberOfResults(BigInteger.TEN);

        try {
            requestHandler.performOperation(newGetFileIDsRequest, null);
        } catch (RequestHandlerException e) {
            System.out.println("Threw RequestHandlerException, which should not happen.");
        }
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerPerformOperationExtractedFileIDsWithOnlyOneEntry() {
        addDescription("Test GetFileIDsRequestHandler#performOperation with non-null result address.");
        addStep("Perform the call to performOperation w. maxNumberOfResults set to 10", "Should not fail.");

        doAnswer(invocationOnMock -> {
            ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
            FileInfo info = new FileInfoStub(fileID, 0L, 0L, new ByteArrayInputStream(new byte[0]));
            res.insertFileInfo(info);
            return res;
        }).when(model).getFileIDsResultSet(eq(fileID), nullable(XMLGregorianCalendar.class), nullable(XMLGregorianCalendar.class),
                nullable(Long.class), eq(collectionID));

        try {
            requestHandler.performOperation(getFileIDsRequest, null);
        } catch (RequestHandlerException e) {
            System.out.println("Threw RequestHandlerException, which should not happen.");
        }
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testGetFileIDsRequestHandlerPerformOperationCantValidateXMLThrowsException() {
        addDescription("Test GetFileIDsRequestHandler#validateXML with invalid data.");
        addStep("Perform the call to validateXML using malformed data string.", "Should fail.");


        assertThrows(JAXBException.class,
                () -> requestHandler.validateXML(
                        new JaxbHelper("xsd/", "BitRepositoryData.xsd"),
                        "ajsdkajdasdjsakasdadjaskdaskdasdsadklæadæladladæadakd<<wrong><>"));
    }

    private static class MockedGetFileIDsRequestHandler extends GetFileIDsRequestHandler {

        /**
         * @param context The context for the message handling.
         * @param model   The storage model for the pillar.
         */
        protected MockedGetFileIDsRequestHandler(MessageHandlerContext context, StorageModel model) {
            super(context, model);
        }

        @Override
        public Class<GetFileIDsRequest> getRequestClass() {
            return super.getRequestClass();
        }

        @Override
        public MessageResponse generateFailedResponse(GetFileIDsRequest request) {
            return super.generateFailedResponse(request);
        }

        @Override
        protected void validateRequest(GetFileIDsRequest request, MessageContext requestContext) throws RequestHandlerException {
            super.validateRequest(request, requestContext);
        }

        @Override
        protected void sendProgressResponse(GetFileIDsRequest request, MessageContext requestContext) {
            super.sendProgressResponse(request, requestContext);
        }

        @Override
        protected void performOperation(GetFileIDsRequest request, MessageContext requestContext) throws RequestHandlerException {
            super.performOperation(request, requestContext);
        }

        @Override
        protected void validateXML(JaxbHelper jaxbHelper, String data) throws IOException, JAXBException {
            super.validateXML(jaxbHelper, data);
        }
    }
}
