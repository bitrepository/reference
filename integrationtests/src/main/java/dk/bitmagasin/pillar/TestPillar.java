package dk.bitmagasin.pillar;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

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

public class TestPillar implements MessageListener, ExceptionListener {
	private Log log = LogFactory.getLog(this.getClass());
	private static MockupSettings settings;
	private String pillarId;
	private String name;
	private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
	private MessageProcessor messageProcessor;
    private Session session;
    private Topic bus;
    private MessageProducer busProducer;
	
	public TestPillar(String pillarId) throws JMSException {
		this.pillarId = pillarId;
		name = pillarId + "TestPillar";
    	settings = MockupSettings.getInstance();    		
    	createConnection();
    	messageProcessor = new MessageProcessor();
    	new Thread(messageProcessor).start();
    	log.info("Started "+name);
	}
	
	private void createConnection() throws JMSException {
    		log.info("Connection to messagebus " + settings.getConnectionUrl());
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(MockupConf.user, MockupConf.password, settings.getConnectionUrl());
            Connection connection = connectionFactory.createConnection();
                connection.setClientID(settings.getEnvironmentName() + "_" 
                		+ pillarId);
            log.debug("Connection created to " + settings.getEnvironmentName() + "_" + pillarId);
            connection.setExceptionListener(this);
            connection.start();            
            log.debug("Connection started");
            
    		session = connection.createSession(MockupConf.TRANSACTED, MockupConf.ACKNOWLEDGE_MODE);

    		log.debug("Create messagelistener on SLA topic " + settings.getSlaTopicId());
            bus = session.createTopic(settings.getSlaTopicId());
            //consumer = session.createDurableSubscriber(topic, MockupConf.pillarId);
            session.createConsumer(bus).setMessageListener(this);            

    		log.debug("Create non-persistent producer on bus " + settings.getSlaTopicId());
            busProducer = session.createProducer(bus);
            busProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}
	
	public String readMessageBlocking() {
		return messageQueue.poll();
	}
	
	public void sendMessage(String message) {
		log.info("Sending message"+message);
	}
	
	private class MessageProcessor implements Runnable {
		@Override
		public void run() {
			
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
    	log.warn("Cannot not handle MockupMessage: " + msg.asXML());
    }

    public synchronized void onException(JMSException ex) {
        log.error("JMS Exception occured.  Shutting down client.", ex);
    }
}
