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

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access_client.configuration.AccessConfiguration;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Prototype GetFileIDs Client
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

    // TODO BItRepositoryMessages.xsd IdentifyPillarsForGetFileIDsRequest element FileIDs
    // uniquely identifies the data in given SLA which the file ids are requested for
    // Given in order to be able to give possible estimate of time to deliver
    // I think it should be possible to give a FileIDs list or address to this method
    @Override
    public List<IdentifyPillarsForGetFileIDsResponse> identifyPillarsForGetFileIDs(String slaID) {
        // create identifyPillarsForGetFileIDsRequest message
        String corrID = UUID.randomUUID().toString();
        log.debug("identifyPillarsForGetFileIDs new corrID " + corrID);
        IdentifyPillarsForGetFileIDsRequest identifyRequest = GetFileIDsClientMessageFactory.
                getIdentifyPillarsForGetFileIDsRequestMessage(corrID, slaID, queue, null);

        // put correlationId and new CountDownLatch in identifyResponseCountDownLatchMap
        CountDownLatch countDownLatch = new CountDownLatch(numberOfPillars);
        identifyResponseCountDownLatchMap.put(corrID, countDownLatch);
        // put correlationId and new empty List in identifyPillarsForGetFileIDsResponseMap
        List<IdentifyPillarsForGetFileIDsResponse> responses = new ArrayList<IdentifyPillarsForGetFileIDsResponse>();
        identifyPillarsForGetFileIDsResponseMap.put(corrID, responses);

        // send message
        messageBus.sendMessage(queue, identifyRequest);

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

    // TODO BItRepositoryMessages.xsd GetFileIDsRequest element resultAddress
    // we should be able to give this as a parameter
    // TODO why do we return a File from this method?
    // (the answer is probably that the answer can be given in a file using file exchange and resultAddress)
    @Override
    public File getFileIDs(String correlationID, String slaID, String queue, String pillarID) {
        if (correlationID == null) {
            correlationID = UUID.randomUUID().toString();
            log.debug("getFileIDs new correlationID " + correlationID);
        }
        // create getFileIdsRequest
        GetFileIDsRequest request = GetFileIDsClientMessageFactory.
                getGetFileIDsRequestMessage(correlationID, slaID, queue, pillarID, null, null);

        // put correlationID and new CountDownLatches in appropriate Maps
        CountDownLatch progressResponseCountDown = new CountDownLatch(1);
        // note there could be multiple progress responses, but we only wait for one...
        getFileIDsProgressResponseCountDownLatchMap.put(correlationID, progressResponseCountDown);
        CountDownLatch finalResponseCountDown = new CountDownLatch(1);
        getFileIDsFinalResponseCountDownLatchMap.put(correlationID, finalResponseCountDown);

        // send message
        messageBus.sendMessage(queue, request);

        // wait for messages (or until specified waiting time elapses)
        try {
            boolean responseOk = progressResponseCountDown.await(timeOut, TimeUnit.MILLISECONDS);
            if (responseOk) {
                GetFileIDsProgressResponse response = getFileIDsProgressResponseMap.get(correlationID);
                // TODO how do we want to use the response?
                // if ProgressResponseInfo held info on expected remaining time, it could maybe be useful...
            } else {
                log.info("getFileIDs time out. No GetFileIDsProgressResponse received.");
                // TODO we did not receive a response before time out - it may still come or a complete message may come
            }
        } catch (InterruptedException e) {
            log.error("getFileIDs InterruptedException",e); // TODO handle exception
        }

        File file = new File("fileAddress");

        try {
            boolean completeOk = finalResponseCountDown.await(timeOut, TimeUnit.MILLISECONDS);
            if (completeOk) {
                GetFileIDsFinalResponse finalResponse = getFileIDsFinalResponseMap.get(correlationID);
                ResultingFileIDs result = finalResponse.getResultingFileIDs();
                JAXBContext jaxbContext = JAXBContext.newInstance("org.bitrepository.bitrepositoryelements");
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(result, new FileOutputStream(file));
                // TODO use other parts of complete message ?
            } else {
                log.info("getFileIDs time out. No GetFileIDsFinalResponse received. return null.");
                return null;
            }
        } catch (InterruptedException e) {
            log.error("getFileIDs InterruptedException",e); // TODO handle exception
        } catch (FileNotFoundException e) {
            log.error("getFileIDs FileNotFoundException",e); // TODO handle exception
        } catch (JAXBException e) {
            log.error("getFileIDs JAXBException",e); // TODO handle exception
        }

        return file;
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
}
