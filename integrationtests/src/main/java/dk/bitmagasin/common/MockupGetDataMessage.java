package dk.bitmagasin.common;

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
