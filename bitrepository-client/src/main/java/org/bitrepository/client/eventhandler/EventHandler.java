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

/**
 * Defines the interface for clients to follow the sequence of events unfolding after a operation on the BitRepository
 * has been requested.
 * <p>
 * To follow the status of a request, create an <code>EventHandler</code> and supply the
 * EventHandler when call the relevant method on the client.
 */
public interface EventHandler {
    /**
     * Called by the client each time a event is detected relevant for the active operation
     *
     * @param event Contains information about the concrete event.
     */
    void handleEvent(OperationEvent event);
}
