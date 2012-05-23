/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getfile.conversation;

import java.net.URL;

import org.bitrepository.access.getfile.selectors.GetFileSelector;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * The context for the conversation for the GetFile operation.
 */
public class GetFileConversationContext extends ConversationContext {
    /** @see getFileID() */
    private final String fileID;
    /** @see getFilePart() */
    private FilePart filePart;
    /** @see getUrlForResult() */
    private final URL urlForResult;
    /** @see getSelector() */
    private final GetFileSelector selector;

    /**
     * Constructor.
     * @param fileID The id of the file to retrieve.
     * @param urlForResult The address for the delivery of the results.
     * @param filePart The part of the file. Null if whole file is wanted.
     * @param selector The selector for choosing the pillar to retrieve from.
     * @param settings The settings.
     * @param messageSender The message sender.
     * @param clientID The id of the client.
     * @param eventHandler The eventhandler. 
     * @param auditTrailInformation The audit trail information for the conversation.
     */
    public GetFileConversationContext(String fileID, URL urlForResult, FilePart filePart, GetFileSelector selector,
            Settings settings, MessageSender messageSender, String clientID, EventHandler eventHandler,
            String auditTrailInformation) {
        super(settings, messageSender, clientID, eventHandler, auditTrailInformation);
        this.fileID = fileID;
        this.filePart = filePart;
        this.urlForResult = urlForResult;
        this.selector = selector;
    }

    /**
     * @return The id of the file to retrieve.
     */
    public String getFileID() {
        return fileID;
    }

    /**
     * @return The part of the file to retrieve (null if whole file).
     */
    public FilePart getFilePart() {
        return filePart;
    }
    
    /**
     * @return The URL for the results to be delivered.
     */
    public URL getUrlForResult() {
        return urlForResult;
    }
    
    /**
     * @return The selector for selecting the pillar, where the file is to be retrieved.
     */
    public GetFileSelector getSelector() {
        return selector;
    }
}
