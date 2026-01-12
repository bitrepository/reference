package org.bitrepository.protocol;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
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
import org.bitrepository.protocol.utils.AllureTestUtils;
import org.bitrepository.protocol.utils.TestWatcherExtension;
import org.bitrepository.protocol.utils.AllureEventLogger; // NEW
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jms.JMSException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestWatcherExtension.class)
public abstract class IntegrationTest {
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
    protected String NON_DEFAULT_FILE_ID;
    protected static String DEFAULT_FILE_ID;
    protected static URL DEFAULT_FILE_URL;
    protected static String DEFAULT_DOWNLOAD_FILE_ADDRESS;
    protected static String DEFAULT_UPLOAD_FILE_ADDRESS;
    protected String DEFAULT_AUDIT_INFORMATION;

    protected String testMethodName;

    protected void addDescription(String description) {
        AllureTestUtils.addDescription(description);
    }

    protected void addStep(String stepDescription, String expectedResult) {
        AllureTestUtils.addStep(stepDescription, expectedResult);
    }

    protected void addFixture(String fixtureDescription) {
        AllureTestUtils.addFixture(fixtureDescription);
    }

    protected void addReference(String reference) {
        AllureTestUtils.addReference(reference);
    }

    private void initializationMethod() {
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
            throw new RuntimeException("Never happens");
        }
    }

    /**
     * May be extended by subclasses needing to have their receivers managed. Remember to still call
     * <code>super.registerReceivers()</code> when overriding
     */
    @Step("Register message receivers")
    protected void registerMessageReceivers() {
        alarmReceiver = new MessageReceiver(settingsForCUT.getAlarmDestination());
        addReceiver(alarmReceiver);
    }

    protected void addReceiver(MessageReceiver receiver) {
        receiverManager.addReceiver(receiver);
    }

    @BeforeAll
    public void initMessagebus() {
        Allure.step("Initialize message bus", () -> {
            initializationMethod();
            setupMessageBus();
        });
    }

    @AfterAll
    public void shutdownSuite() {
        Allure.step("Shutdown test suite", () -> {
            teardownMessageBus();
            teardownHttpServer();
        });
    }

    /**
     * Defines the standard BitRepositoryCollection configuration
     */
    @BeforeEach
    public final void beforeMethod(TestInfo testInfo) {
        testMethodName = testInfo.getTestMethod().get().getName();

        Allure.step("Setup test: " + testMethodName, () -> {
            setupSettings();
            NON_DEFAULT_FILE_ID = TestFileHelper.createUniquePrefix(testMethodName);
            DEFAULT_AUDIT_INFORMATION = testMethodName;
            receiverManager = new MessageReceiverManager(messageBus);
            registerMessageReceivers();
            messageBus.setCollectionFilter(List.of());
            messageBus.setComponentFilter(List.of());
            receiverManager.startListeners();
            initializeCUT();
        });
    }

    protected void initializeCUT() {}

    @AfterEach
    public final void afterMethod() {
        Allure.step("Teardown test: " + testMethodName, () -> {
            if (receiverManager != null) {
                receiverManager.stopListeners();
            }
            if (TestWatcherExtension.isTestSuccessful()) {
                afterMethodVerification();
            }
            shutdownCUT();
        });
    }

    /**
     * May be used by specific tests for general verification when the test method has finished. Will only be run
     * if the test has passed (so far).
     */
    @Step("Verify no messages remain in receivers")
    protected void afterMethodVerification() {
        receiverManager.checkNoMessagesRemainInReceivers();
    }

    /**
     * Purges all messages from the receivers.
     */
    @Step("Clear all receivers")
    protected void clearReceivers() {
        receiverManager.clearMessagesInReceivers();
    }

    /**
     * May be overridden by specific tests wishing to do stuff. Remember to call super if this is overridden.
     */
    protected void shutdownCUT() {}

    /**
     * Initializes the settings. Will postfix the alarm and collection topics with '-${user.name}
     */
    @Step("Setup settings")
    protected void setupSettings() {
        settingsForCUT = loadSettings(getComponentID());
        makeUserSpecificSettings(settingsForCUT);
        SettingsUtils.initialize(settingsForCUT);

        alarmDestinationID = settingsForCUT.getRepositorySettings().getProtocolSettings().getAlarmDestination();

        settingsForTestClient = loadSettings(testMethodName);
        makeUserSpecificSettings(settingsForTestClient);
    }

    protected Settings loadSettings(String componentID) {
        return TestSettingsProvider.reloadSettings(componentID);
    }

    private void makeUserSpecificSettings(Settings settings) {
        settings.getRepositorySettings().getProtocolSettings()
                .setCollectionDestination(settings.getCollectionDestination() + getTopicPostfix());
        settings.getRepositorySettings().getProtocolSettings().setAlarmDestination(settings.getAlarmDestination() + getTopicPostfix());
    }

    /**
     * Indicated whether an embedded active MQ should be started and used
     */
    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "true").equals("true");
    }

    /**
     * Indicated whether an embedded http server should be started and used
     */
    public boolean useEmbeddedHttpServer() {
        return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
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

    /**
     * Shutdown the message bus.
     */
    private void teardownMessageBus() {
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

    /**
     * Returns the postfix string to use when accessing user specific topics, which is the mechanism we use in the
     * bit repository tests.
     *
     * @return The string to postfix all topix names with.
     */
    protected String getTopicPostfix() {
        return "-" + System.getProperty("user.name");
    }

    protected String getComponentID() {
        return getClass().getSimpleName();
    }

    protected String createDate() {
        return Long.toString(System.currentTimeMillis());
    }

    protected SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }
}