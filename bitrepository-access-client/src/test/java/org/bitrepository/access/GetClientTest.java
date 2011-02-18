/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.access;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.protocol.ConnectionFactory;
import org.bitrepository.protocol.Message;
import org.bitrepository.protocol.MessageFactory;
import org.bitrepository.protocol.MessageListener;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetClient'.
 * @author jolf
 */
public class GetClientTest extends ExtendedTestCase {

    @Test(groups = {"regressiontest"})
    public void sendMessageTest() throws Exception {
        addDescription("Tests whether a specific message is sent by the GetClient");
        String dataId = "dataId1";
        GetClient gc = new GetClient();
        TestMessageListener listener = new TestMessageListener();
        ConnectionFactory.getInstance().addListener(gc.queue, listener);
        gc.getData(dataId);

        synchronized(this) {
            try {
            wait(500);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        Assert.assertNotNull(listener.getMessage(), "The message must not be null.");
        IdentifyPillarsForGetFileRequest message = MessageFactory.createMessage(
                IdentifyPillarsForGetFileRequest.class, listener.getMessage());
        Assert.assertEquals(message.getFileID(), dataId);
    }
    
    /**
     * 
     * @author jolf
     */
    protected class TestMessageListener implements MessageListener, 
            ExceptionListener {
        private String message = null;
        @Override
        public void onMessage(Message msg) {
            try {
                message = msg.getText();
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }
        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
        public String getMessage() {
            return message;
        }
    }
}
