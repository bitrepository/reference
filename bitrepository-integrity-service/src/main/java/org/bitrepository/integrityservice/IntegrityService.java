/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice;

import java.util.Collection;

import org.bitrepository.integrityservice.workflow.Workflow;

public interface IntegrityService {
    /**
     * Retrieves all the scheduled tasks in the system, which are running.
     * @return The names of the tasks, which are scheduled by the system.
     */
    Collection<Workflow> getWorkflows();
    
    /**
     * @param pillarId The pillar which has the files.
     * @return The number of files on the given pillar.
     */
    long getNumberOfFiles(String pillarId);
    
    /**
     * @param pillarId The pillar which might be missing some files.
     * @return The number of files missing for the given pillar.
     */
    long getNumberOfMissingFiles(String pillarId);
    
    /**
     * @param pillarId The pillar which might contain files with checksum error.
     * @return The number of files with checksum error at the given pillar.
     */
    long getNumberOfChecksumErrors(String pillarId);

}
