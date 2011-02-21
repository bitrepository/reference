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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.protocol.Message;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.MessageFactory;
import org.bitrepository.protocol.MessageListener;
import org.bitrepository.protocol.ProtocolComponentFactory;

/**
 * The client for sending and handling 'Get' messages.
 * 
 * @author jolf
 */
public class GetClient implements MessageListener {
    private MessageBus messageBus;
    String queue;
    
    /**
     * Constructor.
     * @throws Exception If a connection to the messagebus could not be 
     * established.
     */
    public GetClient() throws Exception {
        // TODO
        // Load settings!
        // Establish connection to bus!
        
        queue = "DefaultTopic";
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        //con.addListener(queue, this);
    }
    
    public void getData(String dataId) throws Exception {
        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        msg.setMinVersion((short) 1);
        msg.setVersion((short) 1);
        msg.setFileID(dataId);
        msg.setCorrelationID("TheCorrelationID_FOR_THIS_MESSAGE");
        msg.setSlaID("ID-FOR-THE-SLA");
        msg.setReplyTo(queue);
        

        messageBus.sendMessage(queue, MessageFactory.retrieveMessage(msg));
        
//        GetRequest msg = new GetRequest();
//        msg.setVersion((short) 1);
//        msg.setMinVersion((short) 1);
//        msg.setDataID(dataId);
//        msg.
    }

    @Override
    public void onMessage(Message message) {
        // TODO Auto-generated method stub
        
    }
}
