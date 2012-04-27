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
package org.bitrepository.integrityservice.web;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.IntegrityService;
import org.bitrepository.integrityservice.SimpleIntegrityService;
import org.bitrepository.integrityservice.workflow.Workflow;
import org.bitrepository.service.LifeCycledService;

import java.util.Collection;
import java.util.List;

/**
 * The IntegrityService wrapped into the interface for a web server.
 */
public class IntegrityServiceWebInterface implements IntegrityService, LifeCycledService {
    /** The wrapped IntegrityService.*/
    private final SimpleIntegrityService service;
    /** The settings for the wrapped integrity service.*/
    private final Settings settings;
    
    /**
     * Constructor.
     * @param simpleIntegrityService The service to wrap.
     * @param settings The settings for the wrapped service.
     */
    public IntegrityServiceWebInterface(SimpleIntegrityService simpleIntegrityService, Settings settings) {
        this.service = simpleIntegrityService;
        this.settings = settings;
    }
    
    /**
     * @return The list of pillars in the settings.
     */
    public List<String> getPillarList() {
        return settings.getCollectionSettings().getClientSettings().getPillarIDs();    	
    }
    
    /**
     * @return The interval between schedulings.
     */
    public long getSchedulingInterval() {
        return settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval();
    }
    
    @Override
    public long getNumberOfFiles(String pillarID) {
        return service.getNumberOfFiles(pillarID);
    }
    
    @Override
    public long getNumberOfMissingFiles(String pillarID) {
        return service.getNumberOfMissingFiles(pillarID);
    }
    
    @Override
    public long getNumberOfChecksumErrors(String pillarID) {
        return service.getNumberOfChecksumErrors(pillarID);
    }
    
    @Override
    public Collection<Workflow> getWorkflows() {
        return service.getWorkflows();
    }

    @Override
    public void start() {
        //Nothing to do
    }

    @Override
    public void shutdown() {
        service.shutdown();
    }
}
