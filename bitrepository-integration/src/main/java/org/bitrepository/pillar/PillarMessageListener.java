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
package org.bitrepository.pillar;

import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.AbstractMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple message listener for the reference pillar.
 * Handles all the different request messages by sending them directly to the
 * reference pillar. 
 */
public class PillarMessageListener extends AbstractMessageListener {
    /** The log for this class.*/
    private final Logger log = LoggerFactory.getLogger(
            PillarMessageListener.class);

    /** The PutClient dedicated to this message listener instance.*/
    private PillarAPI client;
    
    /** 
     * Constructor. Is instantiated by the PutClient.
     * 
     * @param gc The PutClient for this instance.
     */
    public PillarMessageListener(PillarAPI pillar) {
        this.client = pillar;
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest msg) {
        log.debug("Received IdentifyPillarsForGetFileRequest message.");
        client.identifyForGetFile(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest msg) {
        log.debug("Received IdentifyPillarsForGetFileIDsRequest message.");
        client.identifyForGetFileIds(msg);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest msg) {
        log.debug("Received IdentifyPillarsForGetChecksumsRequest message.");
        client.identifyForGetChecksum(msg);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest msg) {
        log.debug("Received IdentifyPillarsForPutFileRequest message.");
        client.identifyForPutFile(msg);
    }
    
    @Override
    public void onMessage(GetChecksumsRequest msg) {
        log.debug("Received GetChecksumsRequest message.");
        client.getChecksum(msg);
    }

    @Override
    public void onMessage(GetFileIDsRequest msg) {
        log.debug("Received GetFileIDsRequest message.");
        client.getFileIds(msg);
    }

    @Override
    public void onMessage(GetFileRequest msg) {
        log.debug("Received GetFileRequest message.");
        client.getFile(msg);
    }
    
    @Override
    public void onMessage(PutFileRequest msg) {
        log.debug("Received PutFileRequest message.");
        client.putFile(msg);
    }
}
