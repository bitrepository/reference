package org.bitrepository.integrityservice.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to carry and store active, failed and finished contributors for keeping track of who to collect from.
 * 
 * The class handles the state of contributors:
 *  - Active, a contributor that have not had all its information collected. 
 *  - Failed, a contributor that have failed delivering information
 *  - Finished, a contributor that have delivered all information  
 */
public class IntegrityContributors {

    Set<String> activeContributors = Collections.synchronizedSet(new HashSet<>());
    Set<String> finishedContributors = Collections.synchronizedSet(new HashSet<>());
    Set<String> failedContributors = Collections.synchronizedSet(new HashSet<>());
    
    /**
     * Constructor, initializes the set of active contributors 
     * @param contributors The full set of contributors, all are set as active initially 
     */
    public IntegrityContributors(Collection<String> contributors) {
        activeContributors.addAll(contributors);
    }
    
    /**
     * Move all finished contributors to active contributors 
     */
    public void reloadActiveContributors() {
        activeContributors.addAll(finishedContributors);
        finishedContributors.clear();
    }
    
    /**
     * Get the set of finished contributors 
     */
    public Set<String> getFinishedContributors() {
        return new HashSet<>(finishedContributors);
    }
    
    /**
     *  Get the set of failed contributors
     */
    public Set<String> getFailedContributors() {
        return new HashSet<>(failedContributors);
    }
    
    /**
     * Get the set of active contributors 
     */
    public Set<String> getActiveContributors() {
        return new HashSet<>(activeContributors);
    }
    
    /**
     * Mark an contributor as failed 
     */
    public void failContributor(String contributor) {
        failedContributors.add(contributor);
        activeContributors.remove(contributor);
    }
    
    /**
     * Mark an contributor as finished 
     */
    public void finishContributor(String contributor) {
        finishedContributors.add(contributor);
        activeContributors.remove(contributor);
    }
    
}
