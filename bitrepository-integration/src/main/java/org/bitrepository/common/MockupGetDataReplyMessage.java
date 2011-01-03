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
package org.bitrepository.common;

/**
 * Message for telling that the GetDataMessage has been received and the 
 * process of retrieving the data and uploading it to the wanted destination
 * is about to begin.
 * When the data has been completely uploaded a MockupGetDataCompleteMessage
 * will be sent.
 * 
 * @author jolf
 *
 */
public class MockupGetDataReplyMessage extends MockupMessage {

	public MockupGetDataReplyMessage(String conversationId, String dataId) {
		setOperationId("GetDataReply");
		addConversationId(conversationId);
		setDataId(dataId);
	}
	
	public MockupGetDataReplyMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void setDataId(String dataId) {
		xmlDoc.setLeafValue("message.dataId", dataId);
	}
	
	public String getDataId() {
		return xmlDoc.getLeafValue("message.dataId");
	}
}
