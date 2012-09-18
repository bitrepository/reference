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
import java.util.Collection;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * The context for the conversation for the GetFile operation.
 */
public class GetFileConversationContext extends ConversationContext {
    private FilePart filePart;
    private final URL urlForResult;

    /**
     * @see ConversationContext for general parameter documentation.
     * @param filePart The part of the file. Null if whole file is wanted.
     * @param urlForResult
     */
    public GetFileConversationContext(
            String fileID, URL urlForResult, FilePart filePart, Collection<String> contributors,
            Settings settings, MessageSender messageSender, String clientID, EventHandler eventHandler,
            String auditTrailInformation) {
        super(settings, messageSender, clientID, fileID, contributors, eventHandler, auditTrailInformation);
        this.filePart = filePart;
        this.urlForResult = urlForResult;
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
}
