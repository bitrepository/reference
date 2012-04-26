package org.bitrepository.integrityservice.contributor;

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

    @BeforeMethod
    public void setupContext() {
        context = new ContributorContext(
                messageBus, settings, CONTRIBUTOR_ID, contributorDestinationId);
        statusMessageFactory = new GetStatusContributorTestMessageFactory(settings.getCollectionID(), CONTRIBUTOR_ID,
                collectionDestinationID, CLIENT_ID, clientDestinationId);
    }

    @Test
    public void identifyContributorsForGetStatusRequestTest() {
        Contributor contributor = new ContributorForIntegrityService(messageBus, context);
        contributor.start();
        IdentifyContributorsForGetStatusRequest identifyRequest =
                statusMessageFactory.createIdentifyContributorsForGetStatusRequest();
        messageBus.sendMessage(identifyRequest);
        IdentifyContributorsForGetStatusResponse response =
                collectionDestination.waitForMessage(IdentifyContributorsForGetStatusResponse.class);
        Assert.assertEquals(collectionDestination.waitForMessage(IdentifyContributorsForGetStatusResponse.class),
                statusMessageFactory.createExpectedIdentifyContributorsForGetStatusResponse(response));
    }

    @Override
    protected String getContributorID() {
        return CONTRIBUTOR_ID;
    }
}
