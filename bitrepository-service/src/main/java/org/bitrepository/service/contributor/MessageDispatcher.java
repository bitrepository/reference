package org.bitrepository.service.contributor;

/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
