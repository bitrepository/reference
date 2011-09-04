package org.bitrepository.access.getfile;

import java.io.File;

import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractGetFileClientTest extends DefaultFixtureClientTest   {
    protected TestGetFileMessageFactory testMessageFactory;
    protected TestFileStore pillar1FileStore;
    private MutableGetFileClientSettings getFileClientSettings;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        if (useMockupPillar()) {
            testMessageFactory = new TestGetFileMessageFactory(
                    getFileClientSettings().getStandardSettings().getBitRepositoryCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1", new File("src/test/resources/test-files/", DEFAULT_FILE_ID));
            // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
            // with content.
        }
        httpServer.clearFiles();
    }

    @Override
    protected void setupSettings(String testName) {
        getFileClientSettings = new MutableGetFileClientSettings();
        getFileClientSettings.setGetFileDefaultTimeout(1000);
        super.setupSettings(testName);
    };
    
    @Override
    protected MutableClientSettings getClientSettings() {
        return getFileClientSettings();
    }
    
    protected MutableGetFileClientSettings getFileClientSettings() {
        return getFileClientSettings;
    }
}
