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

import org.jspecify.annotations.NonNull;

/**
 * Container for information about a pillar which has been identified and marked as selected for a request.
 *
 * @param componentID    The ID of the selected pillar
 * @param componentTopic The topic for communication with the selected pillar
 */
public record SelectedComponentInfo(@NonNull String componentID, @NonNull String componentTopic) {

    /**
     * @return The ID of the pillar chosen by this selector
     * @deprecated Use {@link #componentID()} instead
     */
    @Deprecated(forRemoval = true)
    public @NonNull String getID() {
        return componentID;
    }

    /**
     * @return The topic for sending messages to the pillar chosen by this selector
     * @deprecated Use {@link #componentTopic()} instead
     */
    @Deprecated(forRemoval = true)
    public @NonNull String getDestination() {
        return componentTopic;
    }
}
