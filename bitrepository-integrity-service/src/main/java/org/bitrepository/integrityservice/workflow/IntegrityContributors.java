/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
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
     */
    public IntegrityContributors(Collection<String> contributors, int maxContributorFailures) {
        for(String contributor : contributors) {
            activeContributors.put(contributor, 0);
            this.maxContributorFailures = maxContributorFailures;
        }
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
     */
    public synchronized void succeedContributor(String contributor) {
        if(activeContributors.containsKey(contributor)) {
            activeContributors.put(contributor, 0);
        }
    }
    
    /**
     * Mark an contributor as finished 
     */
    public synchronized void finishContributor(String contributor) {
        if(activeContributors.containsKey(contributor)) {
            finishedContributors.add(contributor);
            activeContributors.remove(contributor);
        }
    }
    
}
