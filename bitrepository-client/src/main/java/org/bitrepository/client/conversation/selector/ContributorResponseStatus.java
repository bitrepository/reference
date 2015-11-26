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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.utils.MessageUtils;
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
     * Maintains the bookkeeping regarding which components have responded.
     * @param response the response that was received
     */
    public final void responseReceived(MessageResponse response) {
        if (MessageUtils.isEndMessageForPrimitive(response)) {
            String componentID = response.getFrom();
            log.debug("Received response from: " + componentID);
            if (componentsWithOutstandingResponse.contains(componentID)) {
                componentsWithOutstandingResponse.remove(componentID);
            } else if (!componentsWithOutstandingResponse.contains(componentID) &&
                    componentsWhichShouldRespond.contains(componentID)) {
                log.debug("Received more than one response from component " + componentID);
            } else {
                log.debug("Received response from irrelevant component " + componentID);
            }
        }
    }

    /** Get components where a identify response hasn't been received.
     * @return a list of components where a identify response hasn't been received.
     * */
    public Collection<String> getOutstandComponents() {
        return Collections.unmodifiableCollection(componentsWithOutstandingResponse);
    }

    /**
     * @return true if all components have responded. False otherwise
     */
    public final boolean haveAllComponentsResponded() {
        log.debug("Expected contributors: " + componentsWhichShouldRespond + ", components that have not answered: " + componentsWithOutstandingResponse);
        return componentsWithOutstandingResponse.isEmpty();
    }

    /**
     * @return The set of components which should respond to the identification request.
     */
    public final Set<String> getComponentsWhichShouldRespond() {
        return componentsWhichShouldRespond;
    }
}
