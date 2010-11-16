package dk.bitmagasin.common;

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
