package org.bitrepository.pillar.checksumpillar.deletefile;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.checksumpillar.ChecksumPillar;
import org.bitrepository.pillar.checksumpillar.MemoryCache;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteFileOnChecksumPillarTest extends DefaultFixturePillarTest {
    DeleteFileMessageFactory msgFactory;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetChecksumsTests() throws Exception {
        msgFactory = new DeleteFileMessageFactory(settings);
    }

    @Test( groups = {"pillartest"})
    public void pillarDeleteFileTestSuccessCase() throws Exception {
        addDescription("Testing the delete operation for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String AUDIT = "DELETE-FILE-TEST";
        String CHECKSUM = "delete-checksum";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        MemoryCache csCache = new MemoryCache();
        csCache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Instantiate the checksum pillar.", "Should connect to the messagebus.");
        ChecksumPillar csPillar = new ChecksumPillar(messageBus, settings, csCache);
        
        addStep("Send message for identification of the pillar.", 
                "The checksum pillar receive and handle the message.");
        IdentifyPillarsForDeleteFileRequest identifyReq = msgFactory.createIdentifyPillarsForDeleteFileRequest(AUDIT, 
                clientDestinationId, FILE_ID);
        messageBus.sendMessage(identifyReq);
        
        addStep("Receive and validate response from the checksum pillar.",
                "The pillar should make a positive response.");
        IdentifyPillarsForDeleteFileResponse identifyRes = clientTopic.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(identifyRes, msgFactory.createIdentifyPillarsForDeleteFileResponse(csSpec, 
                identifyRes.getCorrelationID(), FILE_ID, identifyRes.getReplyTo(), pillarId, 
                identifyRes.getTimeToDeliver(), clientDestinationId, identifyRes.getResponseInfo()));
        
        
        
        
        csPillar.close();
    }

}
