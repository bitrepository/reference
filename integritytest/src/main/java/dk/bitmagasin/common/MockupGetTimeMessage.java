package dk.bitmagasin.common;

import java.util.List;

public class MockupGetTimeMessage extends MockupMessage {
	
	public MockupGetTimeMessage() {
		super();
		xmlDoc.setLeafValue("message.operationId", "GetTime");
	}
	
	public MockupGetTimeMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void addPillars(String... pillarIds) {
		for(String pId : pillarIds) {
			xmlDoc.addNewBranchValue("message.pillarIds", "pillarId", pId);
		}
	}
	
	public List<String> getPillarIds() {
		return xmlDoc.getLeafValues("message.pillarIds.pillarId");
	}
	
	public void addDataId(String dataId) {
		xmlDoc.setLeafValue("message.dataId", dataId);
	}
	
	public String getDataId() {
		return xmlDoc.getLeafValue("message.dataId");
	}
}
