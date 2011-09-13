package org.bitrepository.access.getfile;

import java.io.File;

import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.collection.settings.standardsettings.GetFileTYPE;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractGetFileClientTest extends DefaultFixtureClientTest   {
    protected TestGetFileMessageFactory testMessageFactory;
    protected TestFileStore pillar1FileStore;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        if (useMockupPillar()) {
            testMessageFactory = new TestGetFileMessageFactory(
                    settings.getBitRepositoryCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1", new File("src/test/resources/test-files/", DEFAULT_FILE_ID));
            // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
            // with content.
        }
        httpServer.clearFiles();
    }

    @Override
    public void initCollectionSettings() {
        super.initCollectionSettings();
        GetFileTYPE getfileSettings = new GetFileTYPE();
        getfileSettings.setIdentificationTimeout(defaultTime);
        getfileSettings.setOperationTimeout(defaultTime);
        getfileSettings.getPillarIDs().add(PILLAR1_ID);
        settings.setGetFile(getfileSettings);
        
        System.out.println(getfileSettings);
        System.out.println(settings);
    };
}
