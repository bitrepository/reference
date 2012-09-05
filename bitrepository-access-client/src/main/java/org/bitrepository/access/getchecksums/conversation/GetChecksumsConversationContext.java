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
package org.bitrepository.access.getchecksums.conversation;

import java.net.URL;
import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/** Encapsulates the context for a GetChecksums operation */
public class GetChecksumsConversationContext extends ConversationContext {
    private final Collection<String> pillars;
    private final FileIDs fileIDs;
    private final URL urlForResult;
    private final ChecksumSpecTYPE checksumSpec;

    public GetChecksumsConversationContext(Collection<String> contributors, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, URL urlForResult,
            Settings settings, MessageSender messageSender, String clientID, EventHandler eventHandler,
            String auditTrailInformation) {
        super(settings, messageSender, clientID, eventHandler, auditTrailInformation);
        this.pillars = contributors;
        this.fileIDs = fileIDs;
        this.urlForResult = urlForResult;       
        this.checksumSpec = checksumSpec;
    }

    public FileIDs getFileIDs() {
        return fileIDs;
    }

    public URL getUrlForResult() {
        return urlForResult;
    }
    
    public ChecksumSpecTYPE getChecksumSpec() {
        return checksumSpec;
    }

    public Collection<String> getPillars() {
        return pillars;
    }
}
