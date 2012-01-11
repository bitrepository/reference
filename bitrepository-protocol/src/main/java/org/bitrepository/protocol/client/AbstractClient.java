package org.bitrepository.protocol.client;

import javax.jms.JMSException;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.Conversation;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the generic functionality for a reference client
 */
public class AbstractClient implements BitrepositoryClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The settings for this instance.*/
    protected final Settings settings;
    /** The messagebus for communication.*/
    protected final MessageBus messageBus;   
    /** The mediator which should manage the conversations. */
    private final ConversationMediator conversationMediator;
    
    public AbstractClient(Settings settings, ConversationMediator conversationMediator, MessageBus messageBus) {
        this.settings = settings;
        this.messageBus = messageBus;
        this.conversationMediator = conversationMediator;
    }
    
    protected void startConversation(Conversation conversation) {
        conversationMediator.addConversation(conversation);
        conversation.startConversation();
    }

    @Override
    public void shutdown() {
        try {
            messageBus.close();
            // TODO Kill any lingering timer threads
        } catch (JMSException e) {
            log.info("Error during shutdown of messagebus ", e);
        }
    }

}
