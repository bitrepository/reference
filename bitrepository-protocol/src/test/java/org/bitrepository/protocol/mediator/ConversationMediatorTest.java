package org.bitrepository.protocol.mediator;

import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.testng.annotations.Test;

/**
 * Test the general ConversationMediator functionality.
 */
@Test
public abstract class ConversationMediatorTest {
    protected MutableClientSettings settings = new MutableClientSettings(); 
    protected MessageBus messagebus;
    protected String listenerDestination;
    
    /**
     * Validates the core mediator functionality of delegating messages from the message bus to the relevant 
     * conversation.
     */
    @Test (groups = {"testfirst"})
    public void messagedelegationTest() {
        //  ConversationMediator mediator = createMediator(settings, messagebus, listenerDestination);
    }
    
    abstract ConversationMediator<?> createMediator(
            ClientSettings settings, 
            MessageBus messagebus,
            String listenerDestination);
}
