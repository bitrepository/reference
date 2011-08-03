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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prototype GetFileIDs Client.
 * TODO update to new interface
 */
public class BasicGetFileIDsClient implements GetFileIDsClient {

    /** The log for this class.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The connection to the message bus.*/
    private final MessageBus messageBus;
    /** The queue used by the client for incoming messages. */
    private final String queue;
    /** The GetClientServer, which receives the messages.*/
    private final GetFileIDsClientMessageListener messageListener;

    /** The configuration for the access module.*/
    private final AccessConfiguration config;

    private final long timeOut;
    private final int numberOfPillars;

    /** Map from correlationID to CountDownLatch counting down the identify responses we are waiting for. */
    private Map<String, CountDownLatch> identifyResponseCountDownLatchMap;
    /** Map from correlationID to List of IdentifyPillarsForGetFileIDsResponses. */
    private Map<String, List<IdentifyPillarsForGetFileIDsResponse>> identifyPillarsForGetFileIDsResponseMap;

    /** Map from correlationID to CountDownLatch counting down the Progress responses we are waiting for. */
    private Map<String, CountDownLatch> getFileIDsProgressResponseCountDownLatchMap;
    /** Map from correlationID to List of GetFileIDsProgressResponses. */
    private Map<String, GetFileIDsProgressResponse> getFileIDsProgressResponseMap;

    /** Map from correlationID to CountDownLatch counting down the FinalResponse messages we are waiting for. */
    private Map<String, CountDownLatch> getFileIDsFinalResponseCountDownLatchMap;
    /** Map from correlationID to List of GetFileIDsFinalResponse messages. */
    private Map<String, GetFileIDsFinalResponse> getFileIDsFinalResponseMap;

    public BasicGetFileIDsClient() {
        // TODO update (change use of messageBus and settings)
        config = AccessComponentFactory.getInstance().getConfig();

        // retrieve the queue from the configuration.
        queue = config.getGetFileIDsClientQueue();

        messageListener = new GetFileIDsClientMessageListener(this);

        // Add the messageListener to the messagebus for listening to the queue.
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        messageBus.addListener(queue, messageListener);

        // retrieve time out and expected number of pillars from configuration
        timeOut = Long.parseLong(config.getGetFileIDsClientTimeOut());
        numberOfPillars = Integer.parseInt(config.getGetFileIDsClientNumberOfPillars());

        identifyResponseCountDownLatchMap = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
        identifyPillarsForGetFileIDsResponseMap =
                Collections.synchronizedMap(new HashMap<String, List<IdentifyPillarsForGetFileIDsResponse>>());
        getFileIDsProgressResponseCountDownLatchMap = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
        getFileIDsProgressResponseMap = Collections.synchronizedMap(new HashMap<String, GetFileIDsProgressResponse>());
        getFileIDsFinalResponseCountDownLatchMap = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
        getFileIDsFinalResponseMap = Collections.synchronizedMap(new HashMap<String, GetFileIDsFinalResponse>());
    }

    // TODO update
    public List<IdentifyPillarsForGetFileIDsResponse> identifyPillarsForGetFileIDs(String bitRepositoryCollectionID,
                                                                                   FileIDs fileIDs)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        // create identifyPillarsForGetFileIDsRequest message
        String corrID = UUID.randomUUID().toString();
        log.debug("identifyPillarsForGetFileIDs new corrID " + corrID);
        IdentifyPillarsForGetFileIDsRequest identifyRequest = GetFileIDsClientMessageFactory.
                getIdentifyPillarsForGetFileIDsRequestMessage(corrID, bitRepositoryCollectionID, queue, null);

        // put correlationId and new CountDownLatch in identifyResponseCountDownLatchMap
        CountDownLatch countDownLatch = new CountDownLatch(numberOfPillars);
        identifyResponseCountDownLatchMap.put(corrID, countDownLatch);
        // put correlationId and new empty List in identifyPillarsForGetFileIDsResponseMap
        List<IdentifyPillarsForGetFileIDsResponse> responses = new ArrayList<IdentifyPillarsForGetFileIDsResponse>();
        identifyPillarsForGetFileIDsResponseMap.put(corrID, responses);

        // send message
        messageBus.sendMessage(identifyRequest);

        // wait for messages (or until specified waiting time elapses)
        try {
            boolean allPillarsResponded = countDownLatch.await(timeOut, TimeUnit.MILLISECONDS);
            if (!allPillarsResponded) {
                log.info("identifyPillarsForGetFileIDs time out. Not all pillars responded.");
            }
        } catch (InterruptedException e) {
            log.error("identifyPillarsForGetFileIDs InterruptedException",e);  // TODO handle exception
        }

        log.debug("identifyPillarsForGetFileIDs responseMap" +
                identifyPillarsForGetFileIDsResponseMap.get(corrID).toString());

        return responses;
    }

    // TODO update
    public GetFileIDsFinalResponse getFileIDs(String correlationID, String destination, String pillarID,
                                       String bitRepositoryCollectionID, FileIDs fileIDs, URL resultAddress)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        if (correlationID == null) {
            correlationID = UUID.randomUUID().toString();
            log.debug("getFileIDsFromFastestPillar new correlationID " + correlationID);
        }
        // create getFileIdsRequest
        GetFileIDsRequest request = GetFileIDsClientMessageFactory.
                getGetFileIDsRequestMessage(correlationID, bitRepositoryCollectionID, destination, pillarID, null, null);

        // put correlationID and new CountDownLatches in appropriate Maps
        CountDownLatch progressResponseCountDown = new CountDownLatch(1);
        // note there could be multiple progress responses, but we only wait for one...
        getFileIDsProgressResponseCountDownLatchMap.put(correlationID, progressResponseCountDown);
        CountDownLatch finalResponseCountDown = new CountDownLatch(1);
        getFileIDsFinalResponseCountDownLatchMap.put(correlationID, finalResponseCountDown);

        // send message
        messageBus.sendMessage(request);

        // wait for messages (or until specified waiting time elapses)
        try {
            boolean responseOk = progressResponseCountDown.await(timeOut, TimeUnit.MILLISECONDS);
            if (responseOk) {
                GetFileIDsProgressResponse response = getFileIDsProgressResponseMap.get(correlationID);
                // TODO how do we want to use the response?
                // if ProgressResponseInfo held info on expected remaining time, it could maybe be useful...
            } else {
                log.info("getFileIDsFromFastestPillar time out. No GetFileIDsProgressResponse received.");
                // TODO we did not receive a response before time out - it may still come or a complete message may come
            }
        } catch (InterruptedException e) {
            log.error("getFileIDsFromFastestPillar InterruptedException",e); // TODO handle exception
        }

        try {
            boolean completeOk = finalResponseCountDown.await(timeOut, TimeUnit.MILLISECONDS);
            if (completeOk) {
                return getFileIDsFinalResponseMap.get(correlationID);
                // TODO use other parts of final response message ?
            } else {
                log.info("getFileIDsFromFastestPillar time out. No GetFileIDsFinalResponse received. return null.");
                return null;
            }
        } catch (InterruptedException e) {
            log.error("getFileIDsFromFastestPillar InterruptedException",e); // TODO handle exception
        }

        return null;
    }

    /**
     * Handles a response to identify pillars to deliver fileIDs.
     * @param msg The IdentifyPillarsForGetFileIDsResponse to handle.
     */
    public void handleIdentifyPillarsForGetFileIDsResponse(IdentifyPillarsForGetFileIDsResponse msg) {
        String correlationID = msg.getCorrelationID();

        // place message in IdentifyPillarsForGetFileIDsResponse
        List<IdentifyPillarsForGetFileIDsResponse> responses =
                identifyPillarsForGetFileIDsResponseMap.get(correlationID);
        if (responses != null) {
            responses.add(msg);
            log.debug("IdentifyPillarsForGetFileIDsResponse from " + msg.getPillarID() + " added to map.");
        } else {
            log.debug("Unknown correlationID: " + correlationID);
            // note if responses null, this is a response to an unknown correlationID, and this client ignores it
        }

        // count down corresponding countDownLatch
        CountDownLatch countDownLatch = identifyResponseCountDownLatchMap.get(correlationID);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public void handleGetFileIDsProgressResponse(GetFileIDsProgressResponse msg) {
        String correlationID = msg.getCorrelationID();

        // place message in getFileIDsProgressResponseMap
        if (getFileIDsProgressResponseCountDownLatchMap.containsKey(correlationID)) {
            getFileIDsProgressResponseMap.put(correlationID, msg);
            log.debug("GetFileIDsProgressResponse from " + msg.getPillarID() + " added to map.");
        } else {
            log.debug("Unknown correlationID: " + correlationID);
        }

        // count down corresponding countDownLatch
        CountDownLatch countDownLatch = getFileIDsProgressResponseCountDownLatchMap.get(correlationID);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public void handleGetFileIDsFinalResponse(GetFileIDsFinalResponse msg) {
        String correlationID = msg.getCorrelationID();

        // place message in getFileIDsProgressResponseMap
        if (getFileIDsFinalResponseCountDownLatchMap.containsKey(correlationID)) {
            getFileIDsFinalResponseMap.put(correlationID, msg);
            log.debug("GetFileIDsFinalResponse from " + msg.getPillarID() + " added to map.");
        } else {
            log.debug("Unknown correlationID: " + correlationID);
        }

        // count down corresponding countDownLatch
        CountDownLatch countDownLatch = getFileIDsFinalResponseCountDownLatchMap.get(correlationID);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    @Override
    public ResultingFileIDs getFileIDsFromFastestPillar(String bitRepositoryCollectionID, FileIDs fileIDs) throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getFileIDsFromFastestPillar(String bitRepositoryCollectionID, FileIDs fileIDs, EventHandler eventHandler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getFileIDsFromFastestPillar(String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl) throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getFileIDsFromFastestPillar(String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl, EventHandler eventHandler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResultingFileIDs getFileIDsFromSpecificPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs) throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getFileIDsFromSpecificPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs, EventHandler eventHandler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getFileIDsFromSpecificPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl) throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void getFileIDsFromSpecificPillar(String pillarID, String bitRepositoryCollectionID, FileIDs fileIDs, URL uploadUrl, EventHandler eventHandler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
