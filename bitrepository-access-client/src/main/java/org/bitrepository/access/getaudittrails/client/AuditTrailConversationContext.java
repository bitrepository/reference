package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.ConversationContext;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

public class AuditTrailConversationContext extends ConversationContext {
    private final AuditTrailQuery[] componentQueries;
    private final FileIDs fileIDs;
    private final String urlForResult;

    public AuditTrailConversationContext(AuditTrailQuery[] componentQueries, FileIDs fileIDs, String urlForResult,
            Settings settings, MessageSender messageSender, EventHandler eventHandler,
            String auditTrailInformation) {
        super(settings, messageSender, eventHandler, auditTrailInformation);
        this.componentQueries = componentQueries;
        this.fileIDs = fileIDs;
        this.urlForResult = urlForResult;
    }

    public AuditTrailQuery[] getComponentQueries() {
        return componentQueries;
    }

    public FileIDs getFileIDs() {
        return fileIDs;
    }

    public String getUrlForResult() {
        return urlForResult;
    }
}
