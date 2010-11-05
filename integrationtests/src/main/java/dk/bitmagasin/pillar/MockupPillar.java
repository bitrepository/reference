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
package dk.bitmagasin.pillar;

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

import dk.bitmagasin.common.MockupConf;
import dk.bitmagasin.common.MockupGetDataMessage;
import dk.bitmagasin.common.MockupGetTimeMessage;
import dk.bitmagasin.common.MockupGetTimeReplyMessage;
import dk.bitmagasin.common.MockupMessage;
import dk.bitmagasin.common.MockupSettings;

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
            if (settings.getPillarId() != null && settings.getPillarId().length() > 0 
            		&& !"null".equals(settings.getPillarId())) {
                connection.setClientID(settings.getEnvironmentName() + "_" 
                		+ settings.getPillarId());
            }
            connection.setExceptionListener(this);
            connection.start();

            session = connection.createSession(MockupConf.TRANSACTED, MockupConf.ACKNOWLEDGE_MODE);

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
        	} else {
        		log.info("Message " + msg.getJMSMessageID() + " of type '" 
        				+txtMsg.getJMSType() + "' ignored!");
        	}
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
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

		TextMessage sendMsg = session.createTextMessage(replyMsg.asXML());
		sendMsg.setJMSType("GetTimeReply");

		log.debug("Sending: MockupGetTimeReplyMessage to: " 
				+ replyTo);

		MessageProducer mp = session.createProducer(replyTo);
		mp.send(replyTo, sendMsg);
		if(MockupConf.TRANSACTED) {
			session.commit();
		}
    }
    
    public void visit(MockupGetDataMessage msg, Destination replyTo) {
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
    	
    	// TODO upload to 'token'.
    	// BUT HOW!!!
    }
    
    public void visit(MockupMessage msg, Destination replyTo) {
    	// TODO ??
    	log.warn("Cannot not handle MockupMessage: " + msg.asXML());
    }

    public synchronized void onException(JMSException ex) {
        log.error("JMS Exception occured.  Shutting down client.", ex);
    }
}
