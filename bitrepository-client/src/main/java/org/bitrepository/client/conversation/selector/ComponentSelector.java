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
package org.bitrepository.client.conversation.selector;

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

import java.util.List;

/** Can be used to select a single component to run an operation on by handling the identify responses. <p>
 * The algorithm for selecting the component is implemented in the concrete classes.
 */
public interface ComponentSelector {

    /**
     *
     * @return A string representation of the selected contributors.
     */
    String getContributersAsString();

    /**
     * Method for identifying the components, which needs to be identified for this operation to be finished.
     * @return An array of the IDs of the components which have not yet responded.
     */
    List<String> getOutstandingComponents();
    
    /**
     * Returns true if all the need information to select a pillar has been processed. <p>
     * 
     * Note that a component might have been selected before finished, but the selection might change until the selector 
     * has finished.
     * @throws UnableToFinishException Indicates that the selector was unable to find a component. 
     */
    boolean isFinished() throws UnableToFinishException;

    /**
     * @return <code>true</code> if any components have been selected, else <code>false</code>.
     */
    boolean hasSelectedComponent();

    /**
     * @param response The response to handle.
     */
    void processResponse(MessageResponse response)  throws UnexpectedResponseException;
}
