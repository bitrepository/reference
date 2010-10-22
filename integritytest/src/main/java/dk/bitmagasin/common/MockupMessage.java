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
package dk.bitmagasin.common;


public class MockupMessage {
	
	MockupXmlDocument xmlDoc;
	
	protected MockupMessage() {
		xmlDoc = new MockupXmlDocument();
		xmlDoc.setLeafValue("message", "GetTime");
	}
	
	protected MockupMessage(String xml) throws Exception {
		xmlDoc = new MockupXmlDocument(xml);
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
	
	public void addToken(String token) {
		xmlDoc.setLeafValue("message.token", token);
	}
	
	public String getToken() {
		return xmlDoc.getLeafValue("message.token");
	}
	
	public void addError(String err) {
		xmlDoc.setLeafValue("message.error", err);
	}
	
	public String errMessage() {
		return xmlDoc.getLeafValue("message.error");
	}
	
	public String asXML() {
		return xmlDoc.asXML();
	}
}
