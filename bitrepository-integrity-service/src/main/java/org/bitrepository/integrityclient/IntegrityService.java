/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityclient;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.workflow.Workflow;

public class IntegrityService {
    private final SimpleIntegrityService service;
    private final Settings settings;
    
    public IntegrityService(SimpleIntegrityService simpleIntegrityService, Settings settings) {
        this.service = simpleIntegrityService;
        this.settings = settings;
    }
    
    public List<String> getPillarList() {
        return settings.getCollectionSettings().getClientSettings().getPillarIDs();    	
    }
    
    public long getNumberOfFiles(String pillarID) {
        return service.getNumberOfFiles(pillarID);
    }
    
    public long getNumberOfMissingFiles(String pillarID) {
        return service.getNumberOfMissingFiles(pillarID);
    }
    
    public Date getDateForLastFileIDUpdate(String pillarID) {
        return new Date(0);
    }
    
    public long getNumberOfChecksumErrors(String pillarID) {
        return service.getNumberOfChecksumErrors(pillarID);
    }
    
    public Date getDateForLastChecksumUpdate(String pillarID) {
        return new Date(0);
    }
    
    public long getSchedulingInterval() {
        return settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval();
    }
    
    public Collection<Workflow> getWorkflows() {
        return service.getScheduledTasks();
    }
    
    public void close() {
        service.close();
    }
    
}
