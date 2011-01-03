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

public class MockupGetDataMessage extends MockupMessage {

	public MockupGetDataMessage(String dataId, String pillarId, String token) {
		super();
		setOperationId("GetData");
		setPillarId(pillarId);
		setDataId(dataId);
		setToken(token);
	}
	
	public MockupGetDataMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void setPillarId(String pillarId) {
		xmlDoc.setLeafValue("message.pillarId", pillarId);
	}
	
	public String getPillarId() {
		return xmlDoc.getLeafValue("message.pillarId");
	}
	
	public void setDataId(String dataId) {
		xmlDoc.setLeafValue("message.dataId", dataId);
	}
	
	public String getDataId() {
		return xmlDoc.getLeafValue("message.dataId");
	}
	
	public void setToken(String token) {
		xmlDoc.setLeafValue("message.token", token);
	}
	
	public String getToken() {
		return xmlDoc.getLeafValue("message.token");
	}
}
