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
package org.bitrepository.integrityclient.workflow.scheduler;

import org.bitrepository.integrityclient.workflow.Workflow;

/**
 * Abstract trigger, that triggers at given interval.
 * Trigger will run the run() method, if triggered.
 */
public abstract class IntervalTrigger implements Workflow, Runnable {
    /** Time of last event, truncated to two seconds. */
    private long time; //no see
    /** The interval between triggers. */
    private final long interval;

    /**
     * Initialise trigger.
     * @param interval The interval between triggering events in milliseconds.
     */
    public IntervalTrigger(long interval) {
        long now = System.currentTimeMillis();
        this.interval = interval;
        time = now - (now % this.interval);
    }

    @Override
    public synchronized boolean isTriggered() {
        return ((System.currentTimeMillis() - time) > interval);
    }

    @Override
    public void trigger() {
        synchronized (this) {
            if (!isTriggered()) {
                return;
            }
            long now = System.currentTimeMillis();
            time = now - (now % interval);
        }
        run();
    }
}
