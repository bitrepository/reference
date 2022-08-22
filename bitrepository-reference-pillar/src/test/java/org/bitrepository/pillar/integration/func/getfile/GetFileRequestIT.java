package org.bitrepository.pillar.integration.func.getfile;

import org.apache.commons.io.IOUtils;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class GetFileRequestIT extends PillarFunctionTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    protected GetFileMessageFactory msgFactory;
    protected URL testFileURL = null;
    protected FileExchange fe = null;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        String pillarDestination = lookupGetFileDestination();
        msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
        testFileURL = new URL(DEFAULT_FILE_URL.toExternalForm() + System.currentTimeMillis());
        fe = ProtocolComponentFactory.getInstance().getFileExchange(settingsForCUT);
    }

    @AfterMethod(alwaysRun=true)
    public void cleanUp(Method method) {
        try {
            fe.deleteFile(testFileURL);
        } catch (Exception e) {
            log.warn("Could not clean up file '{}' after method '{}'", testFileURL, method.getName());
        }
    }

    @Test(groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void normalGetFileTest() throws IOException {
        addDescription("Tests a normal GetFile sequence");
        addStep("Send a getFile request to " + testConfiguration.getPillarUnderTestID(),
                "The pillar should send a final response with the following elements: <ol>"  +
                        "<li>'CollectionID' element corresponding to the supplied value</li>" +
                        "<li>'CorrelationID' element corresponding to the supplied value</li>" +
                        "<li>'From' element corresponding to the pillars component ID</li>" +
                        "<li>'To' element should be set to the value of the 'From' elements in the request</li>" +
                        "<li>'Destination' element should be set to the value of 'ReplyTo' from the request</li>" +
                        "<li>'FilePart' element should be null</li>" +
                        "<li>'PillarID' element corresponding to the pillars component ID</li>" +
                        "<li>'FileID' element corresponding to the supplied fileID</li>" +
                        "<li>'FileAddress' element corresponding to the supplied FileAddress</li>"  +
                        "<li>'ResponseInfo.ResponseCode' element should be OPERATION_COMPLETED</li>" +
                        "</ol>");

        GetFileRequest getRequest = (GetFileRequest) createRequest();
        messageBus.sendMessage(getRequest);

        GetFileFinalResponse finalResponse = (GetFileFinalResponse) receiveResponse();
        assertNotNull(finalResponse);
        assertEquals(finalResponse.getCorrelationID(), getRequest.getCorrelationID(),
                "Received unexpected 'CorrelationID' element.");
        assertEquals(finalResponse.getCollectionID(), getRequest.getCollectionID(),
                "Received unexpected 'CollectionID' element.");
        assertEquals(finalResponse.getFrom(), getPillarID(),
                "Received unexpected 'From' element.");
        assertEquals(finalResponse.getTo(), getRequest.getFrom(),
                "Received unexpected 'To' element.");
        assertEquals(finalResponse.getDestination(), getRequest.getReplyTo(),
                "Received unexpected 'Destination' element.");
        assertNull(finalResponse.getFilePart(),
                "Received unexpected 'FilePart' element.");
        assertEquals(finalResponse.getFileID(), getRequest.getFileID(),
                "Received unexpected 'To' element.");
        assertEquals(finalResponse.getFileAddress(), getRequest.getFileAddress(),
                "Received unexpected 'FileAddress' element.");
        assertEquals(finalResponse.getPillarID(), getPillarID(),
                "Received unexpected 'PillarID' element.");
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED,
                "Received unexpected 'ResponseCode' element.");

        try (InputStream localFileIS = TestFileHelper.getDefaultFile();
             InputStream getFileIS = fe.getFile(testFileURL)) {
            String localFileContent = IOUtils.toString(localFileIS, StandardCharsets.UTF_8);
            String getFileContent = IOUtils.toString(getFileIS, StandardCharsets.UTF_8);
            assertEquals(getFileContent, localFileContent,
                    "Differing content between original file and file from GetFileRequest");
        }
    }

    @Test(groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void getFileWithFilePartTest() throws IOException {
        addDescription("Tests that a pillar is able to return a specified FilePart in the final response");
        addStep("Send a getFile request to " + testConfiguration.getPillarUnderTestID() + " with a specified " +
                "FilePart", "The pillar should send a final response with the FilePart element for the " +
                "supplied file");
        GetFileRequest getRequest = (GetFileRequest) createRequest();

        final int offsetAndLength = 5;
        FilePart filePart = new FilePart();
        filePart.setPartOffSet(BigInteger.valueOf(offsetAndLength));
        filePart.setPartLength(BigInteger.valueOf(offsetAndLength));
        getRequest.setFilePart(filePart);
        messageBus.sendMessage(getRequest);

        GetFileFinalResponse finalResponse = (GetFileFinalResponse) receiveResponse();
        assertEquals(finalResponse.getFilePart(), getRequest.getFilePart(),
                "Received unexpected 'FilePart' element.");

        try (InputStream localFileIS = TestFileHelper.getDefaultFile();
             InputStream getFileIS = fe.getFile(testFileURL)) {
            byte[] localFilePartContent = new byte[offsetAndLength];
            localFileIS.skip(offsetAndLength);
            localFileIS.read(localFilePartContent, 0, offsetAndLength);
            String getFileContent = IOUtils.toString(getFileIS, StandardCharsets.UTF_8);
            assertEquals(getFileContent, new String(localFilePartContent, StandardCharsets.UTF_8),
                    "Differing content between original file and file from GetFileRequest");
        }
    }

    @Test(groups = {PillarTestGroups.FULL_PILLAR_TEST})
    public void getMissingFileTest() {
        addDescription("Tests that a pillar gives an error when trying to get a non-existing file");
        addStep("Send a getFile request to " + testConfiguration.getPillarUnderTestID() + " with a " +
                "non-existing fileID", "The pillar should send a failure response");

        GetFileRequest getRequest = (GetFileRequest) createRequest();
        getRequest.setFileID("NonExistingFile");
        messageBus.sendMessage(getRequest);

        GetFileFinalResponse finalResponse = (GetFileFinalResponse) receiveResponse();
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE,
                "Received unexpected 'ResponseCode' element.");
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST} )
    public void missingCollectionIDTest() {
        addDescription("Verifies the a missing collectionID in the request is rejected");
        addStep("Sending a request without a collectionID.",
                "The pillar should send a REQUEST_NOT_UNDERSTOOD_FAILURE Response.");
        MessageRequest request = createRequest();
        request.setCollectionID(null);
        messageBus.sendMessage(request);

        MessageResponse receivedResponse = receiveResponse();
        Assert.assertEquals(receivedResponse.getResponseInfo().getResponseCode(),
                ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
    }

    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST} )
    public void otherCollectionTest() {
        addDescription("Verifies identification works correctly for a second collection defined for pillar");
        addStep("Sending a identify request with a non-default collectionID (not the first collection) " +
                        "the pillar is part of",
                "The pillar under test should make a positive response");
        MessageRequest request = createRequest();
        request.setCollectionID(nonDefaultCollectionId);
        messageBus.sendMessage(request);
        assertPositivResponseIsReceived();
    }

    protected MessageRequest createRequest() {
        return msgFactory.createGetFileRequest(testFileURL.toExternalForm(), DEFAULT_FILE_ID);
    }

    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(GetFileFinalResponse.class, getOperationTimeout(),
                TimeUnit.SECONDS);
    }

    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(GetFileFinalResponse.class);
    }

    public String lookupGetFileDestination() {
        GetFileMessageFactory getFileMessageFactory =
                new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        IdentifyPillarsForGetFileRequest identifyRequest = getFileMessageFactory.createIdentifyPillarsForGetFileRequest(
                TestFileHelper.DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyPillarsForGetFileResponse.class).getReplyTo();
    }

    protected void assertPositivResponseIsReceived() {
        MessageResponse receivedResponse = receiveResponse();
        Assert.assertEquals(receivedResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_COMPLETED);
    }
}
