package org.bitrepository.protocol.conversation;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Encapsulates the shared state between a conversation and the related conversation states.
 */
public class ConversationContext {
    private final String conversationID;
    private final Settings settings;
    public final MessageSender messageSender;
    private final ConversationEventMonitor monitor;
    private final String auditTrailInformation;
    private GeneralConversationState state;

    public ConversationContext(
            Settings settings,
            MessageSender messageSender,
            EventHandler eventHandler,
            String auditTrailInformation) {
        this.settings = settings;
        this.messageSender = messageSender;
        this.conversationID = ConversationIDGenerator.generateConversationID();
        this.monitor = new ConversationEventMonitor(conversationID, eventHandler);
        this.auditTrailInformation = auditTrailInformation;
    }

    public String getConversationID() {
        return conversationID;
    }

    public Settings getSettings() {
        return settings;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public ConversationEventMonitor getMonitor() {
        return monitor;
    }

    public String getAuditTrailInformation() {
        return auditTrailInformation;
    }

    public GeneralConversationState getState() {
        return state;
    }
    public void setState(GeneralConversationState state) {
        this.state = state;
    }
}
