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


public class MockupMessage {
	
	MockupXmlDocument xmlDoc;
	
	protected MockupMessage() {
		xmlDoc = new MockupXmlDocument();
	}
	
	protected MockupMessage(String xml) throws Exception {
		xmlDoc = new MockupXmlDocument(xml);
	}

	public void setOperationId(String id) {
		xmlDoc.setLeafValue("message.operationId", id);
	}
	
	public String getOperationId() {
		return xmlDoc.getLeafValue("message.operationId");
	}

	public void addConversationId(String conversationId) {
		xmlDoc.setLeafValue("message.conversationId", conversationId);
	}
	
	public String getConversationId() {
		return xmlDoc.getLeafValue("message.conversationId");
	}
	
	public void addReplyQueue(String queue) {
		xmlDoc.setLeafValue("message.replyQueueName", queue);
	}
	
	public String getReplyQueue() {
		return xmlDoc.getLeafValue("message.replyQueueName");
	}
	
	public String asXML() {
		return xmlDoc.asXML();
	}
}
