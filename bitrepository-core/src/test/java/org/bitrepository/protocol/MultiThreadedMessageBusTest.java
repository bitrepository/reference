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
package org.bitrepository.protocol;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class for testing the interface with the message bus.
 */
public class MultiThreadedMessageBusTest extends IntegrationTest {
    /** The time to wait when sending a message before it definitely should 
     * have been consumed by a listener.*/
    static final int TIME_FOR_WAIT = 2500;
    private final static int threadCount = 3;
    private int count = 0;
    private final static String FINISH = "FINISH";
    private BlockingQueue<String> finishQueue = new LinkedBlockingQueue<String>(1);
    
    @Test(groups = { "regressiontest" })
    public final void manyTheadsBeforeFinish() throws Exception {
        addDescription("Tests whether it is possible to start the handling of many threads simultaneously.");
        IdentifyPillarsForGetFileRequest content =
                ExampleMessageFactory.createMessage(IdentifyPillarsForGetFileRequest.class);
        MultiMessageListener listener = new MultiMessageListener();
        messageBus.addListener("BusActivityTest", listener);
        content.setTo("BusActivityTest");
        
        addStep("Send one message for each listener", "When all have receiver, then they give respond on 'finishQueue'");
        for(int i = 0; i < threadCount; i++) {
            messageBus.sendMessage(content);
        }
        Assert.assertEquals(finishQueue.poll(TIME_FOR_WAIT, TimeUnit.MILLISECONDS), FINISH);
        Assert.assertEquals(count, threadCount);
    }
    
    @Override
    protected String getComponentID() {
        return getClass().getSimpleName();
    }
    
    protected class MultiMessageListener implements MessageListener {
        private BlockingQueue<String> queue = new LinkedBlockingQueue<String>(threadCount);
        
        @Override
        public final void onMessage(Message message) {
            try {
                testIfFinished();
                Assert.assertNotNull(queue.poll(TIME_FOR_WAIT, TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }
        
        private void testIfFinished() throws InterruptedException {
            count++;
            if(count >= threadCount) {
                for(int i = 0; i < threadCount; i++) {
                    queue.put("Count '" + i + "'");
                }
                finishQueue.put(FINISH);
            }
        }
    }
}
