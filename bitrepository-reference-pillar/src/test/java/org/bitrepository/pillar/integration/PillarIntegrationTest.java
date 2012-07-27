package org.bitrepository.pillar.integration;

import java.io.IOException;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.referencepillar.ReferencePillarLauncher;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.jaccept.TestEventManager;
import org.testng.annotations.BeforeSuite;

/**
 * Super class for all tests which should test functionality on a single pillar.
 *
 * Note That no setup/teardown is possible in this test of external pillars, so tests need to be written
 * to be invariant against the initial pillar state.
 */
public abstract class PillarIntegrationTest extends DefaultFixturePillarTest {
    /**
     * The path to the default settings used for integration test of pillars. The value is
     * <code>settings/xml/integretion-test</code>
     */
    public static final String PATH_TO_DEFAULT_SETTINGS = "settings/xml/integration-test/";
    /**
     * Environment variable used to override the default path to the pillar settings whci should be used for the
     * integration test. Value = <code>pillar.integrationtest.settings.path</code>
     */
    public static final String PILLAR_INTEGRATIONTEST_SETTINGS_PATH = "pillar.integrationtest.settings.path";

    public static final String PROPERTY_FILE_NAME = "pillar-integration-test.properties";

    private PillarIntegrationTestSettings testSettings;

    protected TestFileHelper fileHelper;

    /** Indicated whether reference pillars should be started should be started and used. Note that mockup pillars
     * should be used in this case, e.g. the useMockupPillar() call should return false. */
    public boolean useEmbeddedPillar() {
        return System.getProperty("useEmbeddedPillar", "false").equals("true");
    }
    @BeforeSuite  (alwaysRun = true)
    protected void prepareIntegrationTest() throws IOException {
        loadTestSettings();
        startEmbeddedReferencePillar();
        configureFileHelper();
    }

    protected void startEmbeddedReferencePillar() {
        if (testSettings.useEmbeddedPillar()) {
            ReferencePillarLauncher.main( new String[] {getPathToSettings(), null, "embeddedReferencePillar"});
        }
    }

    protected void configureFileHelper() {
        HttpServerConfiguration config = testSettings.getHttpServerConfig();
        fileHelper = new TestFileHelper(
                componentSettings,
                new HttpServerConnector(config, TestEventManager.getInstance()));
    }

    @Override
    protected String getComponentID() {
        return "PillarUnderIntegrationTest";
    }

    private String getPathToSettings() {
        return System.getProperty(PILLAR_INTEGRATIONTEST_SETTINGS_PATH, PATH_TO_DEFAULT_SETTINGS);
    }

    private void loadTestSettings() throws IOException {
        testSettings = new PillarIntegrationTestSettings(PATH_TO_DEFAULT_SETTINGS + PROPERTY_FILE_NAME);
    }
}
