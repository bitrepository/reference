package org.bitrepository.service;

import org.bitrepository.client.MessageReceiver;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

import java.math.BigInteger;

/**
 */
public abstract class ContributerTest extends IntegrationTest {
    protected static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;

    protected static String clientDestinationId;
    protected MessageReceiver clientTopic;

    protected static String contributorDestinationId;
    protected MessageReceiver contributorDestination;

    protected static final BigInteger defaultTime = BigInteger.valueOf(3000);

    @Override
    protected void teardownMessageBusListeners() {
        messageBus.removeListener(clientDestinationId, clientTopic.getMessageListener());
        messageBus.removeListener(contributorDestinationId, contributorDestination.getMessageListener());
        super.teardownMessageBusListeners();
    }

    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        clientDestinationId = settings.getReferenceSettings().getClientSettings().getReceiverDestination() + getTopicPostfix();
        settings.getReferenceSettings().getClientSettings().setReceiverDestination(clientDestinationId);
        clientTopic = new MessageReceiver("Client topic receiver", testEventManager);

        contributorDestinationId =  collectionDestinationID + "-" +  getContributorID() + "-" + getTopicPostfix();
        contributorDestination = new MessageReceiver(contributorDestinationId + " topic receiver", testEventManager);
        messageBus.addListener(clientDestinationId, clientTopic.getMessageListener());
        messageBus.addListener(contributorDestinationId, contributorDestination.getMessageListener());
    }

    protected abstract String getContributorID();
}
