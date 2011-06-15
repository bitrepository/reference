/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol;

import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.List;
import java.util.Map;

/**
 * TestMessageListener
 */
public class TestMessageListener extends AbstractMessageListener
        implements ExceptionListener {

    private Logger log = LoggerFactory.getLogger(TestMessageListener.class);

    private String pillarID;
    private Map<Object, List<Object>> stimuliResponseMap;
    private String testQueue;

    private Object lastMessage;

    /**
     * TestMessageListener constructor sets pillarID and StimuliResponseMap.
     * @param pillarID pillar ID
     * @param StimuliResponseMap Map from request messages to reply messages. When one
     *        request gives rise to both a response and a complete message, the request key
     *        maps to a list with the response first and the complete message second.
     * @param testQueue in the mock up test scenario all communication is via this queue
     *        (should be via the queue specified in the ReplyTo field of the message)
     */
    public TestMessageListener(String pillarID, Map<Object, List<Object>> StimuliResponseMap, String testQueue) {
        log.debug("TestMessageListener: " + pillarID);

        this.pillarID = pillarID;
        stimuliResponseMap = StimuliResponseMap;
        this.testQueue = testQueue;
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        onMessage((Object) message);
        if (stimuliResponseMap.containsKey(message)) {
            sendReply(message, message.getCorrelationID());
        } else {
            for (Object stimuli: stimuliResponseMap.keySet()) {
                // TODO which messages should this test message listener react on?
                if (stimuli instanceof GetFileIDsRequest) {
                    sendReply((GetFileIDsRequest) stimuli, message.getCorrelationID());
                }
            }
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        onMessage((Object) message);
        if (stimuliResponseMap.containsKey(message)) {
            sendReply(message, message.getCorrelationID());
        } else {
            for (Object stimuli: stimuliResponseMap.keySet()) {
                // TODO which messages should this test message listener react on?
                if (stimuli instanceof IdentifyPillarsForGetFileIDsRequest) {
                    sendReply((IdentifyPillarsForGetFileIDsRequest) stimuli, message.getCorrelationID());
                }
            }
        }
    }

    public void onMessage(Object msg) {
        try {
            lastMessage = msg;
            log.debug("TestMessageListener onMessage: " + msg.getClass());
        } catch (Exception e) {
            Assert.fail("Should not throw an exception: ", e);
        }
    }

    private void sendReply(GetFileIDsRequest message, String correlationID) {
        List<Object> replyList = stimuliResponseMap.get(message);
        if (replyList != null && !replyList.isEmpty() && replyList.get(0) instanceof GetFileIDsProgressResponse) {
            GetFileIDsProgressResponse response = (GetFileIDsProgressResponse) replyList.get(0);
            response.setCorrelationID(correlationID);
            response.setTo(testQueue);
            ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(response);
        }
        if (replyList != null && !replyList.isEmpty() && replyList.get(1) instanceof GetFileIDsFinalResponse) {
            GetFileIDsFinalResponse finalResponse = (GetFileIDsFinalResponse) replyList.get(1);
            finalResponse.setCorrelationID(correlationID);
            response.setTo(testQueue);
            ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(finalResponse);
        }
    }

    private void sendReply(IdentifyPillarsForGetFileIDsRequest message, String correlationID) {
        List<Object> replyList = stimuliResponseMap.get(message);
        if (replyList != null && !replyList.isEmpty() &&
                replyList.get(0) instanceof IdentifyPillarsForGetFileIDsResponse) {
            IdentifyPillarsForGetFileIDsResponse response = (IdentifyPillarsForGetFileIDsResponse) replyList.get(0);
            response.setCorrelationID(correlationID);
            response.setTo(testQueue);
            ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(response);
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
        log.info("TestMessageListener.onMessage IdentifyPillarsForGetFileIDsResponse message IGNORE");
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse message) {
        log.info("TestMessageListener.onMessage GetFileIDsResponse message IGNORE");
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse message) {
        log.info("TestMessageListener.onMessage GetFileIDsComplete message IGNORE");
    }


    @Override
    public void onException(JMSException e) {
        e.printStackTrace();
    }
    public String getPillarID() {
        return pillarID;
    }
    public Object getMessage() {
        return lastMessage;
    }
    public Class getMessageClass() {
        return lastMessage.getClass();
    }
}
