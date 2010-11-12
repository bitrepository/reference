package dk.bitmagasin.common;

/**
 * The message for telling when a piece of data has completely been put onto
 * the requested location.
 * 
 * @author jolf
 *
 */
public class MockupGetDataCompleteMessage extends MockupMessage {
	public MockupGetDataCompleteMessage(String conversationId, String dataId,
			String token) {
		setOperationId("GetDataComplete");
		addConversationId(conversationId);
		setDataId(dataId);
		setToken(token);
	}
	
	public MockupGetDataCompleteMessage(String xml) throws Exception {
		super(xml);
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
