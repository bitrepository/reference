/*
 * #%L
 * Bitrepository Access Client
 * *
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
package org.bitrepository.access.getfileids;

import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetFileIDsClientMessageListener takes the message and sends it to the corresponding method in the GetFileIDsClient.
 */
public class GetFileIDsClientMessageListener extends AbstractMessageListener {
    /** The log for this class.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The BasicGetFileIDsClient which should handle the messages.*/
    private BasicGetFileIDsClient client;

    /**
     * Constructor.
     * @param gfic The GetFileIDsClient which should handle the content of the messages.
     */
    public GetFileIDsClientMessageListener(BasicGetFileIDsClient gfic) {
        this.client = gfic;
    }

    /**
     * Method for handling the IdentifyPillarsForGetFileIDsResponse messages.
     * @param msg The IdentyfyPillarsForGetFileIDsResponse message.
     */
    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse msg) {
        log.info("Received IdentifyPillarsForGetFileIDsResponse '" + msg + "'.");
        client.handleIdentifyPillarsForGetFileIDsResponse(msg);
    }

    /**
     * Method for handling the GetFileIDsProgressResponse messages.
     * @param msg The GetFileIDsProgressResponse message.
     */
    @Override
    public void onMessage(GetFileIDsProgressResponse msg) {
        log.info("Received GetFileIDsProgressResponse message '" + msg + "'.");
        client.handleGetFileIDsProgressResponse(msg);
    }

    /**
     * Method for handling the GetFileIDsFinalResponse messages.
     * @param msg The GetFileIDsFinalResponse message.
     */
    @Override
    public void onMessage(GetFileIDsFinalResponse msg) {
        log.info("Reieved GetFileFinalResponse message '" + msg + "'.");
        client.handleGetFileIDsFinalResponse(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        log.info("GetFileIDsClientMessageListener.onMessage IdentifyPillarsForGetFileIDsRequest message IGNORE");
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        log.info("GetFileIDsClientMessageListener.onMessage GetFileIDsRequest message IGNORE");
    }

}
