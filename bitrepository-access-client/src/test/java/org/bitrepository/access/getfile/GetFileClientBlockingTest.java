package org.bitrepository.access.getfile;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetFileClientBlockingTest extends AbstractGetFileClientTest  {

    @Test(groups = {"specification-only"})
    public void blocking() {
        addDescription("Tests that the blocking usage of the GetClient works correctly in the trivial case");
        addStep("Request a file from a specific using the blocking get method", 
                "The method should return after the complete message has been received");
    }
    
    @Test(groups = {"specification-only"})
    public void blockingWithError() throws Exception {
        addDescription("Tests that the blocking usage of the GetClient works correctly in case of an error occuring");
        addStep("Request a file from a specific non-existing pillar using the blocking get method", 
                "The method should block until a identify timeout occures, causing a TimeoutException to be thrown");
        
        System.out.println(settings);
        settings.getGetFile().setIdentificationTimeout(defaultTime);
        GetFileClient getFileClient = 
            new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(settings), 
                    testEventManager);
        try {
            getFileClient.getFileFromSpecificPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), "Invalid pillar");
            Assert.fail("Expected NoPillarFoundException after timeout, but nothing happened");
        } catch (NoPillarFoundException e) {
            //As expected
        } 
    }
}
