package org.bitrepository.integrityservice.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to carry and store active, failed and finished contributors for keeping track of who to collect from.
 * The class also handles retries, i.e. contributors are not failed unless they fail a number of times in a row.
 * 
 * The class handles the state of contributors:
 *  - Active, a contributor that have not had all its information collected. 
 *  - Failed, a contributor that have failed delivering information
 *  - Finished, a contributor that have delivered all information  
 */
public class IntegrityContributors {

    Set<String> finishedContributors = Collections.synchronizedSet(new HashSet<>());
    Set<String> failedContributors = Collections.synchronizedSet(new HashSet<>());
    /** Mapping between active contributors and how many times they each have failed in a row */
    Map<String, Integer> activeContributors = Collections.synchronizedMap(new HashMap<>()); 
    private int maxContributorFailures;
    
    /**
     * Constructor, initializes the set of active contributors 
     * @param contributors The full set of contributors, all are set as active initially
     * @param maxContributorFailures The number of times in a row a contributor must have failed to be marked as failed
     */
    public IntegrityContributors(Collection<String> contributors, int maxContributorFailures) {
        for(String contributor : contributors) {
            activeContributors.put(contributor, 0);
        }
        this.maxContributorFailures = maxContributorFailures;
    }
    
    /**
     * Move all finished contributors to active contributors 
     */
    public synchronized void reloadActiveContributors() {
        for(String contributor : finishedContributors) {
            activeContributors.put(contributor, 0);
        }
        finishedContributors.clear();
    }
    
    /**
     * @return the set of finished contributors
     */
    public Set<String> getFinishedContributors() {
        return new HashSet<>(finishedContributors);
    }
    
    /**
     *  @return the set of failed contributors
     */
    public Set<String> getFailedContributors() {
        return new HashSet<>(failedContributors);
    }
    
    /**
     * @return the set of active contributors
     */
    public Set<String> getActiveContributors() {
        return new HashSet<>(activeContributors.keySet());
    }
    
    /**
     * Mark an contributor as having failed an attempt 
     * @param contributor The contributor which have failed
     */
    public synchronized void failContributor(String contributor) {
        Integer failures = activeContributors.get(contributor);
        if(failures != null) {
            failures++;
            if(failures >= maxContributorFailures) {
                failedContributors.add(contributor);
                activeContributors.remove(contributor);
            } else {
                activeContributors.put(contributor, failures);
            }
            
        }
    }
    
    /**
     * Mark a contributor as having succeeded the last request. 
     * @param contributor The contributor which have succeeded
     */
    public synchronized void succeedContributor(String contributor) {
        if(activeContributors.containsKey(contributor)) {
            activeContributors.put(contributor, 0);
        }
    }
    
    /**
     * Mark an contributor as finished
     * @param contributor The contributor which have finished
     */
    public synchronized void finishContributor(String contributor) {
        if(activeContributors.containsKey(contributor)) {
            finishedContributors.add(contributor);
            activeContributors.remove(contributor);
        }
    }
    
}
