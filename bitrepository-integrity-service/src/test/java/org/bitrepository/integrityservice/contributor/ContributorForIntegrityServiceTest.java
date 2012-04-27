package org.bitrepository.integrityservice.contributor;

import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.protocol.message.GetStatusContributorTestMessageFactory;
import org.bitrepository.service.ContributerTest;
import org.bitrepository.service.contributor.Contributor;
import org.bitrepository.service.contributor.ContributorContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 */
public class ContributorForIntegrityServiceTest extends ContributerTest {
    private ContributorContext context;
    private static final String CONTRIBUTOR_ID = "Integrity-Service";
    protected GetStatusContributorTestMessageFactory statusMessageFactory;
    private static final String CLIENT_ID = "ContributorForIntegrityServiceTest";

    @BeforeMethod (alwaysRun = true)
    public void setupContext() {
        context = new ContributorContext(
                messageBus, settings, CONTRIBUTOR_ID, contributorDestinationId);
        statusMessageFactory = new GetStatusContributorTestMessageFactory(
                settings.getCollectionID(), collectionDestinationID,
                CONTRIBUTOR_ID, contributorDestinationId,
                CLIENT_ID, clientDestinationId);
    }

    @Test(groups = {"regressiontest"})
    public void identifyContributorsForGetStatusRequestTest() {
        Contributor contributor = new ContributorForIntegrityService(messageBus, context);
        contributor.start();
        IdentifyContributorsForGetStatusRequest identifyRequest =
                statusMessageFactory.createIdentifyContributorsForGetStatusRequest();
        messageBus.sendMessage(identifyRequest);
        IdentifyContributorsForGetStatusResponse response =
                clientTopic.waitForMessage(IdentifyContributorsForGetStatusResponse.class);
        Assert.assertEquals(response,
                statusMessageFactory.createExpectedIdentifyContributorsForGetStatusResponse(response));
    }

    @Test(groups = {"regressiontest"})
    public void getStatusRequestTest() {
        Contributor contributor = new ContributorForIntegrityService(messageBus, context);
        contributor.start();
        GetStatusRequest identifyRequest =
                statusMessageFactory.createGetStatusRequest("xxxx");
        messageBus.sendMessage(identifyRequest);
        GetStatusFinalResponse response = clientTopic.waitForMessage(GetStatusFinalResponse.class);
        Assert.assertEquals(response,
                statusMessageFactory.createExpectedGetStatusFinalResponse(response));
    }

    @Override
    protected String getContributorID() {
        return CONTRIBUTOR_ID;
    }
}
