package dk.bitmagasin.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class for containing the reply data for the time of a specific dataId.
 * @author jolf
 *
 */
public class DataTime {
	/** The dataId. */
	public String dataId;
	/** The measure of time. */
	public Long timeMeasure;
	/** The units in which the time is counted. */
	public TimeUnits timeUnit;
	
	/**
	 * Constructor.
	 * 
	 * @param id The id for the data.
	 * @param measure The measure for the time.
	 * @param unit The unit for the time.
	 */
	public DataTime(String id, Long measure, TimeUnits unit) {
		this.dataId = id;
		this.timeMeasure = measure;
		this.timeUnit = unit;
	}
	
	/**
	 * Retrieves the data out of a map.
	 * @param content The map with the content of this datamodel. The map must 
	 * contain 'dataId', 'timeMeasure', and 'timeUnit'.
	 */
	public DataTime(Map<String, String> content) {
		this.dataId = content.get("dataId");
		this.timeMeasure = Long.parseLong(content.get("timeMeasure"));
		this.timeUnit = TimeUnits.valueOf(content.get("timeUnit"));
	}
	
	/**
	 * Retrieval of the content of this class as a map.
	 * Used for inserting in the XML documents. 
	 * 
	 * @return This instance returned as a Map. Ready for insert into a XML 
	 * document.
	 */
	public Map<String, String> getAsMap() {
		Map<String, String> res = new HashMap<String, String>();
		res.put("dataId", dataId);
		res.put("timeMeasure", timeMeasure.toString());
		res.put("timeUnit", timeUnit.toString());
		return res;
	}
}
