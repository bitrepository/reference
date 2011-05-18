/*
 * #%L
 * Bitrepository Integrity Client
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
package org.bitrepository.integrityclient.collection;

/**
 * Interface for defining a trigger for collecting events.
 *
 * Implementations should trigger collection of integrity information using an
 * {@link IntegrityInformationCollector}.
 *
 * Conditions for the trigger should pull on configuration combined with data from the
 * {@link org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage}.
 */
public interface Trigger {
    /**
     * Whether the trigger triggers an event. Note, this is informational only, due to race conditions,
     * no event may be available when actually requesting the trigger.
     * This method will should be implemented to be fairly fast, and not require too many resources,
     * since it can be called quite frequently from the scheduler.
     *
     * @return True if this triggers an event. False otherwise.
     */
    boolean isTriggered();

    /**
     * Trigger a collection event.
     *
     * This should trigger the collection of information using an {@link IntegrityInformationCollector}.
     *
     * May do nothing, if triggering conditions do not apply.
     */
    void trigger();
}
