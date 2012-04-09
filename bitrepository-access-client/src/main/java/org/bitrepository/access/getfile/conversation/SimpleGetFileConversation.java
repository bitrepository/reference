/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfile.conversation;

import java.net.URL;
import java.util.UUID;

import org.bitrepository.access.getfile.selectors.PillarSelectorForGetFile;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * A conversation for GetFile.
 *
 * Logic for behaving sanely in GetFile conversations.
 */
public class SimpleGetFileConversation extends AbstractConversation {
    /** The sender to use for dispatching messages */
    final MessageSender messageSender; 
    /** The configuration specific to the SLA related to this conversion. */
    final Settings settings;

    /** The url which the pillar should upload the file to. */
    final URL uploadUrl;
    /** The ID of the file which should be uploaded to the supplied url */
    final String fileID;
    /** Selects a pillar based on responses. */
    final PillarSelectorForGetFile selector;
    /** The conversation state (State pattern) */
    GetFileState conversationState;
    /**
     * Initializes the file directory, and the message bus used for sending messages.
     * The fileDir is retrieved from the configuration.
     *
     * @param messageSender The message bus used for sending messages.
     */
    public SimpleGetFileConversation(
            MessageSender messageSender, 
            Settings settings,
            PillarSelectorForGetFile selector,
            String fileID,
            URL uploadUrl,
            EventHandler eventHandler,
            FlowController flowController) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);

        this.messageSender = messageSender;
        this.settings = settings;
        this.selector = selector;
        this.uploadUrl = uploadUrl;
        this.fileID = fileID;
        conversationState = new IdentifyingPillarsForGetFile(this);
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof GetFileFinished;
    }

    public URL getResult() {
        return uploadUrl;
    }

    @Override
    public synchronized void onMessage(GetFileFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(GetFileProgressResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForGetFileResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public void endConversation() {
        conversationState.endConversation();
    }
    
    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }
}
