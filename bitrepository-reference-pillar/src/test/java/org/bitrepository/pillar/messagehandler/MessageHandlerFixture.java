package org.bitrepository.pillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ResponseDispatcher;
import org.bitrepository.service.exception.RequestHandlerException;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Shared fixture for unit testing the pillar request handlers without a message bus.
 * Responses are captured from a mocked {@link ResponseDispatcher}, and results are delivered to
 * {@code file://} addresses in the given temporary directory. Create a new instance per test.
 */
final class MessageHandlerFixture {
    static final String PILLAR_ID = "MessageHandlerTestPillar";
    static final String FILE_ID = "test-file";
    static final String CORRELATION_ID = "correlation-id";
    static final String CLIENT_ID = "test-client";

    private final Path tempDir;
    private final String collectionID;
    private final StorageModel model;
    private final ResponseDispatcher responseDispatcher;
    private final AuditTrailManager auditManager;
    private final MessageHandlerContext context;
    private final MessageContext messageContext = new MessageContext(null);

    /**
     * @param tempDir The directory results are delivered to, typically a JUnit {@code @TempDir}.
     */
    MessageHandlerFixture(Path tempDir) {
        this.tempDir = tempDir;
        // Reload so changes made by one test do not leak into the next through the cached settings.
        Settings settings = TestSettingsProvider.reloadSettings(PILLAR_ID);
        settings.getReferenceSettings().getPillarSettings().setPillarID(PILLAR_ID);
        collectionID = settings.getCollections().get(0).getID();

        model = mock(StorageModel.class);
        when(model.getPillarID()).thenReturn(PILLAR_ID);
        responseDispatcher = mock(ResponseDispatcher.class);
        auditManager = mock(AuditTrailManager.class);
        context = new MessageHandlerContext(settings, List.of(collectionID), responseDispatcher,
                mock(PillarAlarmDispatcher.class), auditManager);
    }

    String collectionID() {
        return collectionID;
    }

    StorageModel model() {
        return model;
    }

    ResponseDispatcher responseDispatcher() {
        return responseDispatcher;
    }

    AuditTrailManager auditManager() {
        return auditManager;
    }

    MessageHandlerContext context() {
        return context;
    }

    MessageContext messageContext() {
        return messageContext;
    }

    /**
     * @param fileName The name of the file in the temporary directory.
     * @return A {@code file://} address for delivering data to the temporary directory.
     */
    String deliveryAddress(String fileName) {
        return tempDir.resolve(fileName).toUri().toString();
    }

    /**
     * @return A {@code file://} address in a non-existing directory, so delivery to it fails.
     */
    String undeliverableAddress() {
        return tempDir.resolve("missing-dir").resolve("result").toUri().toString();
    }

    /**
     * @return All responses dispatched by the handler, in the order they were sent.
     */
    List<MessageResponse> dispatchedResponses() {
        ArgumentCaptor<MessageResponse> captor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(responseDispatcher, atLeastOnce()).dispatchResponse(captor.capture(), any());
        return captor.getAllValues();
    }

    static void assertResponseCode(ResponseCode expected, RequestHandlerException e) {
        Assertions.assertEquals(expected, e.getResponseInfo().getResponseCode());
    }
}
