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
package org.bitrepository.modify;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileReply;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileResponse;
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
    public void onMessage(IdentifyPillarsForPutFileReply msg) {
        log.debug("Received IdentifyPillarsForPutFileReply message.");
        client.identifyReply(msg);
    }
    
    @Override
    public void onMessage(PutFileResponse msg) {
        log.debug("Received PutFileResponse message.");
        client.handlePutResponse(msg);
    }
    
    @Override
    public void onMessage(PutFileComplete msg) {
        log.debug("Received PutFileComplete message.");
        client.handlePutComplete(msg);
    }    
}
