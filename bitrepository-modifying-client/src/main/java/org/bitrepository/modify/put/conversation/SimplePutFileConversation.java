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
package org.bitrepository.modify.put.conversation;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.modify.put.PutFileClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.UUID;

/**
 * A conversation for PutFile.
 * Logic for behaving sanely in PutFile conversations.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimplePutFileConversation extends AbstractConversation<URL> {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The sender to use for dispatching messages */
    final MessageSender messageSender;
    /** The configuration specific to the SLA related to this conversion. */
    final ClientSettings settings;

    /** The URL which the pillar should download the file from. */
    final URL downloadUrl;
    /** The ID of the file which should be downloaded from the supplied URL. */
    final String fileID;
    /** The checksum of the file, which the pillars should download.*/
    final ChecksumDataForFileTYPE checksum;
    /** The event handler to send notifications of the get file progress */
    final EventHandler eventHandler;
    /** The state of the PutFile transaction.*/
    PutFileState conversationState;
    /** The exception if the operation failed.*/
    OperationFailedException operationFailedException;

    /**
     * Constructor.
     * Initializes all the variables for the conversation.
     *
     * @param messageSender The instance to send the messages with.
     * @param settings The settings of the client.
     * @param urlToDownload The URL where the file to be 'put' is located.
     * @param fileId The id of the file.
     * @param checksum The checksum of the file to upload.
     * @param eventHandler The event handler.
     */
	public SimplePutFileConversation(MessageSender messageSender,
			PutFileClientSettings settings,
			URL urlToDownload,
			String fileId,
			ChecksumDataForFileTYPE checksum,
			EventHandler eventHandler) {
		super(messageSender, UUID.randomUUID().toString());

		this.messageSender = messageSender;
		this.settings = settings;
		this.downloadUrl = urlToDownload;
		this.fileID = fileId;
		this.checksum = checksum;
		this.eventHandler = eventHandler;
	}

	@Override
	public void failConversion(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public URL getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasEnded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startConversion() throws OperationFailedException {
		// TODO Auto-generated method stub

	}
}
