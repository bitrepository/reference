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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.flow.UnexpectedResponseException;

public abstract class AbstractSinglePillarSelector implements SinglePillarSelector {
    private final Set<String> pillarsWhichShouldRespond;
    private final Set<String> pillarsWithOutstandingResponse;
    /** The ID of the selected pillar */
    protected String pillarID = null;
    /** The topic for communication with the selected pillar */
    protected String pillarTopic = null;
    
    /** A the information about failure to find a pillar is stored in this exception, and is throw when the 
     * {@link AbstractSinglePillarSelector#isFinished()} method is called.
     */
    protected UnableToFinishException failureException = null;
    protected boolean finished;
    
    /** 
     * Must be called by the implementing classes.
     * @param pillarsWhichShouldRespond Array containing the IDs of the pillars which should respond.
     */
    protected AbstractSinglePillarSelector(String[] pillarsWhichShouldRespond) {
        this.pillarsWhichShouldRespond = new HashSet<String>(Arrays.asList(pillarsWhichShouldRespond));
        this.pillarsWithOutstandingResponse = new HashSet<String>(Arrays.asList(pillarsWhichShouldRespond));
    }
    
    /** Return the ID of the pillar chosen by this selector if finished. If unfinished null is returned */
    public String getIDForSelectedPillar() {
        return pillarID;
    }

    /** If finished return the topic for sending messages to the pillar chosen by this selector. 
     * If unfinished null is returned 
     */
    public String getDestinationForSelectedPillar() {
        return pillarTopic;
    }   

    /**
     * Maintains the bookkeeping regarding which pillars have responded. When all the indicated pillars have replied, 
     * the selection process is considered finished. 
     * 
     * @throws UnexpectedResponseException This can mean: <ol>
     * <li>A null pillarID</li>
     * <li>A response has already been received from this pillar</li>
     * <li>No response was expected from this pillar</li>
     * </ol>
     *  
     */
    protected final void responseReceived(String pillarId) throws UnexpectedResponseException {
        if (pillarId == null) {
            throw new UnexpectedResponseException("Received response with null pillarID");
        } else if (pillarsWithOutstandingResponse.contains(pillarId)) {
            pillarsWithOutstandingResponse.remove(pillarId);
        } else if (pillarsWhichShouldRespond.contains(pillarId)) {
                throw new UnexpectedResponseException("Received more than one response from pillar " + pillarId);
        } else {
            throw new UnexpectedResponseException("Received response from unknown pillar " + pillarId);  
        }
        if (pillarsWithOutstandingResponse.size() == 0) {
            finished = true;
        }
    }
    
    /**
     * Return true if this <code>Selector</code> has chosen a pillar, else false. 
     * @throws UnableToFinishException Thrown if the selector is unable to find a pillar. 
     */
    public final boolean isFinished() throws UnableToFinishException {
        if (failureException != null ) {
            throw failureException;
        } else {
            return finished; 
        }
    }
}
