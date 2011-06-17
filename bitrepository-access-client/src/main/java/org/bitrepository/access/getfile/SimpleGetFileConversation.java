/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.access.getfile;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bitrepository.access.AccessException;
import org.bitrepository.access.getfile.selectors.PillarSelectorForGetFile;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.bitrepositorycollection.ClientSettings;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.AbstractMessagebusBackedConversation;
import org.bitrepository.protocol.MessageSender;
import org.bitrepository.protocol.flow.UnexpectedResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A conversation for GetFile.
 *
 * Logic for behaving sanely in GetFile conversations.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimpleGetFileConversation extends AbstractMessagebusBackedConversation<File> {
    /** Protocol version used. */
    //TODO These constants should be defined elsewhere!
    public static final long PROTOCOL_VERSION = 1L;
    /** Protocol minimum version used. */
    //TODO These constants should be defined elsewhere!
    public static final long PROTOCOL_MIN_VERSION = 1L;
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The amount of milliseconds to wait before executing the timertask when waiting for identify responses. */
    // TODO replace with configuration.
    private static final Long TIMER_TASK_DELAY = 100000L;

    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The configuration specific to the SLA related to this conversion. */
    private final ClientSettings settings;
    /** The timeout when getting a file. */
    private long getFileTimeout;
    /** The timer. Schedules conversation timeouts for this conversation. */
    private final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The timer task for timeout of identify in this conversation. */
    private final TimerTask identifyTimeoutTask = new IdentifyTimerTask();
    /** The timer task for timeout of getFile in this conversation. */
    private final TimerTask getFileTimeoutTask = new GetFileTimerTask();

    /** Whether the conversation has ended. */
    private boolean ended = false;
    /** The url which the pillar should upload the file to. */
    private final URL uploadUrl;
    /** The ID of the file which should be uploaded to the supplied url */
    private final String fileID;
    /** Selects a pillar based on responses. */
    private final PillarSelectorForGetFile selector;

    /**
     * Initializes the file directory, and the message bus used for sending messages.
     * The fileDir is retrieved from the configuration.
     *
     * @param messageBus The message bus used for sending messages.
     * @param expectedNumberOfPillars The number of pillars to wait for replies from, when identifying pillars.
     * @param getFileDefaultTimeout The timeout when getting a file. If the conversation identifies pillars, this value
     * is replaced by twice the time the pillar estimated.
     * @param fileDir The directory to store retrieved files in.
     */
    public SimpleGetFileConversation(
            MessageSender messageSender, 
            GetFileClientSettings settings,
            PillarSelectorForGetFile selector,
            String fileID,
            URL uploadUrl) {
        super(messageSender, UUID.randomUUID().toString());

        this.settings = settings;
        this.selector = selector;
        this.uploadUrl = uploadUrl;
        this.fileID = fileID;
    }

    @Override
    public boolean isEnded() {
        return ended;
    }

    @Override
    public File getResult() {
        return null;
    }

    /**
     * Sends the message as expected, but also sets up a timer task, that times out if we wait too long for enough
     * replies.
     *
     * @see MessageSender#sendMessage(IdentifyPillarsForGetFileRequest)
     * @param message Message to send.
     */
    @Override
    public void sendMessage(IdentifyPillarsForGetFileRequest message) {
        super.sendMessage(message);
        // add this as a task for the timer.
        timer.schedule(identifyTimeoutTask, TIMER_TASK_DELAY);
    }

    /**
     * Sends the message as expected, but also sets up a timer task, that times out if we wait too long for enough
     * replies.
     *
     * @see MessageSender#sendMessage(GetFileRequest)
     * @param message Message to send.
     */
    @Override
    public void sendMessage(GetFileRequest message) {
        super.sendMessage(message);
        // add this as a task for the timer.
        timer.schedule(getFileTimeoutTask, getFileTimeout);
    }

    /**
     * Handles a reply for identifying the fastest pillar to deliver a given file.
     * Removes the pillar responsible for the response from the outstanding list for the file in the reply. If no more
     * pillars are outstanding, then the file is requested from fastest pillar.
     *
     * @param response The IdentifyPillarsForGetFileResponse to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse response) {
        try {
            selector.processResponse(response);
        } catch (UnexpectedResponseException e) {
            throw new IllegalArgumentException("Invalid IdentifyPillarsForGetFileResponse.");
        }
        
        try {
            if (selector.isFinished()) {
                // stop the timer task for this outstanding instance, and then get the file from the selected pillar
                identifyTimeoutTask.cancel();
                // TODO: Race condition, what if timeout task already triggered this just before now
                getFileFromSelectedPillar();
            }
        } catch (UnableToFinishException e) {
            failConversation("Cannot request file, since no valid pillar could be found", e);
        }

    }

    /**
     * Method for handling the GetFileProgressResponse messages.
     * Currently logs the progress response, but does nothing else.
     *
     * @param msg The GetFileProgressResponse message to be handled by this method.
     */
    @Override
    public void onMessage(GetFileProgressResponse msg) {
        // TODO Something else?
        log.info("Received progress response for retrieval of file " + msg);
    }

    /**
     * Method for final response the get.
     *
     * @param msg The GetFileFinalResponse message to be handled by this method.
     */
    @Override
    public void onMessage(GetFileFinalResponse msg) {
        ArgumentValidator.checkNotNull(msg, "GetFileFinalResponse");
        getFileTimeoutTask.cancel();
        // TODO: Race condition, what if timeout task already triggered this just before now
        endConversation();
    }

    /** Method for making the client send a request for the file to the fastest pillar. */
    private void getFileFromSelectedPillar() {
        if (selector.getPillarID() == null) {
            failConversation("Unable to getFile, no pillar was selected");
        }
        GetFileRequest msg = new GetFileRequest();
        msg.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        msg.setCorrelationID(getConversationID());
        msg.setFileAddress(uploadUrl.toExternalForm());
        msg.setFileID(fileID);
        msg.setPillarID(selector.getPillarID());
        msg.setReplyTo(settings.getClientTopicID());
        msg.setMinVersion(BigInteger.valueOf(PROTOCOL_MIN_VERSION));
        msg.setVersion(BigInteger.valueOf(PROTOCOL_VERSION));
        msg.setTo(selector.getPillarTopic());
        sendMessage(msg);
    }

    @Override
    public void startConversion() {
        IdentifyPillarsForGetFileRequest identifyRequest = new IdentifyPillarsForGetFileRequest();
        identifyRequest.setMinVersion(BigInteger.valueOf(1L));
        identifyRequest.setVersion(BigInteger.valueOf(1L));
        identifyRequest.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        identifyRequest.setFileID(fileID);
        identifyRequest.setReplyTo(settings.getClientTopicID());
        identifyRequest.setTo(settings.getBitRepositoryCollectionTopicID());
        sendMessage(identifyRequest);
    }
    
    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    private void endConversation() {
        ended = true;
        synchronized (this) {
            notifyAll();
        }
        mediator.endConversation(this);
    }


    private void failConversation(String message) {
        failConversation(message, null);
    }
    /**
     * Standard operation for failing the conversation in a standard way
     * @param message Information about why the conversation did fail.
     * @param e Optional exception if this caused the failure.
     */
    // ToDo 
    private void failConversation(String message, Exception e) {
        endConversation();
        // TODO But reporting an actual time to deliver is optional ?!
        throw new AccessException(message, e);
    }
    
    /**
     * The timer task class for the outstanding identify requests. When the time is reached the selected pillar should
     * be called requested for the delivery of the file.
     */
    private class IdentifyTimerTask extends TimerTask {
        @Override
        public void run() {
            //TODO: Exception handler needed, this is a thread
            log.warn("Time has run out for selecting a pillar.");
            getFileFromSelectedPillar();
        }
    }

    /**
     * The timer task class for the outstanding get file requests. When the time is reached the conversation should be
     * marked as ended.
     */
    private class GetFileTimerTask extends TimerTask {
        @Override
        public void run() {
            //TODO: Exception handler needed, this is a thread
            log.debug("Time has run out for getting file from pillar.");
            endConversation();
        }
    }
}
