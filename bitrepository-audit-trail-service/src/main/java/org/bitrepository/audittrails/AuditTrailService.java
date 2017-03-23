/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.ConversationBasedAuditTrailClient;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.preserver.AuditTrailPreserver;
import org.bitrepository.audittrails.preserver.LocalAuditTrailPreserver;
import org.bitrepository.audittrails.store.AuditTrailDatabaseManager;
import org.bitrepository.audittrails.store.AuditTrailServiceDAO;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.putfile.ConversationBasedPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.audittrails.webservice.CollectorInfo;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.service.contributor.SimpleContributorMediator;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.AuditTrailServiceSettings;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

/**
 * Class to expose the functionality of the AuditTrailService. 
 * Aggregates the needed classes.   
 */
public class AuditTrailService implements LifeCycledService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The storage of audit trail information.*/
    private final AuditTrailStore store;
    /** The collector of new audit trails.*/
    private final AuditTrailCollector collector;
    /** The mediator for handling the messages.*/
    private final ContributorMediator contributorMediator;
    /** The settings.*/
    private final Settings settings;
    private final MessageBus messageBus;
    private final CollectionBasedConversationMediator conversationMediator;
    private final LocalAuditTrailPreserver preserver;


    public AuditTrailService(Settings settings, SecurityManager securityManager) {

        this.settings = settings;
        messageBus = MessageBusManager.createMessageBus(settings, securityManager);
        conversationMediator = new CollectionBasedConversationMediator(settings, messageBus);

        FileExchange fileExchange = ProtocolComponentFactory.createFileExchange(settings);
        contributorMediator = new SimpleContributorMediator(messageBus, settings, null, fileExchange);


        PutFileClient putClient = new ConversationBasedPutFileClient(messageBus, conversationMediator, this.settings, "audit-trail-preserver");

        AuditTrailServiceSettings serviceSettings = settings.getReferenceSettings().getAuditTrailServiceSettings();
        DatabaseSpecifics auditTrailServiceDatabase = serviceSettings.getAuditTrailServiceDatabase();

        DatabaseManager auditTrailServiceDatabaseManager = new AuditTrailDatabaseManager(auditTrailServiceDatabase);
        store = new AuditTrailServiceDAO(auditTrailServiceDatabaseManager);

        AuditTrailClient client = new ConversationBasedAuditTrailClient(settings, conversationMediator, messageBus, serviceSettings.getID());
        AlarmDispatcher alarmDispatcher = new AlarmDispatcher(settings, messageBus);
        collector = new AuditTrailCollector(settings, client, store, alarmDispatcher);


        if (serviceSettings.isSetAuditTrailPreservation()) {
            preserver = new LocalAuditTrailPreserver(
                    settings, store, putClient, fileExchange);
            preserver.start();
        } else {
            preserver = null;
            log.info("Audit trail preservation disabled, no configuration defined.");
        }
    }

    /**
     * Retrieve an iterator to all AuditTrailEvents matching the criteria from the parameters.
     * All parameters are allowed to be null, meaning that the parameter imposes no restriction on the result
     * @param fromDate Restrict the results to only provide events after this point in time
     * @param toDate Restrict the results to only provide events up till this point in time
     * @param fileID Restrict the results to only be about this fileID
     * @param collectionID restrict the results to this collection
     * @param reportingComponent Restrict the results to only be reported by this component
     * @param actor Restrict the results to only be events caused by this actor
     * @param action Restrict the results to only be about this type of action
     * @param fingerprint the fingerprint
     * @param operationID Restrict the results to only this operationID
     * @return an iterator to all AuditTrailEvents matching the criteria from the parameters
     */
    public AuditEventIterator queryAuditTrailEventsByIterator(Date fromDate, Date toDate, String fileID, 
            String collectionID, String reportingComponent, String actor, FileAction action, 
            String fingerprint, String operationID) {
        return store.getAuditTrailsByIterator(fileID, collectionID, reportingComponent, null, null, actor, action, 
                fromDate, toDate, fingerprint, operationID);
    }

    /**
     * Collects all the newest audit trails from the given collection.
     * TODO this currently calls all collections. It should only call a specified collection, which should be given
     * as argument.
     */
    public void collectAuditTrails() {
        for(org.bitrepository.settings.repositorysettings.Collection c 
                : settings.getRepositorySettings().getCollections().getCollection()) {
            collector.collectNewestAudits(c.getID());
        }
    }
    
    /**
     * Get the list of {@link CollectorInfo} 
     * @return The list of CollectorInfo
     */
    public List<CollectorInfo> getCollectorInfos() {
        List<CollectorInfo> infos = new ArrayList<CollectorInfo>();
        for(org.bitrepository.settings.repositorysettings.Collection c 
                : settings.getRepositorySettings().getCollections().getCollection()) {
            infos.add(collector.getCollectorInfo(c.getID()));
        }
        
        return infos;
    }
    
    /**
     *  Get the list of known contributors from the backend. 
     *  @return The list of known contributors 
     */
    public List<String> getContributors() {
        return store.getKnownContributors();
    }

    @Override
    public void start() {
        contributorMediator.start();
    }

    @Override
    public void shutdown() {
        collector.close();
        store.close();
        contributorMediator.shutdown();
        conversationMediator.shutdown();
        if (preserver != null) {
            preserver.close();
        }
        try {
            messageBus.close();
        } catch (JMSException e) {
            throw new Error(e);
        }
    }
}
