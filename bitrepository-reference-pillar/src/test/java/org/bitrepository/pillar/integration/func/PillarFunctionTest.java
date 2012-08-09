package org.bitrepository.pillar.integration.func;


import org.bitrepository.client.MessageReceiver;
import org.bitrepository.pillar.integration.PillarIntegrationTest;

public class PillarFunctionTest extends PillarIntegrationTest {
    protected MessageReceiver clientReceiver;

    /**
     * Adds a client topic listener.
     */
    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        clientReceiver = new MessageReceiver("Client topic receiver", testEventManager);
        messageBus.addListener(componentSettings.getReceiverDestinationID(), clientReceiver.getMessageListener());
    }
}
