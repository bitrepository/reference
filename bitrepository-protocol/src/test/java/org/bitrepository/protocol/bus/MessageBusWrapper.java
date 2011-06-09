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

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
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
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.MessageListener;
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
    public void sendMessage(String destinationId,
            GetChecksumsFinalResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetChecksumsRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            GetChecksumsProgressResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileFinalResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            GetFileIDsFinalResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileIDsRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            GetFileIDsProgressResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, GetFileRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            GetFileProgressResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForGetChecksumsResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForGetChecksumsRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForGetFileIDsRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForGetFileIDsResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForGetFileRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForGetFileResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForPutFileResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            IdentifyPillarsForPutFileRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileFinalResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId, PutFileRequest content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
    }

    @Override
    public void sendMessage(String destinationId,
            PutFileProgressResponse content) {
        testEventManager.addStimuli("Sending message (on topic " + destinationId + "): " + content);
        messageBus.sendMessage(destinationId, content);
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
