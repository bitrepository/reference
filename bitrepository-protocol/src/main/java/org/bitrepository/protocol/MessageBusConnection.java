package org.bitrepository.protocol;

import javax.jms.JMSException;
import javax.jms.MessageListener;

public interface MessageBusConnection {
	/**
	 * The retrieval of a bus connection instance.
	 * @param property The properties for the connection.
	 * @return The connection for the given connection property.
	 */
//	MessageBusConnection getInstance(ConnectionProperty property);
	
	/**
	 * Adds the supplied listener to the indicated topic
	 * @param topicId The topic to listen to
	 * @param listener The listener with should handle the messages arriving on 
	 * the topic
	 * @throws JMSException Something has gone wrong in the JMS messaging
	 */
    void addListener(String topicId, MessageListener listener) 
	        throws JMSException;
	
    /**
     * Removes the supplied listener from the indicated topic.
     * @param topidId The id for the topic, where the listener should be 
     * removed.
     * @param listener The listener to remove from the topic.
     * @throws JMSException If something goes wrong with the JMS-connection.
     */
    void removeListener(String topidId, MessageListener listener) 
            throws JMSException;
    
    /**
     * Method for sending a message on a specific queue/topic.
     * 
     * @param queueId The id for the queue/topic to send message.
     * @param content The content of the message.
     * @throws JMSException If a problem with the connection to the Bus occurs 
     * during the transportation of this message.
     */
    void sendMessage(String queueId, String content)
            throws JMSException;
    
}
