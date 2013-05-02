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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.CollectionStat;
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
    private final Map<String, List<Workflow>> workflows = new HashMap<String, List<Workflow>>();
    
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
    public Collection<Workflow> getAllWorkflows(String collectionID) {
        List<Workflow> res = new ArrayList<Workflow>(workflows.get(collectionID));
        return res;
    }

    @Override
    public void scheduleWorkflow(Workflow workflow, String collectionID, long timeBetweenRuns) {
        scheduler.scheduleWorkflow(workflow, timeBetweenRuns);
        if(!workflows.get(collectionID).contains(workflow)) {
            workflows.get(collectionID).add(workflow);
        }
    }
    
    @Override
    public Collection<WorkflowTimerTask> getScheduledWorkflows(String collectionID) {
        List<WorkflowTimerTask> res = new ArrayList<WorkflowTimerTask>();
        for(WorkflowTimerTask task : scheduler.getScheduledWorkflows()) {
            if(task.getWorkflowID().getCollectionID().equals(collectionID)) {
                res.add(task);
            }
        }
        
        return res;
    }
    
    @Override
    public List<String> getChecksumErrors(String collectionID, String pillarID,
            int firstID, int lastID) {
        return cache.getFilesWithChecksumErrorsAtPillar(pillarID, firstID, lastID, collectionID);
    }

    @Override
    public List<String> getMissingFiles(String collectionID, String pillarID,
            int firstID, int lastID) {
        return cache.getMissingFilesAtPillar(pillarID, firstID, lastID, collectionID);
    }

    @Override
    public List<String> getAllFileIDs(String collectionID, String pillarID,
            int firstID, int lastID) {
        return cache.getFilesOnPillar(pillarID, firstID, lastID, collectionID);
    }
    
    @Override
    public long getNumberOfFiles(String pillarId, String collectionId) {
        return cache.getNumberOfFiles(pillarId, collectionId);
    }
    
    @Override
    public long getNumberOfMissingFiles(String pillarId, String collectionId) {
        return cache.getNumberOfMissingFiles(pillarId, collectionId);
    }
    
    @Override
    public long getNumberOfChecksumErrors(String pillarId, String collectionId) {
        return cache.getNumberOfChecksumErrors(pillarId, collectionId);
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

        if(cache != null) {
            cache.close();
        }
    }
    
    /**
     * Initialises the workflows.
     */
    private void initialiseWorkflows() {
        for(org.bitrepository.settings.repositorysettings.Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            List<Workflow> workflowList = new ArrayList<Workflow>();
            Workflow w1 = new CompleteIntegrityCheck(settings, collector, cache, checker, alerter, auditManager, c.getID());
            workflowList.add(w1);
            workflows.put(c.getID(), workflowList);
        }
        
    }

    @Override
    public List<String> getPillarList(String collectionID) {
        return SettingsUtils.getPillarIDsForCollection(settings, collectionID);
    }

    @Override
    public Date getDateForNewestFileInCollection(String collectionID) {
        return cache.getDateForNewestFileEntryForCollection(collectionID);
    }

    @Override
    public Long getCollectionSize(String collectionID) {
        return cache.getCollectionFileSize(collectionID);
    }

    @Override
    public Long getNumberOfFilesInCollection(String collectionID) {
        return cache.getNumberOfFilesInCollection(collectionID);
    }

    @Override
    public List<CollectionStat> getLatestCollectionStatistics() {
        List<CollectionStat> res = new ArrayList<CollectionStat>();
        for(String collection : SettingsUtils.getAllCollectionsIDs(settings)) {
            List<CollectionStat> stats = cache.getLatestCollectionStat(collection, 1);
            if(!stats.isEmpty()) {
                res.add(stats.get(0));   
            }
        }
        
        return res;
    }
    
    @Override 
    public List<CollectionStat> getCollectionStatisticsHistory(String collectionID, int count) {
        return cache.getLatestCollectionStat(collectionID, count);
    }
    
    @Override
    public List<StatisticsPillarSize> getCurrentPillarsDataSize() {
        List<StatisticsPillarSize> stats = new ArrayList<StatisticsPillarSize>();
        for(String pillar : SettingsUtils.getAllPillarIDs(settings)) {
            StatisticsPillarSize stat = new StatisticsPillarSize();
            Long dataSize = cache.getPillarDataSize(pillar);
            stat.setDataSize(dataSize);
            stat.setPillarID(pillar);
            stats.add(stat);
        }
        return stats;
    }

}
