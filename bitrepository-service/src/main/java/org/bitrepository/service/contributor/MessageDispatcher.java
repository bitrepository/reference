package org.bitrepository.service.contributor;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Provides the general functionality for sending reposnes from a pillar.
 */
public class MessageDispatcher {
    protected final Settings settings;
    private final MessageSender sender;

    public MessageDispatcher(Settings settings, MessageSender sender) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(sender, "sender");
        this.settings = settings;
        this.sender = sender;
    }

    /**
     * Completes and sends a given message.
     * All the values of the specific response elements has to be set, including the ResponseInfo.
     * <br/> Sets the fields:
     * <br/> CollectionID
     * <br/> From
     * <br/> MinVersion
     * <br/> Version
     *
     * @param message The message which only needs the basic information to be send.
     */
    protected void dispatchMessage(Message message) {
        message.setCollectionID(settings.getCollectionID());
        message.setFrom(settings.getComponentID());
        message.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        message.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());
        sender.sendMessage(message);
    }
}
