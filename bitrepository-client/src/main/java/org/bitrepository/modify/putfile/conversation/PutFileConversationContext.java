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
package org.bitrepository.modify.putfile.conversation;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.OperationType;
import org.bitrepository.protocol.messagebus.MessageSender;

import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;

public class PutFileConversationContext extends ConversationContext {
    private final URL urlForFile;
    private final BigInteger fileSize;
    private final ChecksumDataForFileTYPE checksumForValidationAtPillar;
    private final ChecksumSpecTYPE checksumRequestsForValidation;

    public PutFileConversationContext(String collectionID, String fileID, URL urlForFile, long fileSize,
                                      ChecksumDataForFileTYPE checksumForValidationAtPillar, ChecksumSpecTYPE checksumRequestsForValidation,
                                      Settings settings, MessageSender messageSender, String clientID, Collection<String> contributors,
                                      EventHandler eventHandler, String auditTrailInformation) {
        super(collectionID, OperationType.PUT_FILE, settings, messageSender, clientID, fileID, contributors,
                eventHandler, auditTrailInformation);
        this.urlForFile = urlForFile;
        this.fileSize = new BigInteger(Long.toString(fileSize));
        this.checksumForValidationAtPillar = checksumForValidationAtPillar;
        this.checksumRequestsForValidation = checksumRequestsForValidation;
    }

    public URL getUrlForFile() {
        return urlForFile;
    }

    public BigInteger getFileSize() {
        return fileSize;
    }

    public ChecksumDataForFileTYPE getChecksumForValidationAtPillar() {
        return checksumForValidationAtPillar;
    }

    public ChecksumSpecTYPE getChecksumRequestForValidation() {
        return checksumRequestsForValidation;
    }

}
