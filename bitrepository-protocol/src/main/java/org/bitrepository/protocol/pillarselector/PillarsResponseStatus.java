package org.bitrepository.protocol.pillarselector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bitrepository.protocol.flow.UnexpectedResponseException;

public class PillarsResponseStatus {
    private final Set<String> pillarsWhichShouldRespond;
    private final Set<String> pillarsWithOutstandingResponse;
    
    public PillarsResponseStatus(String[] pillarsWhichShouldRespond) {
        this.pillarsWhichShouldRespond = new HashSet<String>(Arrays.asList(pillarsWhichShouldRespond));
        this.pillarsWithOutstandingResponse = new HashSet<String>(Arrays.asList(pillarsWhichShouldRespond));
    }
    
    /**
     * Maintains the bookkeeping regarding which pillars have responded. 
     * 
     * @throws UnexpectedResponseException This can mean: <ol>
     * <li>A null pillarID</li>
     * <li>A response has already been received from this pillar</li>
     * <li>No response was expected from this pillar</li>
     * </ol>
     *  
     */
    public final void responseReceived(String pillarId) throws UnexpectedResponseException {
        if (pillarId == null) {
            throw new UnexpectedResponseException("Received response with null pillarID");
        } else if (pillarsWithOutstandingResponse.contains(pillarId)) {
            pillarsWithOutstandingResponse.remove(pillarId);
        } else if (pillarsWhichShouldRespond.contains(pillarId)) {
                throw new UnexpectedResponseException("Received more than one response from pillar " + pillarId);
        } else {
            throw new UnexpectedResponseException("Received response from unknown pillar " + pillarId);  
        }
    }
    
    /** Returns a list of pillars where a identify response hasen't been received. */ 
    public String[] getOutstandPillars() {
    	return pillarsWithOutstandingResponse.toArray(new String[0]);
    }
    
    /**
     * Return true all pillars have responded.
     */
    public final boolean haveAllPillarResponded() {
    	return pillarsWithOutstandingResponse.size() == 0;
    }
}
