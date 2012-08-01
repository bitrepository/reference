package org.bitrepository.pillar.integration;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;

public class PerformingPutFileIT extends PillarIntegrationTest {
    protected PutFileMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new PutFileMessageFactory(componentSettings, getComponentID(), pillarDestinationId);
    }

    protected String identifyPillarDestinationForPut(String pillarID) {s
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
            DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);

        addStep("Looking up " + pillarID + "s destination for put responses by sending a putFile identification " +
                "and selecting the response with the test pillarID ",
                "The pillar under test should make a response.");
        while (true) {
            IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
            if (receivedIdentifyResponse.getPillarID().equals(pillarID)) {
                return receivedIdentifyResponse.getReplyTo();
            }
        }
    }
}
