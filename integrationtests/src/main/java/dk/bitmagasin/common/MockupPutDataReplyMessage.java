package dk.bitmagasin.common;

public class MockupPutDataReplyMessage extends MockupMessage {
	public MockupPutDataReplyMessage(String conversationId, String filename, 
			String pillarId) {
		super();
		setOperationId("PutDataReply");
		addConversationId(conversationId);
		setFileName(filename);
		setPillarId(pillarId);
	}
	
	public MockupPutDataReplyMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void setFileName(String name) {
		xmlDoc.setLeafValue("message.filename", name);
	}
	
	public String getFileName() {
		return xmlDoc.getLeafValue("message.filename");
	}

	public void setPillarId(String id) {
		xmlDoc.setLeafValue("message.pillarId", id);
	}
	
	public String getPillarId() {
		return xmlDoc.getLeafValue("message.pillarId");
	}
}
