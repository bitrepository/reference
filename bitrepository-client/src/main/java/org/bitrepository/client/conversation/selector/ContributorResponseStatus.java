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

import org.bitrepository.client.exceptions.UnexpectedResponseException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Models the response state for a given set of components */
public class ContributorResponseStatus {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Set<String> componentsWhichShouldRespond;
    private final Set<String> componentsWithOutstandingResponse;

    /**
     * Use for identify response bookkeeping.
     * @param componentsWhichShouldRespond An array of componentIDs specifying which components are expected to respond 
     */
    public ContributorResponseStatus(Collection<String> componentsWhichShouldRespond) {
        this.componentsWhichShouldRespond = new HashSet<String>(componentsWhichShouldRespond);
        this.componentsWithOutstandingResponse = new HashSet<String>(componentsWhichShouldRespond);
    }
    
    /**
     * Use for operation response bookkeeping.
     * @param componentssWhichShouldRespond An array of selected component specifying which components are expected to respond. 
     */
    public ContributorResponseStatus(SelectedComponentInfo[] componentssWhichShouldRespond) {
        this.componentsWhichShouldRespond = new HashSet<String>();
        this.componentsWithOutstandingResponse = new HashSet<String>();
        for (SelectedComponentInfo pillar: componentssWhichShouldRespond) {
            this.componentsWhichShouldRespond.add(pillar.getID());
            this.componentsWithOutstandingResponse.add(pillar.getID());
        }
    }

    /**
     * Maintains the bookkeeping regarding which components have responded. 
     * 
     * @throws UnexpectedResponseException This can mean: <ol>
     * <li>A null componentID</li>
     * <li>A response has already been received from this component</li>
     * <li>No response was expected from this component</li>
     * </ol>
     *  
     */
    public final void responseReceived(String componentId) throws UnexpectedResponseException {
        if (componentId == null) {
            throw new UnexpectedResponseException("Received response with null componentID");
        } else if (componentsWithOutstandingResponse.contains(componentId)) {
            componentsWithOutstandingResponse.remove(componentId);
        } else if (componentsWhichShouldRespond.contains(componentId)) {
            throw new UnexpectedResponseException("Received more than one response from component " + componentId);
        } else {
            log.debug("Received response from irrelevant component");
        }
    }

    /** Returns a list of components where a identify response hasen't been received. */ 
    public String[] getOutstandComponents() {
        return componentsWithOutstandingResponse.toArray(new String[componentsWithOutstandingResponse.size()]);
    }

    /**
     * @return true all components have responded.
     */
    public final boolean haveAllComponentsResponded() {
        return componentsWithOutstandingResponse.isEmpty();
    }

    /**
     * @return The set of components which should respond to the identification request.
     */
    public final Set<String> getComponentsWhichShouldRespond() {
        return componentsWhichShouldRespond;
    }
}
