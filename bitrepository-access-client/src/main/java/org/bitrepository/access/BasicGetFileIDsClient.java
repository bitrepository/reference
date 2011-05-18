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

    private Logger log = LoggerFactory.getLogger(BasicGetFileIDsClient.class);

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

    /** TODO The correlationIDs should be generated such that we avoid id conflicts. */
    private int correlationIDcounter;

    /** Map from correlationID to CountDownLatch counting down the identify responses we are waiting for. */
    private Map<String, CountDownLatch> identifyResponseCountDownLatchMap;
    /** Map from correlationID to List of IdentifyPillarsForGetFileIDsResponses. */
    private Map<String, List<IdentifyPillarsForGetFileIDsResponse>> identifyPillarsForGetFileIDsResponseMap;

    /** Map from correlationID to CountDownLatch counting down the responses we are waiting for. */
    private Map<String, CountDownLatch> getFileIDsResponseCountDownLatchMap;
    /** Map from correlationID to List of GetFileIDsResponses. */
    private Map<String, GetFileIDsResponse> getFileIDsResponseMap;

    /** Map from correlationID to CountDownLatch counting down the complete messages we are waiting for. */
    private Map<String, CountDownLatch> getFileIDsCompleteCountDownLatchMap;
    /** Map from correlationID to List of GetFileIDsComplete messages. */
    private Map<String, GetFileIDsComplete> getFileIDsCompleteMap;

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

        // initialise correlationIDcounter and Maps
        correlationIDcounter = 786543;
        identifyResponseCountDownLatchMap = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
        identifyPillarsForGetFileIDsResponseMap =
                Collections.synchronizedMap(new HashMap<String, List<IdentifyPillarsForGetFileIDsResponse>>());
        getFileIDsResponseCountDownLatchMap = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
        getFileIDsResponseMap = Collections.synchronizedMap(new HashMap<String, GetFileIDsResponse>());
        getFileIDsCompleteCountDownLatchMap = Collections.synchronizedMap(new HashMap<String, CountDownLatch>());
        getFileIDsCompleteMap = Collections.synchronizedMap(new HashMap<String, GetFileIDsComplete>());
    }

    // TODO BItRepositoryMessages.xsd IdentifyPillarsForGetFileIDsRequest element FileIDs
    // uniquely identifies the data in given SLA which the file ids are requested for
    // Given in order to be able to give possible estimate of time to deliver
    // I think it should be possible to give a FileIDs list or address to this method
    @Override
    public List<IdentifyPillarsForGetFileIDsResponse> identifyPillarsForGetFileIDs(String slaID) {
        // create identifyPillarsForGetFileIDsRequest message
        String corrID = "ipfgfir" + correlationIDcounter;
        correlationIDcounter++;
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

        return responses;
    }

    // TODO BItRepositoryMessages.xsd GetFileIDsRequest element resultAddress
    // we should be able to give this as a parameter
    // TODO why do we return a File from this method?
    // (the answer is probably that the answer can be given in a file using file exchange and resultAddress)
    @Override
    public File getFileIDs(String slaID, String queue, String pillarID) {
        // create getFileIdsRequest
        String corrID = "gfir" + correlationIDcounter;
        correlationIDcounter++;
        GetFileIDsRequest request = GetFileIDsClientMessageFactory.
                getGetFileIDsRequestMessage(corrID, slaID, queue, pillarID, null, null);

        // put correlationID and new CountDownLatches in appropriate Maps
        CountDownLatch responseCountDown = new CountDownLatch(1);
        getFileIDsResponseCountDownLatchMap.put(corrID, responseCountDown);
        CountDownLatch completeCountDown = new CountDownLatch(1);
        getFileIDsCompleteCountDownLatchMap.put(corrID, completeCountDown);

        // send message
        messageBus.sendMessage(queue, request);

        // wait for messages (or until specified waiting time elapses)
        try {
            boolean responseOk = responseCountDown.await(timeOut, TimeUnit.MILLISECONDS);
            if (responseOk) {
                GetFileIDsResponse response = getFileIDsResponseMap.get(corrID);
                // TODO how do we want to use the response?
            } else {
                // TODO we did not receive a response before time out - it may still come or a complete message may come
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  // TODO handle exception
        }

        File file = new File("fileAddress");

        try {
            boolean completeOk = completeCountDown.await(timeOut, TimeUnit.MILLISECONDS);
            if (completeOk) {
                GetFileIDsComplete completeMsg = getFileIDsCompleteMap.get(corrID);
                ResultingFileIDs result = completeMsg.getResultingFileIDs();
                JAXBContext jaxbContext = JAXBContext.newInstance("org.bitrepository.bitrepositoryelements");
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(result, new FileOutputStream(file));
                // TODO use other parts of complete message ?
            } else {
                // TODO we did not receive a complete message - throw exception or?
                file = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace(); // TODO handle exception
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // TODO handle exception
        } catch (JAXBException e) {
            e.printStackTrace(); // TODO handle exception
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
        }
        // note if responses null, this is a response to an unknown correlationID, and this client ignores it

        // count down corresponding countDownLatch
        CountDownLatch countDownLatch = identifyResponseCountDownLatchMap.get(correlationID);
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public void handleGetFileIDsResponse(GetFileIDsResponse msg) {

        // TODO

    }

    public void handleGetFileIDsComplete(GetFileIDsComplete msg) {

        // TODO

    }
}
