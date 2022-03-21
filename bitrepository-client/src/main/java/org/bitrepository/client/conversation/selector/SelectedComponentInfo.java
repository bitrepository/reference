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

/**
 * Container for information about a pillar which as been identified and are marked as selected for a request.
 */
public class SelectedComponentInfo {
    /**
     * The ID of the selected pillar
     */
    protected final String componentID;
    /**
     * The topic for communication with the selected pillar
     */
    protected final String componentTopic;

    /**
     * @param componentID    The ID of the pillar
     * @param componentTopic the topic for communication with the selected pillar
     */
    public SelectedComponentInfo(String componentID, String componentTopic) {
        super();
        this.componentID = componentID;
        this.componentTopic = componentTopic;
    }

    /**
     * @return The ID of the pillar chosen by this selector if finished. If unfinished null is returned
     */
    public String getID() {
        return componentID;
    }

    /**
     * @return If finished return the topic for sending messages to the pillar chosen by this selector.
     * If unfinished null is returned
     */
    public String getDestination() {
        return componentTopic;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": componentID=" + componentID + ", componentTopic=" + componentTopic;
    }
}
