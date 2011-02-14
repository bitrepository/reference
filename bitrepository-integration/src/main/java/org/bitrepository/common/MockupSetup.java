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
package org.bitrepository.common;


/**
 * Set-up MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupSetup {
    public static void main(String[] argv) throws Exception {
        MockupSetup setup = new MockupSetup();
        setup.run();
    }

    private void run() throws Exception {
        // Create connection
//        ActiveMQConnectionFactory connectionFactory =
//                new ActiveMQConnectionFactory(MockupConf.user,
//                        MockupConf.password, MockupConf.url);
//        connection = connectionFactory.createConnection();

        /* Create Session
        * boolean transacted false (indicates whether the session is transacted)
        * int acknowledgeMode = Session.AUTO_ACKNOWLEDGE
        * (with this acknowledgment mode, the session automatically acknowledges
        * a client's receipt of a message either when the session has successfully
        * returned from a call to receive or when the message listener the session
        * has called to process the message successfully returns */
//        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create Topic. Note that this method is not for creating the physical
        // topic. The physical creation of topics is an administrative task and
        // is not to be initiated by the JMS API.
//        topic = session.createTopic(MockupConf.SLAID);

//        connection.start();

        //Thread.sleep(delay * 1000);
        //connection.stop();
        //connection.close();
    }

}
