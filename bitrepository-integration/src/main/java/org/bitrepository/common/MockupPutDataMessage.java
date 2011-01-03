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

import java.net.URL;
import java.util.List;

public class MockupPutDataMessage extends MockupMessage {

	public MockupPutDataMessage(String conversationId, URL url, 
			String filename, String... pillarIds) {
		super();
		setOperationId("PutData");
		addConversationId(conversationId);
		setToken(url.toExternalForm());
		setFileName(filename);
		addPillars(pillarIds);
	}
	
	public MockupPutDataMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void setToken(String token) {
		xmlDoc.setLeafValue("message.token", token);
	}
	
	public String getToken() {
		return xmlDoc.getLeafValue("message.token");
	}
	
	public void setFileName(String name) {
		xmlDoc.setLeafValue("message.filename", name);
	}
	
	public String getFileName() {
		return xmlDoc.getLeafValue("message.filename");
	}
	
	public void addPillars(String... pillarIds) {
		for(String pId : pillarIds) {
			xmlDoc.addNewBranchValue("message.pillarIds", "pillarId", pId);
		}
	}
	
	public List<String> getPillarIds() {
		return xmlDoc.getLeafValues("message.pillarIds.pillarId");
	}
}
