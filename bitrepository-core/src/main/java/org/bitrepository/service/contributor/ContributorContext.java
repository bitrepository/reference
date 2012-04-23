package org.bitrepository.service.contributor;

import org.bitrepository.protocol.messagebus.MessageSender;

/**
 *
 */
public class ContributorContext {
    private final MessageSender dispatcher;
    private final String collectionID;
    private final String componentID;
    private final String replyTo;

    public ContributorContext(MessageSender dispatcher, String collectionID, String componentID, String replyTo) {
        this.dispatcher = dispatcher;
        this.collectionID = collectionID;
        this.componentID = componentID;
        this.replyTo = replyTo;
    }

    public MessageSender getDispatcher() {
        return dispatcher;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public String getComponentID() {
        return componentID;
    }

    public String getReplyTo() {
        return replyTo;
    }
}
