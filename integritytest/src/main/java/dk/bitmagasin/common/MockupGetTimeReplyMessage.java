package dk.bitmagasin.common;

public class MockupGetTimeReplyMessage extends MockupMessage {

	public MockupGetTimeReplyMessage() {
		super();
		xmlDoc.setLeafValue("message.operationId", "GetTimeReply");
	}
	
	public MockupGetTimeReplyMessage(String xml) throws Exception {
		super(xml);
	}
	
	public void setDataId(String dataId) {
		xmlDoc.setLeafValue("message.dataId", dataId);
	}
	
	public String getDataId() {
		return xmlDoc.getLeafValue("message.dataId");
	}

	public void setTimeMeasure(Long time) {
		xmlDoc.setLeafValue("message.time.measure", time.toString());
	}
	
	public Long getTimeMeasure() {
		return Long.parseLong(xmlDoc.getLeafValue("message.time.measure"));
	}

	public void addTimeUnit(String unit) {
		xmlDoc.setLeafValue("message.time.unit", unit);
	}
	
	public String getTimeUnit() {
		return xmlDoc.getLeafValue("message.time.unit");
	}

	public void setPillarId(String pillarId) {
		xmlDoc.setLeafValue("message.pillarId", pillarId);
	}
	
	public String getPillarId() {
		return xmlDoc.getLeafValue("message.pillarId");
	}

}
