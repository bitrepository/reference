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
