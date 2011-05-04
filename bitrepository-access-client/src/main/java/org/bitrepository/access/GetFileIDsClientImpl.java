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
package org.bitrepository.access;

import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prototype GetFileIDs Client
 */
public class GetFileIDsClientImpl implements GetFileIDsClient {

    private Logger log = LoggerFactory.getLogger(GetFileIDsClientImpl.class);

    /** The connection to the message bus.*/
    private final MessageBus messageBus;
    /** The queue to talk. */
    private final String queue;
    /** The GetClientServer, which receives the messages.*/
    private final GetFileIDsClientMessageListener messageListener;

    /** The configuration for the access module.*/
    private final AccessConfiguration config;

    /** Map for keeping track of which files are outstanding for retrieval. Key is the unique id for the file and value
     * is the container for keeping track the outstanding file instance.*/
    private Map<FileIdInstance, OutstandingFileID> outstandingFiles = Collections.synchronizedMap(
            new HashMap<FileIdInstance, OutstandingFileID>());
    /** Map for the files which are attempted to be retrieved from the pillars. Maps between unique identification
     * of the file and the id for the pillar, where it is being retrieved from. */
    private Map<FileIdInstance, String> awaitingComplete = Collections.synchronizedMap(new HashMap<FileIdInstance,
            String>());

    /** TODO The correlationIDs should be generated such that we avoid id conflicts. */
    private int correlationIDcounter;

    public GetFileIDsClientImpl() {
        config = AccessComponentFactory.getInstance().getConfig();

        // retrieve the queue from the configuration.
        queue = config.getGetFileIDsClientQueue();
        messageListener = new GetFileIDsClientMessageListener(this);

        // Add the messageListener to the messagebus for listening to the queue.
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        messageBus.addListener(queue, messageListener);

        // retrieve time out and expected number of pillars from configuration

        // initialise correlationIDcounter
        correlationIDcounter = 786543;
    }

    // BItRepositoryMessages.xsd IdentifyPillarsForGetFileIDsRequest element
    // FileIDs uniquely identifies the data in given SLA which the file ids are requested for
    // Given in order to be able to give possible estimate of time to deliver
    // I think it should be possible to give a FileIDs list or address to this method
    @Override
    public List<IdentifyPillarsForGetFileIDsResponse> identifyPillarsForGetFileIDs(String slaID) {
        // create identifyPillarsForGetFileIDsRequest message
        String corrID = "ipfgir" + correlationIDcounter;
        correlationIDcounter++;
        IdentifyPillarsForGetFileIDsRequest identifyRequest = GetFileIDsClientMessageFactory.
                getIdentifyPillarsForGetFileIDsRequestTestMessage(corrID, slaID, queue, null);

        // send message
        messageBus.sendMessage(queue, identifyRequest);

        // wait for responses? It would be nicer to be notified...

        return null;
    }

    @Override
    public File getFileIDs(String slaID, String queue, String pillarID) {
        return null;  //Todo implement getFileIDs
    }

    public void handleIdentifyPillarsForGetFileIDsResponse(IdentifyPillarsForGetFileIDsResponse msg) {

        // TODO

    }

    public void handleGetFileIDsResponse(GetFileIDsResponse msg) {

        // TODO

    }

    public void handleGetFileIDsComplete(GetFileIDsComplete msg) {

        // TODO

    }
}
