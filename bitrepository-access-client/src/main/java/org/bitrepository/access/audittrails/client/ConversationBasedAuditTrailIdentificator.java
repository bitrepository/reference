package org.bitrepository.access.audittrails.client;

import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.client.AbstractClient;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;

public class ConversationBasedAuditTrailIdentificator extends AbstractClient implements AuditTrailIdentificator {
    /** The log for this class. */
    private final List<String> definedContributorIDs;
       
    public ConversationBasedAuditTrailIdentificator(Settings settings, ConversationMediator conversationMediator, MessageBus messageBus) {
        super(settings, conversationMediator, messageBus);
        this.definedContributorIDs = settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors();
    }

    @Override
    public void getAvailableContributors(
            EventHandler eventHandler, String auditTrailInformation) {
        startConversation(new AuditTrailIdentificationConversation(
                settings, messageBus, eventHandler, auditTrailInformation));
    }
    
    public List<String> getDefinedContributors() {
        return definedContributorIDs;
    }
}
