package org.bitrepository.service.contributor.handler;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.audit.MockAuditManager;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.contributor.ResponseDispatcher;
import org.bitrepository.service.exception.InvalidMessageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class GetAuditTrailsRequestHandlerTest {
    private static final String CONTRIBUTOR_ID = "GetAuditTrailsTestContributor";

    @TempDir
    Path tempDir;

    private final List<Message> sentMessages = new ArrayList<>();
    private final MessageContext messageContext = new MessageContext(null);
    private MockAuditManager auditManager;
    private String collectionID;
    private GetAuditTrailsRequestHandler handler;

    @BeforeEach
    void setUp() {
        Settings settings = TestSettingsProvider.reloadSettings(CONTRIBUTOR_ID);
        collectionID = settings.getCollections().get(0).getID();
        auditManager = new MockAuditManager();
        ContributorContext context = new ContributorContext(new ResponseDispatcher(settings, sentMessages::add),
                null, settings);
        handler = new GetAuditTrailsRequestHandler(context, auditManager);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void returnsAuditTrailsInTheFinalResponseWithoutResultAddress() throws Exception {
        handler.processRequest(createRequest(null), messageContext);

        Assertions.assertEquals(2, sentMessages.size());
        Assertions.assertInstanceOf(GetAuditTrailsProgressResponse.class, sentMessages.get(0));
        GetAuditTrailsFinalResponse finalResponse = (GetAuditTrailsFinalResponse) sentMessages.get(1);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertNotNull(finalResponse.getResultingAuditTrails().getAuditTrailEvents());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void deliversAuditTrailsToTheResultAddress() throws Exception {
        String resultAddress = tempDir.resolve("audit-trails").toUri().toString();

        handler.processRequest(createRequest(resultAddress), messageContext);

        GetAuditTrailsFinalResponse finalResponse = (GetAuditTrailsFinalResponse) sentMessages.get(1);
        Assertions.assertEquals(resultAddress, finalResponse.getResultingAuditTrails().getResultAddress());
        Assertions.assertTrue(Files.readString(tempDir.resolve("audit-trails")).contains(collectionID));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file:///has space", "relative/path", "ftp://host/result"})
    @Tag(TestGroups.REGRESSIONTEST)
    void invalidResultAddressIsRejectedBeforeAnyResponse(String resultAddress) {
        InvalidMessageException e = Assertions.assertThrows(InvalidMessageException.class,
                () -> handler.processRequest(createRequest(resultAddress), messageContext));

        Assertions.assertEquals(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, e.getResponseInfo().getResponseCode());
        Assertions.assertTrue(sentMessages.isEmpty(), "Actual: " + sentMessages);
        Assertions.assertEquals(0, auditManager.getCallsForGetAudits());
    }

    private GetAuditTrailsRequest createRequest(String resultAddress) {
        GetAuditTrailsRequest request = new GetAuditTrailsRequest();
        request.setCollectionID(collectionID);
        request.setContributor(CONTRIBUTOR_ID);
        request.setResultAddress(resultAddress);
        request.setCorrelationID("correlation-id");
        request.setFrom("test-client");
        return request;
    }
}
