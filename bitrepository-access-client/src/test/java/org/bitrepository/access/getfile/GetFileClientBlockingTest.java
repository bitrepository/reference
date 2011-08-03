package org.bitrepository.access.getfile;

import java.io.File;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetFileClientBlockingTest extends DefaultFixtureClientTest  {

    private TestGetFileMessageFactory testMessageFactory;
    private TestFileStore pillar1FileStore;
    private TestFileStore pillar2FileStore;
    private MutableGetFileClientSettings getFileClientSettings;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        getFileClientSettings = new MutableGetFileClientSettings(settings);
        getFileClientSettings.setGetFileDefaultTimeout(1000);

        if (useMockupPillar()) {
            testMessageFactory = new TestGetFileMessageFactory(settings.getBitRepositoryCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1", new File("src/test/resources/test-files/", DEFAULT_FILE_ID));
            pillar2FileStore = new TestFileStore("Pillar2", new File("src/test/resources/test-files/", DEFAULT_FILE_ID));
            // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
            // with content.
        }
        httpServer.clearFiles();
    }

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
        
        ((MutableClientSettings)getFileClientSettings).setIdentifyPillarsTimeout(3000);
        GetFileClient getFileClient = 
            new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                    testEventManager);
        try {
            getFileClient.getFileFromSpecificPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), "Invalid pillar");
            Assert.fail("Expected NoPillarFoundException after timeout, but nothing happened");
        } catch (NoPillarFoundException e) {
            //As expected
        } 
    }
}
