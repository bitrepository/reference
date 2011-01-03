/*
 * #%L
 * Bitmagasin integritetstest
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bitrepository.common.MockupConf;
import org.bitrepository.common.MockupGetDataCompleteMessage;
import org.bitrepository.common.MockupGetDataMessage;
import org.bitrepository.common.MockupGetDataReplyMessage;
import org.bitrepository.common.MockupGetTimeMessage;
import org.bitrepository.common.MockupGetTimeReplyMessage;
import org.bitrepository.common.MockupHTTPClient;
import org.bitrepository.common.MockupMessage;
import org.bitrepository.common.MockupPutDataMessage;
import org.bitrepository.common.MockupPutDataReplyMessage;
import org.bitrepository.common.MockupSettings;

/**
 * Pillar MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupPillar implements MessageListener, ExceptionListener {
	/** The log for this instance.*/
	private final Log log = LogFactory.getLog(this.getClass());

	private static MockupSettings settings;
    /**
     * The communication session.
     */
    private Session session;
    
    /**
     * The destination of the bus.
     */
    private Topic bus;
    /**
     * The producer to the bus.
     */
    private MessageProducer busProducer;

    public static void main(String[] args) {
    	settings = MockupSettings.getInstance(args);

        MockupPillar mockupPillar = new MockupPillar();
        mockupPillar.run();
    }
    
    protected MockupPillar() {
    	log.info("Starting Pillar: " + settings.getPillarId());
    }

    public void run() {
        try {
            // Create connection
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(MockupConf.user,
                            MockupConf.password, settings.getConnectionUrl());
            Connection connection = connectionFactory.createConnection();
            if (settings.getPillarId() != null 
            		&& settings.getPillarId().length() > 0 
            		&& !"null".equals(settings.getPillarId())) {
                connection.setClientID(settings.getEnvironmentName() + "_" 
                		+ settings.getPillarId());
            }
            connection.setExceptionListener(this);
            connection.start();

            session = connection.createSession(MockupConf.TRANSACTED, 
            		MockupConf.ACKNOWLEDGE_MODE);

            //create messagelistener on SLA topic
            bus = session.createTopic(settings.getSlaTopicId());
            //consumer = session.createDurableSubscriber(topic, MockupConf.pillarId);
            session.createConsumer(bus).setMessageListener(this);
            busProducer = session.createProducer(bus);
            busProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (Exception e) {
        	log.error("Caught exception during run!", e);
        }
    }
    
    public void onMessage(Message msg) {
        try {
    		// report on which message is received
        	log.info("Message: " + msg.getJMSMessageID() + " wit type: "
        			+ msg.getJMSType() + ", on " + msg.getJMSDestination());
        	
    		if(!(msg instanceof TextMessage)) {
            	// Cannot handled non-TextMessage!
            	log.error("ERROR: Not in the format of a "
            			+ "TextMessage: \n" + msg);
        		return;	
    		}
    		
        	TextMessage txtMsg = (TextMessage) msg;
        	log.debug("Received text message: " + txtMsg.getText());
        	
        	// verify getType
        	if(txtMsg.getJMSType() == null || txtMsg.getJMSType().isEmpty()) {
        		// TODO handle better
        		log.error("ERROR: Unhandled message jms type: " 
        				+ txtMsg.getJMSType());
        		return;
        	}
        	
    		// TODO make more of these cases.
        	if(txtMsg.getJMSType().equals("GetTime")) {
        		visit(new MockupGetTimeMessage(txtMsg.getText()), 
        				txtMsg.getJMSReplyTo());
        	} else if(txtMsg.getJMSType().equals("GetData")) {
        		visit(new MockupGetDataMessage(txtMsg.getText()), 
        				txtMsg.getJMSReplyTo());
        	} else if(txtMsg.getJMSType().equals("PutData")) {
        		visit(new MockupPutDataMessage(txtMsg.getText()),
        				txtMsg.getJMSReplyTo());
        	} else {
        		log.info("Message " + msg.getJMSMessageID() + " of type '" 
        				+txtMsg.getJMSType() + "' ignored!");
        	}
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
    
    public void visit(MockupPutDataMessage msg, Destination replyTo) 
            throws Exception {
    	log.info("Received MockupPutDataMessage , with id: "
    			+ msg.getConversationId() + ", and reply to: " + replyTo);
    	
    	File outputFile = new File(settings.getDataDir(), msg.getFileName());
    	if(outputFile.exists()) {
    		// TODO ?
    		log.error("The file already exists! Retrieves new file anyway");
    	}
    	
    	MockupHTTPClient.getData(new FileOutputStream(outputFile), 
    			msg.getToken());
    	log.info("File '" + msg.getFileName() + "' retrieved from token: " 
    			+ msg.getToken());
    	
    	// REPLY
    	MockupPutDataReplyMessage replyMsg = new MockupPutDataReplyMessage(
    			msg.getConversationId(), msg.getFileName(), 
    			settings.getPillarId());
    	
    	sendMessage(replyMsg, replyTo);	
    }

    public void visit(MockupGetTimeMessage msg, Destination replyTo) 
            throws JMSException {
    	log.info("Received MockupGetTimeMessage, with id: " 
    			+ msg.getConversationId() + ", and reply to: "+ replyTo);
    	
    	// validate pillarId
    	List<String> pillarIds = msg.getPillarIds();
    	if(!pillarIds.contains(settings.getPillarId())) {
    		log.info("Is not meant for my ID!");
    		// Do not handle message, which are not meant for us!
    		return;
    	}

		MockupGetTimeReplyMessage replyMsg = new MockupGetTimeReplyMessage(
				msg.getConversationId(), settings.getPillarId());

    	// Insert the times for each dataId requested into the reply.
    	for(String dataId : msg.getDataId()) {
    		log.debug("Sending reply for data instance: " + dataId);
    		// TODO retrieve the specific times for each dataId. 
    		// workaround: use default values!
    		replyMsg.addTimeForDataId(dataId, settings.getTimeoutMeasure(), 
    				settings.getTimeoutUnit());
    	}
    	
		if(settings.getErrorCode() != 0) {
			log.info("Inserting error into reply message: '" 
					+ settings.getErrorCode() + " : " 
					+ settings.getErrorMessage() + "'");
			replyMsg.addError(settings.getErrorCode(), 
					settings.getErrorMessage());
		}

		sendMessage(replyMsg, replyTo);
    }
    
    public void visit(MockupGetDataMessage msg, Destination replyTo) 
            throws JMSException {
    	// Check whether it is for me!
    	log.info("Received MockupGetDataMessage, with id: " 
    			+ msg.getConversationId() + ", and reply to: "+ replyTo);
    	
    	// validate pillarId
    	if(!msg.getPillarId().equals(settings.getPillarId())) {
    		log.info("Is not meant for my ID!");
    		// Do not handle message, which are not meant for us!
    		return;
    	}
    	
    	log.info("Sending data '" + msg.getDataId() + "' to token '" 
    			+ msg.getToken() + "'");
    	
    	// reply, that data is being found!
    	MockupGetDataReplyMessage replyMsg 
    	        = new MockupGetDataReplyMessage(msg.getConversationId(), 
    	        		msg.getDataId());
    	
    	sendMessage(replyMsg, replyTo);
    	
    	// start uploading data
    	try {
			File fil = getFile(msg.getDataId());
			if(fil == null) {
				throw new NullPointerException("No file for '" 
						+ msg.getDataId() + "' found.");
			}
    		// handle http!
    		if(msg.getToken().startsWith("http://")) {
    			MockupHTTPClient.putData(new FileInputStream(fil), 
    					new URL(msg.getToken()));
    		} else {
    			throw new IllegalArgumentException("Cannot handle token: '" 
    					+ msg.getToken() + "'");
    		}
    	} catch (Exception e) {
    		log.error("Unexpected error while tranferring data.", e);
    		// TODO send an alarm!
    		return;
    	}
    	
    	// TODO send a reply
    	MockupGetDataCompleteMessage completeMsg 
    	        = new MockupGetDataCompleteMessage(msg.getConversationId(),
    	        		msg.getDataId(), msg.getToken());

    	sendMessage(completeMsg, replyTo);
    }
    
    public void visit(MockupMessage msg, Destination replyTo) {
    	// TODO ??
    	log.warn("Cannot not handle MockupMessage: " + msg.asXML());
    }
    
    private void sendMessage(MockupMessage msg, Destination replyTo) 
            throws JMSException {
    	TextMessage sendMsg = session.createTextMessage(msg.asXML());
    	sendMsg.setJMSType(msg.getOperationId());

    	log.info("Sending: MockupGetDataCompleteMessage to: " 
    			+ replyTo);
    	MessageProducer mp = session.createProducer(replyTo);
    	mp.send(replyTo, sendMsg);
    	if(MockupConf.TRANSACTED) {
    		session.commit();
    	}
    }
    
    protected File getFile(String dataId) {
    	// find data!
    	File res = new File(settings.getDataDir(), dataId);
		if(res.isFile() && res.canRead()) {
			return res;
		}
    	
//		log.warn("Could not find data '" + dataId + "' in directory.");
//    	if(dataId.equals(settings.getDataId())) {
//    		// TODO return other than random file!
//    		res = new File("pom.xml");
//    		if(res.isFile() && res.canRead()) {
//    			return res;
//    		}
//    	} 
    	// data not found. 
    	log.error("Could not find data with id '" + dataId 
    			+ "'. A null is being returned.");
    	return null;
    }

    public synchronized void onException(JMSException ex) {
        log.error("JMS Exception occured.  Shutting down client.", ex);
    }
}
