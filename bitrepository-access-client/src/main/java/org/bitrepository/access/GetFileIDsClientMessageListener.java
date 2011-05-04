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
/*
 * org.bitrepository.access
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
package org.bitrepository.access;

import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.AbstractMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetFileIDsClientMessageListener takes the message and sends it to the corresponding method in the GetFileIDsClient.
 */
public class GetFileIDsClientMessageListener extends AbstractMessageListener {
    /** The log for this class.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The GetFileIDsClientImpl which should handle the messages.*/
    private GetFileIDsClientImpl client;

    /**
     * Constructor.
     * @param gfic The GetFileIDsClient which should handle the content of the messages.
     */
    public GetFileIDsClientMessageListener(GetFileIDsClientImpl gfic) {
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
     * Method for handling the GetFileIDsResponse messages.
     * @param msg The GetFileIDsResponse message.
     */
    @Override
    public void onMessage(GetFileIDsResponse msg) {
        log.info("Received GetFileIDsResponse message '" + msg + "'.");
        client.handleGetFileIDsResponse(msg);
    }

    /**
     * Method for handling the GetFileIDsComplete messages.
     * @param msg The GetFileIDsComplete message.
     */
    @Override
    public void onMessage(GetFileIDsComplete msg) {
        log.info("Reieved GetFileComplete message '" + msg + "'.");
        client.handleGetFileIDsComplete(msg);
    }
}
