/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessComponentFactory.java 212 2011-07-05 10:04:10Z bam $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/AccessComponentFactory.java $
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
package org.bitrepository.alarm;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
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
import org.bitrepository.protocol.conversation.Conversation;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;

public class ConversationReceiverMediator<T> implements ConversationMediator {

	private final MessageBus messagebus;
	private final String listenerDestination;
	private final AlarmHandler handler;
	
	public ConversationReceiverMediator(MessageBus messagebus, String listenerDestination, AlarmHandler handler) {
        this.messagebus = messagebus;
        this.listenerDestination = listenerDestination;
        this.handler = handler;
        
        messagebus.addListener(listenerDestination, this);        
	}
	
	@Override
	public void addConversation(Conversation arg0) {
		// TODO this should not be used.
	}
	
	@Override
	public void onMessage(Alarm msg) {
		
	}

	@Override
	public void onMessage(GetAuditTrailsRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetAuditTrailsProgressResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetAuditTrailsFinalResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetChecksumsFinalResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetChecksumsRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetChecksumsProgressResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetFileFinalResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetFileIDsFinalResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetFileIDsRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetFileIDsProgressResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetFileRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetFileProgressResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetStatusRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetStatusProgressResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(GetStatusFinalResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForGetChecksumsResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForGetChecksumsRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForGetFileIDsResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForGetFileIDsRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForGetFileResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForGetFileRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForPutFileResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(IdentifyPillarsForPutFileRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(PutFileFinalResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(PutFileRequest arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(PutFileProgressResponse arg0) {
		// TODO Auto-generated method stub
		
	}
}
