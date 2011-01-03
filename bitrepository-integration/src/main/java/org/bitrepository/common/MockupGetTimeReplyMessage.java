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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockupGetTimeReplyMessage extends MockupMessage {

	public MockupGetTimeReplyMessage(String conversationId, String pillarId) {
		super();
		xmlDoc.setLeafValue("message.operationId", "GetTimeReply");
		addConversationId(conversationId);
		setPillarId(pillarId);
	}
	
	public MockupGetTimeReplyMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void addTimeForDataId(String dataId, Long timeMeasure, 
			TimeUnits timeUnit) {
		xmlDoc.addNewBranch("message.dataTimes", "data", 
				new DataTime(dataId, timeMeasure, timeUnit).getAsMap());
	}
	
	public void addTimeForDataId(DataTime dt) {
		xmlDoc.addNewBranch("message.dataTimes", "data", dt.getAsMap());
	}
	
	public List<DataTime> getDataTimes() {
		List<Map<String, String>> content = 
			xmlDoc.retrieveBranches("message.dataTimes", "data");
		List<DataTime> res = new ArrayList<DataTime>(content.size());
		for(Map<String, String> dataTimeMap : content) {
			res.add(new DataTime(dataTimeMap));
		}
		return res;
	}
	
	public void setPillarId(String pillarId) {
		xmlDoc.setLeafValue("message.pillarId", pillarId);
	}
	
	public String getPillarId() {
		return xmlDoc.getLeafValue("message.pillarId");
	}
	
	public void addError(Integer code, String message) {
		xmlDoc.setLeafValue("message.error.errorCode", code.toString());
		xmlDoc.setLeafValue("message.error.errorMessage", message);
	}
	
	public Integer getErrorCode() {
		String res = xmlDoc.getLeafValue("message.error.errorCode");
		if(res == null) {
			return 0;
		} else {
			return Integer.parseInt(res);
		}
	}
	
	public String getErrorMessage() {
		return xmlDoc.getLeafValue("message.error.errorMessage");
	}
}
