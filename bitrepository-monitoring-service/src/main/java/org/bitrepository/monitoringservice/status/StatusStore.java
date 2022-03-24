/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice.status;

import org.bitrepository.bitrepositoryelements.ResultingStatus;

import java.util.Map;

/**
 * Interface for the storage of statuses.
 */
public interface StatusStore {
    /**
     * Updates the status of a given component.
     *
     * @param componentID The id of the component to update.
     * @param status      The resulting status for the given component.
     */
    void updateStatus(String componentID, ResultingStatus status);

    /**
     * Tells the store that new statuses have been requested from the components.
     * This should increase the 'missingReplies' for each component, and whenever the component delivers its
     * reply, then the 'missingReplies' are set to zero again.
     */
    void updateReplyCounts();

    /**
     * @return The mapping between components and their status.
     */
    Map<String, ComponentStatus> getStatusMap();
}
