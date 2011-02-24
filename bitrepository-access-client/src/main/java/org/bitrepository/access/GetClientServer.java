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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileReply;
import org.bitrepository.protocol.Message;
import org.bitrepository.protocol.MessageFactory;
import org.bitrepository.protocol.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler of the messages. Just takes the message and sends it to the 
 * corresponding method in the actual GetClient.
 * TODO this should probably be renamed, or made obsolete by refacturing.
 */
public class GetClientServer implements MessageListener {
    private Logger log = LoggerFactory.getLogger(GetClientServer.class);

    private GetClient client;
    
    public GetClientServer(GetClient gc) {
        this.client = gc;
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            Class msgClass = message.getMessageType();
            // test which kind of message, and handle the corresponding.
            if(msgClass == null) {
                return;
            }
            if(msgClass == IdentifyPillarsForGetFileReply.class) {
                visit((IdentifyPillarsForGetFileReply) MessageFactory.createMessage(
                        msgClass, message.getText()));
            } else {
                log.debug("Unhandled message received '" + msgClass);
            }
        } catch (Exception e) {
            // TODO handle this!
            log.error("Unexpected exception during handling of message '" 
                    + message + "'", e);
            e.printStackTrace();
        }
    }
    
    public void visit(IdentifyPillarsForGetFileReply msg) {
        log.info("Received IdentifyPillarsForGetFileReply '" + msg + "'.");
        client.handleReplyForFastest(msg);
    }
    
    public void visit(Object msg) {
        log.info("Received unexpected message object '" + msg + "'");
        return;
    }

}
