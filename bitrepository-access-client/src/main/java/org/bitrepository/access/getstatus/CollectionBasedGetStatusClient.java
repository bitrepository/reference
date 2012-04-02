package org.bitrepository.access.getstatus;

import javax.jms.JMSException;

import org.bitrepository.access.getstatus.conversation.SimpleGetStatusConversation;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionBasedGetStatusClient implements GetStatusClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The injected settings for the instance */
    private final Settings settings;

    /** The injected messagebus to use */
    private final MessageBus messageBus;
    /** The mediator which should manage the conversations. */
    private final ConversationMediator conversationMediator;
    
    /**
     * Constructor
     * @param messageBus the message bus to use.
     * @param conversationMediator the mediator to facilitate message tranmission
     * @param settings the settings to use.  
     */
    public CollectionBasedGetStatusClient(MessageBus messageBus, ConversationMediator conversationMediator, 
            Settings settings) {
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        this.settings = settings;
        ArgumentValidator.checkNotNullOrEmpty(settings.getCollectionSettings().getGetStatusSettings().getContributorIDs(),
                "ContributorIDs");
        this.conversationMediator = conversationMediator;
        this.messageBus = messageBus;
    }
    

    @Override
    public void getStatus(EventHandler eventHandler) {
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");

        log.info("Requesting status for collection of components.");
        SimpleGetStatusConversation conversation = new SimpleGetStatusConversation(messageBus, 
                settings, settings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                eventHandler, new FlowController(settings));
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
