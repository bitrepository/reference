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
package org.bitrepository.client.eventhandler;


import java.util.List;

/**
 * Indicates an operation has failed to complete.
 */
public class OperationFailedEvent extends AbstractOperationEvent {
    private final List<ContributorEvent> componentResults;

    /**
     * @param collectionID     the collection ID
     * @param info             See {@link #getInfo()}
     * @param componentResults The aggregated list of <code>COMPONENT_COMPLETE</code> events generated during
     *                         the operation.
     */
    public OperationFailedEvent(String collectionID, String info, List<ContributorEvent> componentResults) {
        setEventType(OperationEventType.FAILED);
        setInfo(info);
        setCollectionID(collectionID);
        this.componentResults = componentResults;
    }

    /**
     * @return the results for the individual components contributing to this operation. The list is just
     * aggregation of the <code>COMPONENT_COMPLETE</code> events generated during the operation.
     */
    public List<ContributorEvent> getComponentResults() {
        return componentResults;
    }

    @Override
    public String additionalInfo() {
        return "";
    }

}
