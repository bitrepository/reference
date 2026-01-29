package org.bitrepository.protocol;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.protocol.bus.LocalActiveMQBroker;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.http.EmbeddedHttpServer;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.SimpleMessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.jaccept.TestEventManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.jms.JMSException;
import java.net.MalformedURLException;
import java.net.URL;

public class GlobalSuiteExtension implements BeforeAllCallback, AfterAllCallback {

    private static boolean initialized = false;
    protected static TestEventManager testEventManager = TestEventManager.getInstance();
    public static LocalActiveMQBroker broker;
    public static EmbeddedHttpServer server;
    public static HttpServerConfiguration httpServerConfiguration;
    public static MessageBus messageBus;
    private MessageReceiverManager receiverManager;
    protected static String alarmDestinationID;
    protected static MessageReceiver alarmReceiver;
    protected static SecurityManager securityManager;
    protected static Settings settingsForCUT;
    protected static Settings settingsForTestClient;
    protected static String collectionID;
    protected String nonDefaultFileId;
    protected static String DEFAULT_FILE_ID;
    protected static URL DEFAULT_FILE_URL;
    protected static String DEFAULT_DOWNLOAD_FILE_ADDRESS;
    protected static String DEFAULT_UPLOAD_FILE_ADDRESS;
    protected String DEFAULT_AUDIT_INFORMATION;
    protected String testMethodName;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!initialized) {
            initialized = true;
            settingsForCUT = loadSettings(getComponentID());
            settingsForTestClient = loadSettings("TestSuiteInitialiser");
            makeUserSpecificSettings(settingsForCUT);
            makeUserSpecificSettings(settingsForTestClient);
            httpServerConfiguration = new HttpServerConfiguration(settingsForTestClient.getReferenceSettings().getFileExchangeSettings());
            collectionID = settingsForTestClient.getCollections().get(0).getID();

            securityManager = createSecurityManager();
            DEFAULT_FILE_ID = "DefaultFile";
            try {
                DEFAULT_FILE_URL = httpServerConfiguration.getURL(TestFileHelper.DEFAULT_FILE_ID);
                DEFAULT_DOWNLOAD_FILE_ADDRESS = DEFAULT_FILE_URL.toExternalForm();
                DEFAULT_UPLOAD_FILE_ADDRESS = DEFAULT_FILE_URL.toExternalForm() + "-" + DEFAULT_FILE_ID;
            } catch (MalformedURLException e) {
                throw new RuntimeException("Never happens", e);
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (initialized) {
            teardownMessageBus();
            teardownHttpServer();
        }
    }

    protected Settings loadSettings(String componentID) {
        return TestSettingsProvider.reloadSettings(componentID);
    }

    protected void makeUserSpecificSettings(Settings settings) {
        settings.getRepositorySettings().getProtocolSettings()
                .setCollectionDestination(settings.getCollectionDestination() + getTopicPostfix());
        settings.getRepositorySettings().getProtocolSettings().setAlarmDestination(settings.getAlarmDestination() + getTopicPostfix());
    }

    protected String getTopicPostfix() {
        return "-" + System.getProperty("user.name");
    }

    protected String getComponentID() {
        return getClass().getSimpleName();
    }

    protected SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }

    /**
     * Hooks up the message bus.
     */
    protected void setupMessageBus() {
        if (useEmbeddedMessageBus()) {
            if (messageBus == null) {
                messageBus = new SimpleMessageBus();
            }
        }
    }

    protected void teardownMessageBus() {
        MessageBusManager.clear();
        if (messageBus != null) {
            try {
                messageBus.close();
                messageBus = null;
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        if (broker != null) {
            try {
                broker.stop();
                broker = null;
            } catch (Exception e) {
                // No reason to pollute the test output with this
            }
        }
    }

    /**
     * Shutdown the embedded http server if any.
     */
    protected void teardownHttpServer() {
        if (useEmbeddedHttpServer()) {
            server.stop();
        }
    }

    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "true").equals("true");
    }

    public boolean useEmbeddedHttpServer() {
        return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
    }
}