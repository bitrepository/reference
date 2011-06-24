package org.bitrepository.protocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Stress testing of the messagebus. 
 */
public class MessageBusNumberOfListenersStressTest extends ExtendedTestCase {
	/** The queue name.*/
	private static String QUEUE = "TEST-LISTENERS";
	/** */
	private static long DEFAULT_WAIT_TIME = 500;
	
	/** The reached correlation ID for the message.*/
	private static int idReached = -1;
	
	/** The message to send back and forth over the message bus.*/
	private static Alarm alarmMessage;
	
	/** The message bus instance for sending the messages.*/
	private static MessageBus bus;
	
	/** The amount of messages received.*/
	private static int messageReceived = 0;
	
	/** Whether more messages should be send.*/
	private static boolean sendMoreMessages = true;
	
	/**
	 * Tests the amount of messages send over a message bus, which is not placed locally.
	 * Requires to send at least five per second.
	 * @throws Exception 
	 */
	@Test( groups = {"StressTest"} )
	public void testManyListeners() throws Exception {
		addDescription("Tests how many messages can be handled within a given timeframe when a given number of "
				+ "listeners are receiving them.");
		addStep("Define constants", "This should not be possible to fail.");
		final int numberOfListeners = 10;
		long timeFrame = 60000L; // one minute in millis
		QUEUE += "-" + (new Date()).getTime();
		
		addStep("Define the message to send.", "Should retrieve the Alarm message from examples and set the To.");
		alarmMessage = ExampleMessageFactory.createMessage(Alarm.class);
		alarmMessage.setTo(QUEUE);
		
		addStep("Make configuration for the messagebus.", "Both should be created.");
		MessageBusConfigurations confs = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
		LocalActiveMQBroker broker = new LocalActiveMQBroker(confs.getPrimaryMessageBusConfiguration());
		List<NotificationMessageListener> listeners = new ArrayList<NotificationMessageListener>(numberOfListeners);
		
		try {
			addStep("Start the broker and initialise the listeners.", 
					"Connections should be established.");
			broker.start();
			bus = new ActiveMQMessageBus(confs);
			
			addStep("Initialise the message listeners.", "Should be created and connected to the message bus.");
			for(int i = 0; i < numberOfListeners; i++) {
				listeners.add(new NotificationMessageListener(confs));
			}
			
			addStep("Wait for setup", "We wait!");
            synchronized (this) {
                try {
                    wait(DEFAULT_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
			
			
			addStep("Send the first message", "Message should be send.");
			sendMessageWithId(1);
			
			addStep("Wait for the timeframe on '" + timeFrame + "' milliseconds.", 
					"We wait!");
            synchronized (this) {
                try {
                    wait(timeFrame);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
			
			addStep("Stop sending more messages and await all the messages to be received by all the listeners", 
					"Should be Ok");
			sendMoreMessages = false;
            synchronized (this) {
                try {
                    wait(DEFAULT_WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
			
			addStep("Verifying the amount of message sent.", 
					"Should be the same amount for each listener, and the same amount as the correlation ID of the message");
			Assert.assertTrue(idReached * numberOfListeners == messageReceived, 
					"Reached message Id " + idReached + " thus each message of the " + numberOfListeners + " listener "
					+ "should have received " + idReached + " message, though they have received " 
					+ messageReceived + " message all together.");
			for(NotificationMessageListener listener : listeners) {
				Assert.assertTrue((listener.getCount() == idReached), 
						"Should have received " + idReached + " messages, but has received " 
						+ listener.getCount());
			}
		} finally {
			if(listeners != null) {
				for(NotificationMessageListener listener : listeners) {
					listener.stop();
				}
				listeners.clear();
				listeners = null;
			}
			if(broker != null) {
				broker.stop();
				broker = null;
			}
		}
	}
	
	/**
	 * Method for sending the Alarm message with a specific ID.
	 * 
	 * @param id The correlation id for the message to send.
	 */
	private static void sendMessageWithId(int id) {
		if(sendMoreMessages) {
			alarmMessage.setCorrelationID("" + id);
			bus.sendMessage(alarmMessage);
		}
	}
	
	/**
	 * Function for handling the Correlation id of the received messages of the listeners.
	 * If it is the first time a correlation id is received, then a new message with the subsequent correlation 
	 * id is sent. This ensures that the message is only sent once per Correlation id.
	 * 
	 * @param receivedId The received correlation id.
	 */
	public static synchronized void handleMessageDistribution(int receivedId) {
		if(receivedId > idReached) {
			idReached = receivedId;
			sendMessageWithId(idReached + 1);
		}
		messageReceived++;
	}
	
	/**
	 * Messagelistener which notifies the 'handleMessageDistribution' method with the correlation id whenever 
	 * a message it received.
	 * Otherwise counts the amount of received messages.
	 */
	private class NotificationMessageListener extends AbstractMessageListener {
		/** The message bus.*/
		private final MessageBus bus;
		/** The amount of messages received.*/
		private int count;
		
		/**
		 * Constructor.
		 * @param confs The configurations for declaring the messagebus.
		 */
		public NotificationMessageListener(MessageBusConfigurations confs) {
			this.bus = new ActiveMQMessageBus(confs);
			this.count = 0;
			
			bus.addListener(QUEUE, this);
		}
		
		/**
		 * Method for stopping interaction with the messagelistener.
		 */
		public void stop() {
			bus.removeListener(QUEUE, this);
		}
		
		/**
		 * Retrieval of the amount of messages caught by the listener.
		 * @return The number of message received by this.
		 */
		public int getCount() {
			return count;
		}
		
		@Override
		public void onMessage(Alarm message) {
			count++;
			int receivedId = Integer.parseInt(message.getCorrelationID());
			handleMessageDistribution(receivedId);
		}
	}
}
