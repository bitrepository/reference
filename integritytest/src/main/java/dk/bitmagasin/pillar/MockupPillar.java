package dk.bitmagasin.pillar;
/** Bit Repository Standard Header Open Source License */

import java.util.List;

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

import dk.bitmagasin.common.MockupConf;
import dk.bitmagasin.common.MockupGetTimeMessage;
import dk.bitmagasin.common.MockupGetTimeReplyMessage;

/**
 * Pillar MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupPillar implements MessageListener, ExceptionListener {

	/**
	 * The measure for the timeout for this pillar.
	 */
    private static long timeoutMeasure = 1;
    /**
     * The unit for the timeout for this pillar.
     */
    private static String timeoutUnit = "sec";
    /**
     * The error code for retrieval of data from this pillar.
     */
    private static String errorCode = null;
    /**
     * The id for this pillar.
     */
    private static String pillarId = MockupConf.pillarId;

    private boolean running;
    private Session session;
    
    /**
     * The destination of the bus.
     */
    private Topic bus;
    /**
     * The producer to the bus.
     */
    private MessageProducer busProducer;
    /**
     * The specific queue for messages sent only to this client.
     */
    private Queue queue;
    /**
     * The producer for sending message to this client only.
     * TODO this should be removed!
     */
    private MessageProducer queueProducer;

    private boolean verbose = true;
    private boolean transacted;
    private boolean durable = true;
    private int ackMode = Session.AUTO_ACKNOWLEDGE; //TODO probably not a good idea
    
    public static void main(String[] args) {
    	System.out.println("Arguments (default): timeoutMeasure (1), "
    			+ "timeoutUnit (sec), errorCode (null), pillarId (?)");
    	for(String arg : args) {
    		if(arg.startsWith("timeoutMeasure=")) {
    			timeoutMeasure = Long.parseLong(arg.replaceFirst("timeoutMeasure=", ""));
    		} else if(arg.startsWith("timeoutUnit=")) {
    			timeoutUnit = arg.replace("timeoutUnit=", "");
    		} else if(arg.startsWith("errorCode=")) {
    			errorCode = arg.replace("errorCode=", "");
    		} else if(arg.startsWith("pillarId=")) {
    			pillarId = arg.replace("pillarId=", "");
    		} else {
    			System.err.println("Bad argument: " + arg);
    		}
    	}

        MockupPillar mockupPillar = new MockupPillar();
        mockupPillar.run();
    }

    public void run() {
        try {
            running = true;

            // Create connection
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(MockupConf.user,
                            MockupConf.password, MockupConf.url);
            Connection connection = connectionFactory.createConnection();
            if (durable && pillarId != null && pillarId.length() > 0 
            		&& !"null".equals(pillarId)) {
                connection.setClientID(pillarId);
            }
            connection.setExceptionListener(this);
            connection.start();

            session = connection.createSession(transacted, ackMode);

            //create messagelistener on SLA topic
            bus = session.createTopic(MockupConf.SLAID);
            //consumer = session.createDurableSubscriber(topic, MockupConf.pillarId);
            session.createConsumer(bus).setMessageListener(this);
            busProducer = session.createProducer(bus);
            busProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            queue = session.createQueue(pillarId);
            //consumer = session.createDurableSubscriber(topic, MockupConf.pillarId);
            session.createConsumer(queue).setMessageListener(this);
            queueProducer = session.createProducer(queue);
            queueProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
    
    public void onMessage(Message msg) {
        try {
    		// report on which message is received
        	System.out.print("Message: " + msg.getJMSType() + ", on " 
        			+ msg.getJMSDestination() + "  \t ");
        	
    		if(!(msg instanceof TextMessage)) {
            	// Cannot handled non-TextMessage!
            	System.out.println("ERROR: Not in the format of a "
            			+ "TextMessage: \n" + msg);
        		return;	
    		}
    		
        	TextMessage txtMsg = (TextMessage) msg;

        	// verify getType
        	if(txtMsg.getJMSType() == null || txtMsg.getJMSType().isEmpty()) {
        		// TODO log error!
        		System.err.println("ERROR: Unhandled message jms type: " 
        				+ txtMsg.getJMSType());
        		return;
        	}
        	
    		// TODO make more of these cases.
        	if(txtMsg.getJMSType().equals("GetTime")) {
        		System.out.println("handled!");
        		visit(new MockupGetTimeMessage(txtMsg.getText()), 
        				txtMsg.getJMSReplyTo());
        	} else {
        		System.out.println("ignored!");
        	}
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public void visit(MockupGetTimeMessage msg, Destination replyTo) 
            throws JMSException {
    	if(verbose) {
    		System.out.println("Received MockupGetTimeMessage, with reply: " 
    				+ replyTo);
    	}
    	
    	// validate pillarId
    	List<String> pillarIds = msg.getPillarIds();
    	if(!pillarIds.contains(pillarId)) {
    		System.out.println("Ignored!");
    		// Do not handle message, which are not meant for us!
    		return;
    	}

    	MockupGetTimeReplyMessage replyMsg = new MockupGetTimeReplyMessage();
    	replyMsg.setDataId(msg.getDataId());
    	replyMsg.addConversationId(msg.getConversationId());
    	replyMsg.addTimeUnit(timeoutUnit);
    	replyMsg.setTimeMeasure(timeoutMeasure);
    	replyMsg.setPillarId(pillarId);
    	if(errorCode != null) {
    		replyMsg.addError(errorCode);
    	}
    	
        TextMessage sendMsg = session.createTextMessage(replyMsg.asXML());
        sendMsg.setJMSType("GetTimeReply");
        sendMsg.setJMSReplyTo(queue);

        if(verbose) {
        	System.out.println("Sending: MockupGetTimeReplyMessage to: " 
        			+ replyTo);
        }
        
        MessageProducer mp = session.createProducer(replyTo);
        mp.send(replyTo, sendMsg);
        if(transacted) {
        	session.commit();
        }
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
        running = false;
    }

    synchronized boolean isRunning() {
        return running;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
