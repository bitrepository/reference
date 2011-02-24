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
package org.bitrepository.access;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.protocol.Message;
import org.bitrepository.protocol.MessageFactory;
import org.bitrepository.protocol.MessageListener;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetClient'.
 * @author jolf
 */
public class GetClientTest extends ExtendedTestCase {
    
    private static int WAITING_TIME_FOR_MESSAGE = 1000;

    @Test(groups = {"regressiontest"})
    public void retrieveFileFastestTest() throws Exception {
        addDescription("Tests whether a specific message is sent by the GetClient");
        String dataId = "dataId1";
        String slaId = "THE-SLA";
        String pillarId = "THE-ONLY-PILLAR";
        GetClient gc = new GetClient();
        TestMessageListener listener = new TestMessageListener();
        ProtocolComponentFactory.getInstance().getMessageBus().addListener(gc.queue, listener);
        
        addStep("Request the fastest delivery of file " + dataId, 
                "The GetClient should send a IdentifyPillarsForGetFileRequest "
                + "message.");
        gc.getFileFastest(dataId, slaId, pillarId);

        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        addStep("Ensure that the IdentifyPillarsForGetFileRequest message has "
                + "been caught.", "It should be valid.");
        Assert.assertNotNull(listener.getMessage(), "The message must not be null.");
        Assert.assertEquals(listener.getMessageClass().getName(), 
                IdentifyPillarsForGetFileRequest.class.getName(), 
                "The message should be of the type '" 
                + IdentifyPillarsForGetFileRequest.class.getName() + "'");
        IdentifyPillarsForGetFileRequest identifyMessage = MessageFactory.createMessage(
                IdentifyPillarsForGetFileRequest.class, listener.getMessage());
        Assert.assertEquals(identifyMessage.getFileID(), dataId);
        
        addStep("Sending a reply for the message.", "Should be handled by the "
                + "GetClient.");
        IdentifyPillarsForGetFileReply reply = new IdentifyPillarsForGetFileReply();
        reply.setCorrelationID(identifyMessage.getCorrelationID());
        reply.setFileID(identifyMessage.getFileID());
        reply.setMinVersion((short) 1);
        reply.setPillarID(pillarId);
        // reply.setReplyTo(value)
        reply.setSlaID(identifyMessage.getSlaID());
        reply.setTimeToDeliver("1000");
        reply.setVersion((short) 1);

        ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(
                gc.queue, reply);
        
        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        addStep("Verifies whether the GetClient sends a request for the file.", 
                "Should be a GetFileRequest for the pillar.");
        Assert.assertEquals(GetFileRequest.class, listener.getMessageClass(), 
                "The last message should be a GetFileRequest");
        GetFileRequest getMessage = MessageFactory.createMessage(
                GetFileRequest.class, listener.getMessage());
        Assert.assertEquals(getMessage.getFileID(), dataId);
        Assert.assertEquals(getMessage.getPillarID(), pillarId);
        
        addStep("Upload a file to the given destination, send a complete to "
                + "the GetClient.", "The GetClient should download the file."); 
//        GetFileResponse getReply = new GetFileResponse();
//        getReply.setMinVersion((short) 1);
//        getReply.setVersion((short) 1);
//        getReply.setPillarID(pillarId);
//        getReply.setFileID(dataId);
//
//        ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(
//                gc.queue, getReply);
//        
//        synchronized(this) {
//            try {
//                wait(WAITING_TIME_FOR_MESSAGE);
//            } catch (Exception e) {
//                // print, but ignore!
//                e.printStackTrace();
//            }
//        }
//        
//        
//        System.out.println("Received message: " + listener.getMessageClass() 
//                + " , " + listener.getMessage());
    }
    
    /**
     * 
     * @author jolf
     */
    @SuppressWarnings("rawtypes")
    protected class TestMessageListener implements MessageListener, 
            ExceptionListener {
        private String message = null;
        private Class messageClass = null;
        @Override
        public void onMessage(Message msg) {
            try {
                message = msg.getText();
                messageClass = msg.getMessageType();
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }
        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
        public String getMessage() {
            return message;
        }
        public Class getMessageClass() {
            return messageClass;
        }
    }
}
