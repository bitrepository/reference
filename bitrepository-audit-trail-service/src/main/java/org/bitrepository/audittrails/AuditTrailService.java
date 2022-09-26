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

import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.preserver.AuditTrailPreserver;
import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.audittrails.webservice.CollectorInfo;
import org.bitrepository.audittrails.webservice.PreservationInfo;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.service.contributor.ContributorMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to expose the functionality of the AuditTrailService.
 * Aggregates the needed classes.
 */
public class AuditTrailService implements LifeCycledService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AuditTrailStore store;
    private final AuditTrailCollector collector;
    private final AuditTrailPreserver preserver;
    private final ContributorMediator mediator;
    private final Settings settings;

    /**
     * Constructor for audit trail service.
     *
     * @param store     The store for the audit trail data.
     * @param collector The collector of new audit trail data.
     * @param preserver The preserver for saving collected audit trail data to a collection.
     *                  Provide non-null preserver for enabling preservation.
     * @param mediator  The mediator for the communication of this contributor.
     * @param settings  The AuditTrailService settings.
     */
    public AuditTrailService(AuditTrailStore store, AuditTrailCollector collector, AuditTrailPreserver preserver,
                             ContributorMediator mediator, Settings settings) {
        ArgumentValidator.checkNotNull(collector, "AuditTrailCollector collector");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");
        ArgumentValidator.checkNotNull(mediator, "ContributorMediator mediator");
        ArgumentValidator.checkNotNull(settings, "Settings settings");

        this.store = store;
        this.collector = collector;
        this.preserver = preserver;
        this.mediator = mediator;
        this.settings = settings;
    }

    /**
     * Constructor for audit trail service with disabled preservation.
     * <p/>
     * See {@link #AuditTrailService(AuditTrailStore, AuditTrailCollector, AuditTrailPreserver, ContributorMediator,
     * Settings)} for param descriptions.
     */
    public AuditTrailService(AuditTrailStore store, AuditTrailCollector collector, ContributorMediator mediator, Settings settings) {
        this(store, collector, null, mediator, settings);
    }

    /**
     * Retrieve an iterator to all AuditTrailEvents matching the criteria from the parameters.
     * All parameters are allowed to be null, meaning that the parameter imposes no restriction on the result
     *
     * @param fromDate           Restrict the results to only provide events after this point in time
     * @param toDate             Restrict the results to only provide events up till this point in time
     * @param fileID             Restrict the results to only be about this fileID
     * @param collectionID       restrict the results to this collection
     * @param reportingComponent Restrict the results to only be reported by this component
     * @param actor              Restrict the results to only be events caused by this actor
     * @param action             Restrict the results to only be about this type of action
     * @param fingerprint        The fingerprint
     * @param operationID        Restrict the results to only this operationID
     * @return an iterator to all AuditTrailEvents matching the criteria from the parameters
     */
    public AuditEventIterator queryAuditTrailEventsByIterator(Date fromDate, Date toDate, String fileID, String collectionID,
                                                              String reportingComponent, String actor, FileAction action,
                                                              String fingerprint, String operationID) {
        return store.getAuditTrailsByIterator(fileID, collectionID, reportingComponent, null, null, actor, action, fromDate, toDate,
                fingerprint, operationID);
    }

    /**
     * Collects all the newest audit trails from all collections.
     */
    public void collectAuditTrails() {
        for (org.bitrepository.settings.repositorysettings.Collection c : settings.getRepositorySettings().getCollections()
                .getCollection()) {
            collector.collectNewestAudits(c.getID());
        }
    }

    /**
     * Collects all the newest audit trails from a specific collection.
     */
    public void collectAuditTrails(String collectionID) {
        collector.collectNewestAudits(collectionID);
    }

    /**
     * Get the list of {@link CollectorInfo}
     *
     * @return The list of CollectorInfo
     */
    public List<CollectorInfo> getCollectorInfos() {
        List<CollectorInfo> infos = new ArrayList<>();
        for (org.bitrepository.settings.repositorysettings.Collection c : settings.getRepositorySettings().getCollections()
                .getCollection()) {
            infos.add(collector.getCollectorInfo(c.getID()));
        }

        return infos;
    }

    /**
     * Get preservation info if preservation of audit trails is enabled.
     *
     * @return PreservationInfo or null if not enabled.
     */
    public PreservationInfo getPreservationInfo() {
        if (preserver == null) {
            return null;
        }
        return preserver.getPreservationInfo();
    }

    /**
     * Get the list of known contributors from the backend.
     *
     * @return The list of known contributors
     */
    public List<String> getContributors() {
        return store.getKnownContributors();
    }

    @Override
    public void start() {
        if (preserver != null) {
            preserver.start();
        }
        mediator.start();
    }

    @Override
    public void shutdown() {
        collector.close();
        if (preserver != null) {
            preserver.close();
        }
        store.close();
        mediator.close();

        MessageBus messageBus = MessageBusManager.getMessageBus();
        if (messageBus != null) {
            try {
                messageBus.close();
            } catch (JMSException e) {
                log.warn("Failed to close message bus cleanly, " + e.getMessage());
            }
        }
    }
}
