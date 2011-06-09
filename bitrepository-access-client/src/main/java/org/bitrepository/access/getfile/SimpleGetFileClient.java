/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfile;

import org.bitrepository.access.AccessException;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.CollectionBasedConversationMediator;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The client for sending and handling 'GetFile' operations.
 * Is able to either retrieve a file from a specific pillar, or to identify how fast each pillar in a given SLA is to
 * retrieve  a specific file and then retrieve it from the fastest pillar.
 * The files are delivered to a preconfigured directory.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimpleGetFileClient extends CollectionBasedConversationMediator<SimpleGetFileConversation> implements
        GetFileClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ClientSettings slaConfiguration;

    public SimpleGetFileClient(MessageBus messagebus, 
            SimpleGetFileConversationFactory simpleGetFileConversationFactory,
            ClientSettings slaConfiguration) {
        super(simpleGetFileConversationFactory, messagebus, slaConfiguration.getClientTopicId());
        log.info("Initialized the GetFileClient");

        this.slaConfiguration = slaConfiguration;
    }

    @Override
    public void getFileFromFastestPillar(String fileID, URL uploadUrl) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        
        log.info("Requesting fastest retrieval of the file '" + fileID + "' which belong to the SLA '" + 
                slaConfiguration.getId() + "'.");
        // Create message requesting delivery time for the given file.
        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        msg.setMinVersion(BigInteger.valueOf(1L));
        msg.setVersion(BigInteger.valueOf(1L));
        msg.setBitrepositoryContextID(slaConfiguration.getId());
        msg.setFileID(fileID);
        msg.setReplyTo(slaConfiguration.getClientTopicId());

        SimpleGetFileConversation conversation = startConversation();
        conversation.sendMessage(slaConfiguration.getSlaTopicId(), msg);

        // TODO: Should we wait for the conversation to end before returning? How is the result delivered?
    }

    @Override
    public void getFileFromSpecificPillar(String fileID, URL uploadUrl, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "pillarID");
        
        log.info("Requesting the file '" + fileID + "' from pillar '" + pillarID + "'.");

        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        msg.setMinVersion(BigInteger.valueOf(1L));
        msg.setVersion(BigInteger.valueOf(1L));
        msg.setBitrepositoryContextID(slaConfiguration.getId());
        msg.setFileID(fileID);
        //msg.setPillarId(pillarID);
        if (true) throw new UnsupportedOperationException("The get file by pillar identification isn't currently " +
        		"supported by the protocol");
        msg.setReplyTo(slaConfiguration.getClientTopicId());

        SimpleGetFileConversation conversation = startConversation();
        conversation.sendMessage(slaConfiguration.getSlaTopicId(), msg);

        // TODO: Should we wait for the conversation to end before returning? How is the result delivered?
    }
    
    
}
