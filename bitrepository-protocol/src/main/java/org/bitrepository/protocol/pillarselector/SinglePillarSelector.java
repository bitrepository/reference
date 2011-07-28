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

import org.bitrepository.common.exceptions.UnableToFinishException;

/** Can be used to select a single pillar to run an operation on by handling the identify responses. <p>
 * The algorithm for selecting the pillar is implemented in the concrete classes.
 */
public interface SinglePillarSelector {

    /** Return the ID of the pillar chosen by this selector.*/
    public String getIDForSelectedPillar();

    /** If finished return the topic for sending messages to the pillar chosen by this selector. 
     * If unfinished null is returned 
     */
    public String getDestinationForSelectedPillar();
    
    /**
     * Returns true if all the need information to select a pillar has been processed. <p>
     * 
     * Note that a pillar might have been selected before finished, but the selection might change until the selector 
     * has finished.
     * @throws UnableToFinishException Indicates that the selector was unable to find a pillar. 
     */
    public boolean isFinished() throws UnableToFinishException;
}
