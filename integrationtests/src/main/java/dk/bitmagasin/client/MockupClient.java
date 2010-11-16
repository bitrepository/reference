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
package dk.bitmagasin.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
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

import dk.bitmagasin.common.DataTime;
import dk.bitmagasin.common.MockupConf;
import dk.bitmagasin.common.MockupGetDataCompleteMessage;
import dk.bitmagasin.common.MockupGetDataMessage;
import dk.bitmagasin.common.MockupGetDataReplyMessage;
import dk.bitmagasin.common.MockupGetTimeMessage;
import dk.bitmagasin.common.MockupGetTimeReplyMessage;
import dk.bitmagasin.common.MockupHTTPClient;
import dk.bitmagasin.common.MockupMessage;
import dk.bitmagasin.common.MockupPutDataMessage;
import dk.bitmagasin.common.MockupPutDataReplyMessage;
import dk.bitmagasin.common.MockupSettings;
import dk.bitmagasin.common.TimeUnits;

/**
 * AccessClient MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupClient implements MessageListener, ExceptionListener {
	private final Log log = LogFactory.getLog(this.getClass());

    private Session session;
    private MessageProducer messageProducer;

    private Topic bus;
    private Queue queue;
    
    private static List<String> actions = new ArrayList<String>();
    private static MockupSettings settings;
    
    private int communicationId = 1;
    
    private Map<String, List<String>> missingGetTime 
            = Collections.synchronizedMap(new HashMap<String, List<String>>());
    private Map<String, DataRequestTime> getTimes = Collections.synchronizedMap(
    		new HashMap<String, DataRequestTime>());
    private Map<String, List<String>> missingPut
            = Collections.synchronizedMap(new HashMap<String, List<String>>());

    /**
     * The settings are defined in the arguments.
     * @param args
     */
    public static void main(String... args) {
    	settings = MockupSettings.getInstance(args);

    	// retrieve actions
    	for(String arg : args) {
    		if(arg.startsWith("action=")) {
    			actions.add(arg.replace("action=", ""));
    		}
    	}
    	
    	if(actions.isEmpty()) {
    		actions.add("getTime->KB,SB");
    	}
    	
        MockupClient mockupClient = new MockupClient();
        mockupClient.run();
    }
    
    protected MockupClient() {
    	log.info("Starting MockupClient: " + settings.getClientId() + ", with queue: " 
    			+ settings.getQueue());
    }

    public void run() {
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

            // Create session
            session = connection.createSession(MockupConf.TRANSACTED, 
            		MockupConf.ACKNOWLEDGE_MODE);

            //create messagelistener on SLA topic
            bus = session.createTopic(settings.getSlaTopicId());
            //messageConsumer = session.createDurableSubscriber(topic, clientId);
            session.createConsumer(bus).setMessageListener(this);
            
            // create messagelistener on the unique client queue.
            queue = session.createQueue(settings.getQueue());
            session.createConsumer(queue).setMessageListener(this);
            
            // Create producer with PillarMockUp Queue destination
            messageProducer = session.createProducer(bus);
            messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MessageProducer queueProducer = session.createProducer(queue);
            queueProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

        } catch (Exception e) {
            log.error("Caught exception during initialization.", e);
        } 
    	nextAction();
    }
    
    protected void nextAction() {
    	// only perform more actions, if more actions exists.
    	if(actions.size() <= 0) {
    		log.info("No more actions to perform. System exiting!");
    		System.exit(0);
    	}
    	
		// retrieve next command and remove it from the list.
		String action = actions.remove(0);
		log.info("Handling action: " + action + ", with the following "
				+ "remaining:" + actions);
		
		// handle the action
    	try {
    		handleAction(action);
        } catch (Exception e) {
            log.error("Caught exception during run.", e);
        } 
    }
    
    protected void handleAction(String action) throws Exception {
    	// ensure that the action has the right format.
		String[] split = action.split("->");
		if(split.length < 2) {
			log.warn("Action is not parsable. Needs "
					+ "'command'->'arguments', but was:" + action);
			nextAction();
		}

		// retrieve the command and the arguments.
		String command = split[0];
		
		if(command.equalsIgnoreCase("getTime")) {
			String[] pillars = split[1].split("[,]");
			if(split.length == 2) {
				// if only one '->' then 'GetTime' on default dataId
				sendGetTime(settings.getDataId(), pillars);
			} else {
				// use other '->' argument as dataId.
				sendGetTime(split[2], pillars);
			}
		} else if(command.equalsIgnoreCase("getData")) {
			String[] args = split[1].split("[,]");
			if(args.length < 1) {
				log.error("Bad arguments for 'GetData' : " + action);
				nextAction();
			} else if(args.length == 1) {
				// if no 'data id' then use default.
				log.info("Sending get for default dataId: " 
						+ settings.getDataId());
				sendGetData(args[0], settings.getDataId());
			} else {
				sendGetData(args[0], args[1]);
			}
		} else if(command.equalsIgnoreCase("getFastest")) {
			String dataId = split[1];
			// if(
			if(getTimes.containsKey(dataId)){
				getDataFastest(split[1]);
			} else {
				// run a 'GetTime' before retrieving the data.
				actions.add("GetTime->" + settings.getPillarId() 
						+ "->" + dataId);
				actions.add(action);
				nextAction();
			}
		} else if(command.equalsIgnoreCase("PutData")) {
			String[] pillars = split[1].split("[,]");
			if(split.length == 2) {
				// if no 'data id' then use default.
				log.info("Sending put for data default dataId: " 
						+ settings.getDataId());
				putData(settings.getDataId(), pillars);
			} else {
				putData(split[2], pillars);
			}
		} else {
			log.error("Does not know command: " + command);
		}
    }
    
    protected void putData(String filepath, String... pillars) 
            throws Exception {
        String commId = "CommId" + getNextCommunicationId();
        
    	File file = new File(settings.getDataDir(), filepath);
    	if(!file.isFile() || !file.canRead()) {
    		throw new IllegalArgumentException("Bad file: '" 
    				+ file.getAbsolutePath() + "'");
    	}
    	
    	log.info("Uploading file: '" + file.getName() + "'");
    	URL url = MockupHTTPClient.putData(new FileInputStream(file), 
    			file.getName());
    	log.info("File '" + file.getAbsolutePath() + "' uploaded to '" 
    			+ url.toExternalForm() + "'");
    	
    	MockupPutDataMessage msg = new MockupPutDataMessage(commId, url, 
    			file.getName(), pillars);
    	
    	List<String> pillarIds = new ArrayList<String>();
    	Collections.addAll(pillarIds, pillars);
    	missingPut.put(file.getName(), pillarIds);
    	
    	sendMessage(msg);
    }

    protected void sendGetTime(String dataId, String... pillarIds) 
            throws Exception {
        String commId = "CommId" + getNextCommunicationId();

        List<String> pillars = new ArrayList<String>(2);
        for(String pillarId : pillarIds) {
        	pillars.add(pillarId);
        }

        MockupGetTimeMessage msg = new MockupGetTimeMessage(dataId, 
        		pillars.toArray(new String[pillars.size()]));
        msg.addConversationId(commId);

        // set the retrieval of time for 'dataId' to outstanding for 'pillars'.
        missingGetTime.put(commId, pillars);
        
        sendMessage(msg);
    }
    
    /**
     * Sends a request for a specific dataId from the pillar with the lowest
     * delivery time.
     * 
     * @param session 
     * @param producer
     * @param dataId
     * @throws Exception
     */
    public void getDataFastest(String dataId) throws Exception {
    	log.info("Retrieving '" + dataId + "' from: " + getTimes);
    	DataRequestTime drt = getTimes.get(dataId);
    	if(drt == null) {
    		log.error("The time for data '" + dataId + "' has not yet "
    				+ "been found");
    		nextAction();
    		return;
    	}
    	String pillarId = drt.getFastest();
    	log.info("Retrieving data '" + dataId + "' from pillar '" + pillarId 
    			+ "'");
    	sendGetData(pillarId, dataId);
    }
    
    /**
     * Sends the 'GetData' from a specific pillar. 
     * 
     * @param session
     * @param producer
     * @param dataId
     * @param pillarId
     * @throws Exception
     */
    public void sendGetData(String pillarId, String dataId) throws Exception {
        //TODO multiple gets
        String commId = "CommId" + getNextCommunicationId().toString();

        MockupGetDataMessage msg = new MockupGetDataMessage(
        		dataId, pillarId, MockupHTTPClient.getURL(dataId).toString());
        msg.addConversationId(commId);

        sendMessage(msg);
    }

    public void onMessage(Message msg) { 
    	try {
    		// report on which message is received
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

        	// verify getType
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
        	} else if(txtMsg.getJMSType().equals("GetDataReply")) {
        		visit(new MockupGetDataReplyMessage(txtMsg.getText()),
        				txtMsg.getJMSReplyTo());
        	} else if(txtMsg.getJMSType().equals("GetDataComplete")) {
        		visit(new MockupGetDataCompleteMessage(txtMsg.getText()),
        				txtMsg.getJMSReplyTo());
        	} else if(txtMsg.getJMSType().equals("PutDataReply")) {
        		visit(new MockupPutDataReplyMessage(txtMsg.getText()),
        				txtMsg.getJMSReplyTo());
        	} else {
        		log.info("Cannot handle jms type: " + txtMsg.getJMSType() 
        				+ ", message ignored!");
        	}
    	} catch (Exception e) {
    		log.error("Caught exception during handling of message: " + msg, e);
    	}
    }
    
    public synchronized void visit(MockupPutDataReplyMessage msg, 
    		Destination replyTo) throws Exception {
    	log.info("Received MockupPutDataReplyMessage from '" 
    			+ msg.getPillarId() + "' for file '" + msg.getFileName() + "'");
    	
    	// check if we are awaiting the message.
    	List<String> pillars = missingPut.get(msg.getFileName());
    	if(pillars == null) {
    		// do not handle message, when we are not awaiting dataId.
    		log.debug("The message was not intended for me. Unknown dataId \n" 
    				+ msg.asXML());
    		return;
    	}
    	if(!pillars.contains(msg.getPillarId())) {
    		// do not handle message, when we are not awaiting pillarId
    		log.debug("The message was not intended for me. Unknown pillarId "
    				+ "\n" + msg.asXML());
    		return;
    	}

    	pillars.remove(msg.getPillarId());
    	
    	// no more missing pillars, then remove entry from 'missingGetTime',
    	// and continue with next action! Otherwise: await the remaining 
    	// pillars.
    	if(pillars.isEmpty()) {
    		log.info("Retrieved time for data '" + msg.getFileName() + "' from pillar '"
    				+ msg.getPillarId() + "'. No more outstanding pillars.");
    		missingPut.remove(msg.getFileName());
    		
    		// go to next action
    		nextAction();
    	} else {
    		log.info("Retrieved time for data '" + settings.getDataId() 
    				+ "' from pillar '" + msg.getPillarId() 
    				+ "', outstanding pillars: " + pillars);
    		missingPut.put(msg.getFileName(), pillars);
    	}
    }
    
    public synchronized void visit(MockupGetDataCompleteMessage msg, 
    		Destination replyTo) throws Exception {
    	log.info("Received GetDataCompleteMessage: '" + msg.asXML());
    	
    	File outputFile = new File(settings.getDataDir(), msg.getDataId());
    	if(outputFile.exists()) {
    		log.error("Cannot make output file '" + outputFile 
    				+ "'. It already exists.");
//    		return;
    	}
    	outputFile.createNewFile();
    	
    	OutputStream out = null;
    	try {
    		out = new FileOutputStream(outputFile);
    		MockupHTTPClient.getData(out, msg.getToken());
    	} finally {
    		if(out != null) {
    			out.flush();
    			out.close();
    		}
    	}

    	log.info("Data retrieved to: '" + outputFile.getAbsolutePath() + "'");
    	// It has been retrieved, and we move onto next action!
    	nextAction();
    }
    
    public synchronized void visit(MockupGetDataReplyMessage msg, 
    		Destination replyTo) {
    	log.info("Received GetDataReplyMessage: '" + msg.asXML());
    	// TODO something?
    }
    
    public synchronized void visit(MockupGetTimeReplyMessage msg, 
    		Destination replyTo) {
    	String pillarId = msg.getPillarId();
    	
    	// check if we are awaiting the message.
    	List<String> pillars = missingGetTime.get(msg.getConversationId());
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

    	// check for errors
    	if(msg.getErrorCode() != 0) {
    		log.warn("Received error message from '" + pillarId + "': '"
    				+ msg.getErrorCode() + " : " + msg.getErrorMessage() 
    				+ "'. Trying to proceed anyway!");
    	}

    	pillars.remove(pillarId);
    	
    	// handle replies for retrieval time for dataIds.
    	List<DataTime> replies = msg.getDataTimes();
    	for(DataTime dataTime : replies) {
        	log.info("Pillar '" + pillarId + "' can deliver in: " 
        			+ dataTime.timeMeasure + " " + dataTime.timeUnit);

        	// retrieve the 'DataRequestTime' entry for this data.
        	DataRequestTime drt = getTimes.get(dataTime.dataId);
        	if(drt == null) {
        		drt = new DataRequestTime(dataTime.dataId);
        	}
        	drt.addEntry(pillarId, dataTime.timeMeasure, dataTime.timeUnit);
        	getTimes.put(dataTime.dataId, drt);
    	}
    	
    	// no more missing pillars, then remove entry from 'missingGetTime',
    	// and continue with next action! Otherwise: await the remaining 
    	// pillars.
    	if(pillars.isEmpty()) {
    		log.info("Retrieved time for communication '" 
    				+ msg.getConversationId() + "' from pillar '"
    				+ pillarId + "'. No more outstanding pillars.");
    		missingGetTime.remove(msg.getConversationId());
    		
    		// go to next action
    		nextAction();
    	} else {
    		log.info("Retrieved time for communication '" 
    				+ msg.getConversationId() + "' from pillar '" 
    				+ pillarId + "', outstanding pillars: " + pillars);
    		missingGetTime.put(msg.getConversationId(), pillars);
    	}
    }
    
    public void onException(JMSException e) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }
    
    protected Integer getNextCommunicationId() {
    	return communicationId++;
    }
    
    private void sendMessage(MockupMessage msg) throws JMSException {
        TextMessage message = session.createTextMessage(msg.asXML());
        message.setJMSType(msg.getOperationId());
        message.setJMSReplyTo(queue);
        
        log.info("Sending message to bus: " + message.getText());
        messageProducer.send(bus, message);

        if (MockupConf.TRANSACTED) {
            session.commit();
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
    	
    	/**
    	 * 
    	 * @return
    	 */
    	public String getFastest() {
    		String res = null;
    		Long time = Long.MAX_VALUE;
    		for(Map.Entry<String, Long> entry : pillarTime.entrySet()) {
    			if(entry.getValue() < time) {
    				res = entry.getKey();
    				time = entry.getValue();
    			}
    		}
    		log.info("The fastest pillar: '" + res + "' with time '" + time 
    				+ "', out of " + pillarTime);
    		return res;
    	}
    };
}
