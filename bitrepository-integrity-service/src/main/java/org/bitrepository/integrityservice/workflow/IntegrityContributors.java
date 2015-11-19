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
    public synchronized void reloadActiveContributors() {
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
    public synchronized void failContributor(String contributor) {
        if(activeContributors.remove(contributor)) {
            failedContributors.add(contributor);
        }
        
    }
    
    /**
     * Mark an contributor as finished 
     */
    public synchronized void finishContributor(String contributor) {
        if(activeContributors.remove(contributor)) {
            finishedContributors.add(contributor);    
        }
    }
    
}
