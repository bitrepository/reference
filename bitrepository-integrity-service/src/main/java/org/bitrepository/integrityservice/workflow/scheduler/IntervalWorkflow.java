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
package org.bitrepository.integrityservice.workflow.scheduler;

import java.util.Date;

import org.bitrepository.integrityservice.workflow.Workflow;

/**
 * Abstract trigger, that triggers at given interval.
 * Trigger will run the run() method, if triggered.
 */
public abstract class IntervalWorkflow implements Workflow {
    /** The date for the next run of the workflow.*/
    private Date nextRun;
    /** The name of the workflow.*/
    private final String name;
    /** The interval between triggers. */
    private final long interval;

    /**
     * Initialise trigger.
     * @param interval The interval between triggering events in milliseconds.
     * @param name The name of this workflow.
     */
    public IntervalWorkflow(long interval, String name) {
        this.interval = interval;
        this.name = name;
        nextRun = new Date();
    }
    
    @Override
    public Date getNextRun() {
        return new Date(nextRun.getTime());
    }
    
    @Override
    public long getTimeBetweenRuns() {
        return interval;
    }
    
    @Override
    public void trigger() {
        nextRun = new Date(System.currentTimeMillis() + interval);
        runWorkflow();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Performs the given workflow.
     */
    public abstract void runWorkflow();
}
