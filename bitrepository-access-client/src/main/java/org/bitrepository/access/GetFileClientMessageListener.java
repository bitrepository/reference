/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.access;

import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.protocol.AbstractMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler of the messages. Just takes the message and sends it to the corresponding method in the actual GetClient.
 */
public class GetFileClientMessageListener extends AbstractMessageListener {
    /** The log for this class.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The GetClient which should handle the messages.*/
    private GetFileClientAPI client;
    
    /**
     * Constructor.
     * @param gc The GetClient which should handle the content of the messages.
     */
    public GetFileClientMessageListener(GetFileClientAPI gc) {
        this.client = gc;
    }

    /**
     * Method for handling the IdentifyPillarsForGetFileReply messages.
     * 
     * @param msg The IdentyfyPillarsForGetFileReply message.
     */
    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse msg) {
        log.info("Received IdentifyPillarsForGetFileReply '" + msg + "'.");
        client.handleIdentifyPillarsForGetFileResponse(msg);
    }
    
    /**
     * Method for handling the GetFileResponse messages.
     * 
     * @param msg The GetFileResponse message.
     */
    @Override
    public void onMessage(GetFileResponse msg) {
        log.info("Received GetFileResponse message '" + msg + "'.");
        client.handleGetFileResponse(msg);
    }
    
    /**
     * Method for handling the GetFileComplete messages.
     * 
     * @param msg The GetFileComplete message.
     */
    @Override
    public void onMessage(GetFileComplete msg) {
        log.info("Reieved GetFileComplete message '" + msg + "'.");
        client.handleGetFileComplete(msg);
    }
}
