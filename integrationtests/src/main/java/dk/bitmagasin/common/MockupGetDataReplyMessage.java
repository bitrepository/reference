package dk.bitmagasin.common;

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
