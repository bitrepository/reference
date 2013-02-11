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
package org.bitrepository.integrityservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.CompleteIntegrityCheck;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.service.contributor.SimpleContributorMediator;
import org.bitrepository.service.scheduler.ServiceScheduler;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple integrity service.
 */
public class SimpleIntegrityService implements IntegrityService {
    /** The log.*/
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The scheduler. */
    private final ServiceScheduler scheduler;
    /** The information collector. */
    private final IntegrityInformationCollector collector;
    /** The cache.*/
    private final IntegrityModel cache;
    /** The integrity checker.*/
    private final IntegrityChecker checker;
    /** The settings. */
    private final Settings settings;
    /** The dispatcher of alarms.*/
    private final IntegrityAlerter alerter;
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    /** Provides GetStatus and GetAuditTrails functionality. */
    private final ContributorMediator contributor;
    /** The messagebus for communication.*/
    private final MessageBus messageBus;
    /** The list of available workflows.*/
    private final List<Workflow> workflows = new ArrayList<Workflow>();
    
    /**
     * Constructor.
     * @param settings The settings for the service.
     */
    public SimpleIntegrityService(IntegrityModel model, ServiceScheduler scheduler, IntegrityChecker checker, 
            IntegrityAlerter alerter, IntegrityInformationCollector collector, AuditTrailManager auditManager, 
            Settings settings, MessageBus messageBus) {
        this.settings = settings;
        this.messageBus = messageBus;
        this.cache = model;
        this.scheduler = scheduler;
        this.checker = checker;
        this.alerter = alerter;
        this.collector = collector;
        this.auditManager = auditManager;
        
        this.contributor = new SimpleContributorMediator(messageBus, settings, auditManager);
        contributor.start();
        
        initialiseWorkflows();
    }
    
    @Override
    public Collection<Workflow> getAllWorkflows() {
        List<Workflow> res = new ArrayList<Workflow>(workflows);
        return res;
    }

    @Override
    public void scheduleWorkflow(Workflow workflow, long timeBetweenRuns) {
        scheduler.scheduleWorkflow(workflow, workflow.getClass().getSimpleName(), timeBetweenRuns);
        if(!workflows.contains(workflow)) {
            workflows.add(workflow);
        }
    }
    
    @Override
    public Collection<WorkflowTimerTask> getScheduledWorkflows() {
        return scheduler.getScheduledWorkflows();
    }
    

    @Override
    public List<String> getChecksumErrors(String collectionID, String pillarID,
            int firstID, int lastID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getMissingFiles(String collectionID, String pillarID,
            int firstID, int lastID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllFileIDs(String collectionID, String pillarID,
            int firstID, int lastID) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public long getNumberOfFiles(String pillarId) {
        return cache.getNumberOfFiles(pillarId);
    }
    
    @Override
    public long getNumberOfMissingFiles(String pillarId) {
        return cache.getNumberOfMissingFiles(pillarId);
    }
    
    @Override
    public long getNumberOfChecksumErrors(String pillarId) {
        return cache.getNumberOfChecksumErrors(pillarId);
    }

    @Override
    public void start() {
        //Nothing to do.
    }

    @Override
    public void shutdown() {
        if(messageBus != null) {
            try {
                messageBus.close();
            } catch (Exception e) {
                log.warn("Encountered issues when closing down the messagebus.", e);
            }
        }
        if(contributor != null) {
            contributor.close();
        }
    }
    
    /**
     * Initialises the workflows.
     */
    private void initialiseWorkflows() {
        Workflow w1 = new CompleteIntegrityCheck(settings, collector, cache, checker, alerter, auditManager);
        workflows.add(w1);
    }

    @Override
    public List<String> getPillarList() {
        return settings.getCollectionSettings().getClientSettings().getPillarIDs();
    }
}
