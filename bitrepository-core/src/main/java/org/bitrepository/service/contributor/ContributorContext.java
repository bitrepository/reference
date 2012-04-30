package org.bitrepository.service.contributor;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * The context for the contributor mediator.
 */
public class ContributorContext {
    /** Message sender for this context.*/
    private final MessageSender dispatcher;
    /** The settings for thi context.*/
    private final Settings settings;
    /** The ID of the contributor component for this context.*/
    private final String componentID;
    /** The destination for this context.*/
    private final String replyTo;

    /**
     * 
     * @param dispatcher
     * @param settings
     * @param componentID
     * @param replyTo
     */
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
