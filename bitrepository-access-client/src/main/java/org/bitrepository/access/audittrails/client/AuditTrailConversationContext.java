package org.bitrepository.access.audittrails.client;

import org.bitrepository.access.audittrails.AuditTrailQuery;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.ConversationContext;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

public class AuditTrailConversationContext extends ConversationContext {
    private final AuditTrailQuery[] componentQueries;
    private final String urlForResult;

    public AuditTrailConversationContext(AuditTrailQuery[] componentQueries, String urlForResult,
            Settings settings, MessageSender messageSender, EventHandler eventHandler,
            String auditTrailInformation) {
        super(settings, messageSender, eventHandler, auditTrailInformation);
        this.componentQueries = componentQueries;
        this.urlForResult = urlForResult;
    }

    public AuditTrailQuery[] getComponentQueries() {
        return componentQueries;
    }

    public String getUrlForResult() {
        return urlForResult;
    }
}
