package org.bitrepository.service.contributor;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 *
 */
public class ContributorContext {
    private final MessageSender dispatcher;
    private final Settings settings;
    private final String componentID;
    private final String replyTo;

    public ContributorContext(
            MessageSender dispatcher,
            Settings settings,
            String componentID,
            String replyTo) {
        this.dispatcher = dispatcher;
        this.settings = settings;
        this.componentID = componentID;
        this.replyTo = replyTo;
    }

    public MessageSender getDispatcher() {
        return dispatcher;
    }

    public Settings getSettings() {
        return settings;
    }

    public String getComponentID() {
        return componentID;
    }

    public String getReplyTo() {
        return replyTo;
    }
}
