package dk.bitmagasin.common;
/** Bit Repository Standard Header Open Source License */

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Set-up MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupSetup {

    private Connection connection;
    private Session session;
    private Topic topic;

    private long delay;

    public static void main(String[] argv) throws Exception {
        MockupSetup setup = new MockupSetup();
        setup.run();
    }

    private void run() throws Exception {
        // Create connection
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(MockupConf.user,
                        MockupConf.password, MockupConf.url);
        connection = connectionFactory.createConnection();

        /* Create Session
        * boolean transacted false (indicates whether the session is transacted)
        * int acknowledgeMode = Session.AUTO_ACKNOWLEDGE
        * (with this acknowledgment mode, the session automatically acknowledges
        * a client's receipt of a message either when the session has successfully
        * returned from a call to receive or when the message listener the session
        * has called to process the message successfully returns */
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create Topic. Note that this method is not for creating the physical
        // topic. The physical creation of topics is an administrative task and
        // is not to be initiated by the JMS API.
        topic = session.createTopic(MockupConf.SLAID);

        connection.start();

        //Thread.sleep(delay * 1000);
        //connection.stop();
        //connection.close();
    }

}
