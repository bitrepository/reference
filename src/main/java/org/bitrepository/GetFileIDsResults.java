package org.bitrepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;

public class GetFileIDsResults {

	private boolean done = false;
	private boolean failed = false;
	/** Maps fileID to list of pillarIDs*/
	private Map<String, List<String>> results;
	private List<String> pillarList;
		
	public GetFileIDsResults(List<String> pillars) {
		pillarList = pillars;
	}
	
	public void addResultsFromPillar(String pillarID, ResultingFileIDs fileIDs) {
		if(results == null) {
			results = new HashMap<String, List<String>>();	
		}
		
		List<FileIDsDataItem> items = fileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem();
		for(FileIDsDataItem item : items) {
			synchronized (this) {
				if(results.containsKey(item.getFileID())) {
					results.get(item.getFileID()).add(pillarID);
				} else {
					List<String> value = new ArrayList<String>();
					value.add(pillarID);
					results.put(item.getFileID(), value);
				}	
			}
			
		}
	}
	
	public synchronized boolean isDone() {
		return done;
	}
	
	public synchronized void done() {
		done = true;
	}
	
	public synchronized void failed() {
		failed = true;
	}
	
	public synchronized boolean hasFailed() {
		return failed;
	}
	
	public Map<String, List<String>> getResults() {
		return results;
	}
	
	public List<String> getPillarList() {
		return pillarList;
	}
}
