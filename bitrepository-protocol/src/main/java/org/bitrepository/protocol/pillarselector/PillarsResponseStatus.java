/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.pillarselector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
/** Models the response state for a given set of pillars */
public class PillarsResponseStatus {
    private final Set<String> pillarsWhichShouldRespond;
    private final Set<String> pillarsWithOutstandingResponse;

    /**
     * Use for identify response bookkeeping.
     * @param pillarsWhichShouldRespond An array of pillar IDs specifying which pillars are expected to respond 
     */
    public PillarsResponseStatus(Collection<String> pillarsWhichShouldRespond) {
        this.pillarsWhichShouldRespond = new HashSet<String>(pillarsWhichShouldRespond);
        this.pillarsWithOutstandingResponse = new HashSet<String>(pillarsWhichShouldRespond);
    }
    
    /**
     * Use for operation response bookkeeping.
     * @param pillarsWhichShouldRespond An array of selected pillar specifying which pillars are expected to respond. 
     */
    public PillarsResponseStatus(SelectedPillarInfo[] pillarsWhichShouldRespond) {
        this.pillarsWhichShouldRespond = new HashSet<String>();
        this.pillarsWithOutstandingResponse = new HashSet<String>();
        for (SelectedPillarInfo pillar: pillarsWhichShouldRespond) {
            this.pillarsWhichShouldRespond.add(pillar.getID());
            this.pillarsWithOutstandingResponse.add(pillar.getID());
        }
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
        return pillarsWithOutstandingResponse.toArray(new String[pillarsWithOutstandingResponse.size()]);
    }

    /**
     * Return true all pillars have responded.
     */
    public final boolean haveAllPillarResponded() {
        return pillarsWithOutstandingResponse.isEmpty();
    }
}
