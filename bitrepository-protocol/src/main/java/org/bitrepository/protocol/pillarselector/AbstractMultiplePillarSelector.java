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

/** Defines the implementation of an <code>SinglePillarSelector</code>
 *  
 * The selection algorithm itself needs to be defined in a concrete subclass.
 */
public abstract class AbstractMultiplePillarSelector implements SinglePillarSelector {
    /** The ID of the selected pillar */
    protected String pillarID = null;
    /** The topic for communication with the selected pillar */
    protected String pillarTopic = null;    

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
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": pillarID=" + pillarID
                + ", pillarTopic=" + pillarTopic;
    }
}
