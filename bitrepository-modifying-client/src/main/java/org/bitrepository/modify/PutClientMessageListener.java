/*
 * #%L
 * Bitmagasin modify client
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
package org.bitrepository.modify;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.protocol.AbstractMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MessageListener for the PutClient.
 */
public class PutClientMessageListener extends AbstractMessageListener {
    /** The log for this class.*/
    private final Logger log = LoggerFactory.getLogger(PutClientMessageListener.class);

    /** The PutClient dedicated to this message listener instance.*/
    private PutClientAPI client;
    
    /** 
     * Constructor. Is instantiated by the PutClient.
     * 
     * @param gc The PutClient for this instance.
     */
    public PutClientMessageListener(PutClientAPI pc) {
        this.client = pc;
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse msg) {
        log.debug("Received IdentifyPillarsForPutFileResponse message.");
        client.identifyResponse(msg);
    }
    
    @Override
    public void onMessage(PutFileProgressResponse msg) {
        log.debug("Received PutFileProgressResponse message.");
        client.handlePutProgressResponse(msg);
    }
    
    @Override
    public void onMessage(PutFileFinalResponse msg) {
        log.debug("Received PutFileFinalResponse message.");
        client.handlePutFinalResponse(msg);
    }    
}
