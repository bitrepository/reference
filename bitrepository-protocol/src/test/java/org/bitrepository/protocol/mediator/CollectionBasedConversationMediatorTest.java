package org.bitrepository.protocol.mediator;

import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.testng.annotations.Test;

@Test
public class CollectionBasedConversationMediatorTest extends ConversationMediatorTest {

    @Override
    ConversationMediator<?> createMediator(
            ClientSettings settings, 
            MessageBus messagebus,
            String listenerDestination) {
        return new CollectionBasedConversationMediator(settings, messagebus, listenerDestination);
    }
}
