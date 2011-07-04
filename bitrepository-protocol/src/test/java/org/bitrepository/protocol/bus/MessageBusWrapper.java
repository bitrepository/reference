/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.bus;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.jaccept.TestEventManager;

public class MessageBusWrapper implements MessageBus {
    private final MessageBus messageBus;
    private final TestEventManager testEventManager;
       
    public MessageBusWrapper(MessageBus messageBus, TestEventManager testEventManager) {
        super();
        this.messageBus = messageBus;
        this.testEventManager = testEventManager;
    }

    @Override
    public void sendMessage(Alarm content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }
    
    @Override
    public void sendMessage(GetChecksumsFinalResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetChecksumsRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetChecksumsProgressResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileFinalResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileIDsFinalResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileIDsRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileIDsProgressResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetFileProgressResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(GetStatusRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }
    
    @Override
    public void sendMessage(GetStatusProgressResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }
    
    @Override
    public void sendMessage(GetStatusFinalResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }
    
    @Override
    public void sendMessage(IdentifyPillarsForGetChecksumsResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetChecksumsRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileIDsRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileIDsResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForGetFileResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForPutFileResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(IdentifyPillarsForPutFileRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(PutFileFinalResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(PutFileRequest content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void sendMessage(PutFileProgressResponse content) {
        testEventManager.addStimuli("Sending message: " + content);
        messageBus.sendMessage(content);
    }

    @Override
    public void addListener(String destinationId, MessageListener listener) {
        messageBus.addListener(destinationId, listener);
    }

    @Override
    public void removeListener(String destinationId, MessageListener listener) {
        messageBus.removeListener(destinationId, listener);
    }

}
