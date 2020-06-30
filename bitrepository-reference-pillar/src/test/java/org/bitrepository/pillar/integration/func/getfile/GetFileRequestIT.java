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
import org.bitrepository.pillar.integration.func.DefaultPillarOperationTest;
import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class GetFileRequestIT extends DefaultPillarOperationTest {
    protected GetFileMessageFactory msgFactory;
    protected String testFileURL = null;
    protected FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(settingsForCUT);

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest(Method method) throws Exception {
        String pillarDestination = lookupGetFileDestination();
        msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), pillarDestination);
        testFileURL = DEFAULT_FILE_URL.toExternalForm() + System.currentTimeMillis();
    }

    @AfterMethod
    public void cleanUp() {
        try {
            fe.deleteFile(new URL(testFileURL));
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError("Could not delete file at '" + testFileURL + "'");
        }
    }

    @Test(groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
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

        GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
        assertNotNull(finalResponse);
        assertEquals(finalResponse.getCorrelationID(), getRequest.getCollectionID(),
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

        File file = new File(DEFAULT_FILE_URL.toExternalForm());
        FileInputStream fis = new FileInputStream(file);
        String putFileContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
        InputStream is = fe.getFile(new URL(testFileURL));
        String fileContent = IOUtils.toString(is, StandardCharsets.UTF_8);

        assertEquals(fileContent, putFileContent);
    }

    /*@Test(groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void getFileWithFilePartTest() {
        addDescription("Tests that a pillar is able to return a specified FilePart in the final response");
        addStep("Send a getFile request to " + testConfiguration.getPillarUnderTestID() + " with a specified " +
                "FilePart", "The pillar should send a final response with the FilePart element for the " +
                "supplied file");
        GetFileRequest getRequest = (GetFileRequest) createRequest();

        FilePart filePart = new FilePart();
        filePart.setPartOffSet(BigInteger.valueOf(9));
        filePart.setPartLength(BigInteger.valueOf(7));
        getRequest.setFilePart(filePart);
        messageBus.sendMessage(getRequest);

        GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
        assertEquals(finalResponse.getFilePart(), getRequest.getFilePart(),
                "Received unexpected 'FilePart' element.");
    }

    @Test(groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void getMissingFileTest() {
        addDescription("Tests that a pillar gives an error when trying to get a non-existing file");
        addStep("Send a getFile request to " + testConfiguration.getPillarUnderTestID() + " with a " +
                "non-existing fileID", "The pillar should send a failure response");

        GetFileRequest getRequest = (GetFileRequest) createRequest();
        getRequest.setFileID("NonExistingFile");
        messageBus.sendMessage(getRequest);

        GetFileFinalResponse finalResponse = clientReceiver.waitForMessage(GetFileFinalResponse.class);
        assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FAILURE,
                "Received unexpected 'ResponseCode' element.");
    }*/

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createGetFileRequest(testFileURL, DEFAULT_FILE_ID);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(GetFileFinalResponse.class);
    }

    @Override
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
}
