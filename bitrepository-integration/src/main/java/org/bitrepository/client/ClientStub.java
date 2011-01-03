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
package org.bitrepository.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bitrepository.common.DataTime;
import org.bitrepository.common.MockupConf;
import org.bitrepository.common.MockupGetDataMessage;
import org.bitrepository.common.MockupGetTimeMessage;
import org.bitrepository.common.MockupGetTimeReplyMessage;
import org.bitrepository.common.MockupSettings;
import org.bitrepository.common.TimeUnits;

public class ClientStub implements MessageListener, ExceptionListener {
	private final Log log = LogFactory.getLog(this.getClass());
    private MessageProducer messageProducer;

    private Topic topic;
    private Queue queue;
    
    private static MockupSettings settings;
    
    private Map<String, List<String>> missingGetTime 
            = Collections.synchronizedMap(new HashMap<String, List<String>>());
    private Map<String, DataRequestTime> getTimes = Collections.synchronizedMap(
    		new HashMap<String, DataRequestTime>());

    public ClientStub() {
    	settings = MockupSettings.getInstance(); 
    	log.info("Starting ClientStub: " + settings.getClientId() + 
    			", with queue: " + settings.getQueue());
    	settings = MockupSettings.getInstance();    		
    	createConnection();
    }

    public void createConnection() {
        Connection connection = null;
        try {
            // Create connection
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(MockupConf.user,
                            MockupConf.password, settings.getConnectionUrl());
            connection = connectionFactory.createConnection();
            if (settings.getClientId() != null 
            		&& settings.getClientId().length() > 0 
            		&& !"null".equals(settings.getClientId())) {
                connection.setClientID(settings.getEnvironmentName() + "_" 
                		+ settings.getClientId());
            }
            connection.setExceptionListener(this);
            connection.start();

//            session = connection.createSession(MockupConf.TRANSACTED, 
//            		MockupConf.ACKNOWLEDGE_MODE);
//
//            topic = session.createTopic(settings.getSlaTopicId());
//            session.createConsumer(topic).setMessageListener(this);
//            
//            queue = session.createQueue(settings.getQueue());
//            session.createConsumer(queue).setMessageListener(this);
//            
//            messageProducer = session.createProducer(topic);
//            messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
//            MessageProducer queueProducer = session.createProducer(queue);
//            queueProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

        } catch (Exception e) {
            log.error("Caught exception during initialization.", e);
        } 
    }
    
    protected void sendGetTime(Session session, MessageProducer producer, 
    		String... pillarIds) throws Exception {
        //TODO multiple gets
        String commId = "CommId28";//TODO communication ID generation

        List<String> pillars = new ArrayList<String>(2);
        for(String pillarId : pillarIds) {
        	pillars.add(pillarId);
        }
        
        // set the retrieval of time for 'dataId' to outstanding for 'pillars'.
        missingGetTime.put(settings.getDataId(), pillars);

        MockupGetTimeMessage msg = new MockupGetTimeMessage(settings.getDataId(), 
        		pillars.toArray(new String[pillars.size()]));
        msg.addConversationId(commId);
        
        TextMessage message = session.createTextMessage(msg.asXML());
        message.setJMSType("GetTime");
        message.setJMSReplyTo(queue);
        
        log.info("Sending message to bus: " + message.getText());
        producer.send(topic, message);

        if (MockupConf.TRANSACTED) {
            session.commit();
        }
    }
    
    public void sendGetData(String pillarId) throws Exception {
        //TODO multiple gets
        String commId = "CommId29";//TODO communication ID generation

        MockupGetDataMessage msg = new MockupGetDataMessage(settings.getDataId(), pillarId, 
        		settings.getToken());
        msg.addConversationId(commId);
        
//        TextMessage message = session.createTextMessage(msg.asXML());
//        message.setJMSType("GetData");
//        message.setJMSReplyTo(queue);
//        
//        log.info("Sending message to bus: " + message.getText());
//        messageProducer.send(topic, message);
//
//        if (MockupConf.TRANSACTED) {
//            session.commit();
//        }
    }

    public void onMessage(Message msg) { 
    	try {
        	log.debug("Received message: " + msg.getJMSType() + ", on " 
        			+ msg.getJMSDestination());
        	
    		if(!(msg instanceof TextMessage)) {
            	// TODO throw exception?
            	log.error("ERROR: Not in the format of a "
            			+ "TextMessage: \n" + msg);
        		return;	
    		}
    		
        	TextMessage txtMsg = (TextMessage) msg;
        	log.debug(txtMsg.getText());

        	if(txtMsg.getJMSType() == null || txtMsg.getJMSType().isEmpty()) {
        		// TODO throw exception?
        		log.error("ERROR: Unhandled message jms type: " 
        				+ txtMsg.getJMSType());
        		return;
        	}
        	
    		// TODO make more of these cases. E.g. handle more message-types.
        	if(txtMsg.getJMSType().equals("GetTimeReply")) {
        		visit(new MockupGetTimeReplyMessage(txtMsg.getText()), 
        				txtMsg.getJMSReplyTo());
        	} else {
        		log.debug("Cannot handle jms type: " + txtMsg.getJMSType() 
        				+ ", message ignored!");
        	}
    	} catch (Exception e) {
    		log.error("Caught exception during handling of message: " + msg, e);
    	}
    }
    
    public synchronized void visit(MockupGetTimeReplyMessage msg, 
    		Destination replyTo) {
    	String pillarId = msg.getPillarId();
    	
    	// check if we are awaiting the message.
    	List<String> pillars = missingGetTime.get(settings.getDataId());
    	if(pillars == null) {
    		// do not handle message, when we are not awaiting dataId.
    		log.debug("The message was not intended for me. Unknown dataId \n" 
    				+ msg.asXML());
    		return;
    	}
    	if(!pillars.contains(pillarId)) {
    		// do not handle message, when we are not awaiting pillarId
    		log.debug("The message was not intended for me. Unknown pillarId "
    				+ "\n" + msg.asXML());
    		return;
    	}

    	if(msg.getErrorCode() != 0) {
    		log.warn("Received error message from '" + pillarId + "': '"
    				+ msg.getErrorCode() + " : " + msg.getErrorMessage() 
    				+ "'. Trying to proceed anyway!");
    	}

    	pillars.remove(pillarId);
    	
    	List<DataTime> replies = msg.getDataTimes();
    	for(DataTime dataTime : replies) {
        	log.debug("Pillar '" + pillarId + "' can deliver in: " 
        			+ dataTime.timeMeasure + " " + dataTime.timeUnit);

        	DataRequestTime drt = getTimes.get(dataTime.dataId);
        	if(drt == null) {
        		drt = new DataRequestTime(dataTime.dataId);
        	}
        	drt.addEntry(pillarId, dataTime.timeMeasure, dataTime.timeUnit);
    	}
    	
    	// no more missing pillars, then remove entry from 'missingGetTime',
    	// and continue with next action! Otherwise: await the remaining 
    	// pillars.
    	if(pillars.isEmpty()) {
    		log.info("Retrieved time for data '" + settings.getDataId() + "' from pillar '"
    				+ pillarId + "'. No more outstanding pillars.");
    		missingGetTime.remove(settings.getDataId());    		
    	} else {
    		log.info("Retrieved time for data '" + settings.getDataId() + "' from pillar '" 
    				+ pillarId + "', outstanding pillars: " + pillars);
    		missingGetTime.put(settings.getDataId(), pillars);
    	}
    }
    
    /**
     * Helper class for keeping track of the answers when requesting the time
     * for some data.
     * @author jolf
     */
    protected class DataRequestTime {
    	/** The id for the data which has been requested. */
    	String dataId;
    	/** The date for the time of the creation of this instance. */
    	Date date;
    	/** The time it takes a pillar to put the data to the location.*/
    	Map<String, Long> pillarTime;
    	
    	/**
    	 * The Constructor.
    	 * @param dataId The id for the data requested. 
    	 */
    	public DataRequestTime(String dataId) {
    		this.dataId = dataId;
    		pillarTime = Collections.synchronizedMap(
    				new HashMap<String, Long>());
    		this.date = new Date();
    	}
    	
    	/**
    	 * Adds the time for the pillar to map. 
    	 * 
    	 * @param pillarId The ID for the pillar.
    	 * @param timeMeasure The amount of time it takes.
    	 * @param timeUnit The unit for the measure of time.
    	 */
    	public void addEntry(String pillarId, long timeMeasure, 
    			TimeUnits timeUnit) {
    		// calculate time
    		pillarTime.put(pillarId, timeMeasure 
    				* TimeUnits.getTimeInSeconds(timeUnit));
    	}
    	
    	/**
    	 * Retrieves the date for the creation of this instance.
    	 * @return The date for the creation of this instance.
    	 */
    	public Date getDate() {
    		return date;
    	}
    }

	public void send(Object createMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onException(JMSException arg0) {
		// TODO Auto-generated method stub
		
	};
}
