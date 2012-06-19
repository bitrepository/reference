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
package org.bitrepository.integrityservice.scheduler.workflow;


/**
 * Interface for defining a workflow.
 * A workflow collects data or validate data, or a combination of these two. 
 * 
 * Implementations for a workflow for collection of integrity information should use an
 * {@link org.bitrepository.integrityservice.collector.IntegrityInformationCollector}.
 *
 * Conditions for the trigger should pull on configuration combined with data from the
 * {@link org.bitrepository.integrityservice.cache.IntegrityModel}.
 * 
 * Implementations for a workflow which checks the integrity of the data should use an
 * {@link org.bitrepository.integrityservice.checking.IntegrityChecker}
 */
public interface Workflow {
    void start();
    String currentState();
}
