package org.bitrepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;

public class GetChecksumsResults {

	private boolean done = false;
	private boolean failed = false;
	/** Maps fileID to list of pillarIDs*/
	private Map<String, Map<String, String>> results;
		
	public GetChecksumsResults() {
	    results = new HashMap<String, Map<String, String>>();
	}
	
	public void addResultsFromPillar(String pillarID, ResultingChecksums checksums) {
	    List<ChecksumDataForChecksumSpecTYPE> items = checksums.getChecksumDataItems();
	    for(ChecksumDataForChecksumSpecTYPE item : items) {
	        synchronized (this) {
	            if(!results.containsKey(item.getFileID())) {
	                Map<String, String> value = new HashMap<String, String>();
	                value.put(pillarID, item.getChecksumValue());
	                results.put(item.getFileID(), value);
	            } else {
	                results.get(item.getFileID()).put(pillarID, item.getChecksumValue());
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
	
	public Map<String, Map<String, String>> getResults() {
		return results;
	}

}
