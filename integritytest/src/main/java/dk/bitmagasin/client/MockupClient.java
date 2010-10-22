package dk.bitmagasin.client;
/** Bit Repository Standard Header Open Source License */

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
 * AccessClient MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupClient implements MessageListener, ExceptionListener {

    private boolean running;
    private String clientId = MockupConf.accessClientId; //TODO rename?

    private Session session;
    private MessageProducer messageProducer;

    private boolean verbose = false;
    private boolean transacted;//TODO QUESTION transacted?

    private boolean durable = true;
    private Topic bus;
    private Queue queue;
    
    private static String myQueue = "CLIENT";

    public static void main(String... args) {
    	System.out.println("Arguments (default): clientQueue (CLIENT)");
    	for(String arg : args) {
    		if(arg.startsWith("clientQueue=")) {
    			myQueue = arg.replaceFirst("clientQueue=", "");
    		} else {
    			System.err.println("Bad argument: " + arg);
    		}
    	}
    	
        MockupClient mockupClient = new MockupClient();
        mockupClient.run();
    }

    public void run() {
        Connection connection = null;
        try {
            // Create connection
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(MockupConf.user,
                            MockupConf.password, MockupConf.url);
            connection = connectionFactory.createConnection();
            if (durable && clientId != null && clientId.length() > 0 && !"null".equals(clientId)) {
                connection.setClientID(clientId);
            }
            connection.setExceptionListener(this);
            connection.start();

            // Create session
            session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);

            //create messagelistener on SLA topic
            bus = session.createTopic(MockupConf.SLAID);
            //messageConsumer = session.createDurableSubscriber(topic, clientId);
            session.createConsumer(bus).setMessageListener(this);
            
            // create messagelistener on the unique client queue.
            queue = session.createQueue(myQueue);
            session.createConsumer(queue).setMessageListener(this);
            
            // Create producer with PillarMockUp Queue destination
            messageProducer = session.createProducer(bus);
            messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
            MessageProducer queueProducer = session.createProducer(queue);
            queueProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

            // Send get message
            sendGet(session, messageProducer);

//        	TextMessage msg1 = session.createTextMessage("TESTING1");
//        	TextMessage msg2 = session.createTextMessage("TESTING2");
//
//        	messageProducer.send(bus, msg1);
//        	queueProducer.send(queue, msg2);
//        	
        	// Use the ActiveMQConnection interface to dump the connection
            // stats.
            // ActiveMQConnection c = (ActiveMQConnection)connection;
            // c.getConnectionStats().dump(new IndentPrinter());

        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        } 
    }

    protected void sendGet(Session session, MessageProducer producer) 
            throws Exception {
        //TODO multiple gets
        String commId = "CommId28";//TODO communication ID generation
        String dataId = "1615";
        String token = "https://example.dk/token#1";

        MockupGetTimeMessage msg = new MockupGetTimeMessage();
        msg.addConversationId(commId);
        msg.addDataId(dataId);
//        msg.addPillars(MockupConf.pillarId);
        msg.addPillars(MockupConf.pillarId, "KB", "SB", "THIS_IS_NOT_A_PILLAR");
        msg.addToken(token);
        
        TextMessage message = session.createTextMessage(msg.asXML());
        message.setJMSType("GetTime");
        message.setJMSReplyTo(queue);

        if (verbose) {
            System.out.println("Sending message to bus: " + message.getText());
        }
        producer.send(bus, message);
        
//        if (verbose) {
//            System.out.println("Sending message to queue: " + message.getText());
//        }
//        MessageProducer mp = session.createProducer(queue);
//        mp.setDeliveryMode(DeliveryMode.PERSISTENT);
//        mp.send(queue, message);
        
        if (transacted) {
            session.commit();
        }
    }

    public void onMessage(Message msg) { 
//    	if(true) {
//    		System.out.println("Received a message: " + msg);
//    		return;
//    	}
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
        		System.out.println("ERROR: Unhandled message jms type: " 
        				+ txtMsg.getJMSType());
        		return;
        	}
        	
    		// TODO make more of these cases.
        	if(txtMsg.getJMSType().equals("GetTimeReply")) {
        		System.out.println("handled!");
        		visit(new MockupGetTimeReplyMessage(txtMsg.getText()), 
        				txtMsg.getJMSReplyTo());
        	} else {
        		System.out.println("ignored!");
        	}
    	} catch (Exception e) {
    		System.err.println(e);
    		e.printStackTrace();
    	}
    }
    
    public void visit(MockupGetTimeReplyMessage msg, Destination replyTo) {
//    	System.out.println("Handling MockupGetTimeReplyMessage: " 
//    			+ msg.asXML());
    	System.out.println("Pillar '" + msg.getPillarId() 
    			+ "' can deliver in: " + msg.getTimeMeasure() + " " 
    			+ msg.getTimeUnit());
    	// TODO ??
    }
    
    public void onException(JMSException e) {
        System.out.println("JMS Exception occured.  Shutting down client.");
        running = false;
    }

    synchronized boolean isRunning() {
        return running;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }
}
