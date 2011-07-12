/*
 * #%L
 * Bitmagasin modify client
 * 
 * $Id: SimplePutClient.java 191 2011-06-15 08:31:38Z kfc $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/SimplePutClient.java $
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
package org.bitrepository.modify.put;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsData.ChecksumDataItems;
import org.bitrepository.modify.put.conversation.SimplePutFileConversation;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the PutClient.
 * 
 * TODO perhaps merge the 'outstanding' and the 'FileIdForPut'?
 */
public class SimplePutClient implements PutClient {
    /** The log for this class.*/
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final ConversationMediator<SimplePutFileConversation> conversationMediator;

    public SimplePutClient(MessageBus messageBus, PutFileClientSettings settings) {
    	conversationMediator = new CollectionBasedConversationMediator<SimplePutFileConversation>(settings, messageBus,
    			settings.getClientTopicID());
    }

	@Override
	public void putFileWithId(URL url, String fileId, String collectionId) {
		
	}
    
}
