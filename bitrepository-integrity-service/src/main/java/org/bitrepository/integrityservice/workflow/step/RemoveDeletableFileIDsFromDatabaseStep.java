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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes all entries for the deletable files from the database. 
 * Uses the results of a MissingFileReport for identifying the files, which should be deleted.
 */
public class RemoveDeletableFileIDsFromDatabaseStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel cache;
    /** The report which contains the list of file ids to remove from the database.*/
    private final MissingFileReportModel report;
    /** The manager of audit trails.*/
    private final AuditTrailManager auditManager;
    /** The settings.*/
    private final Settings settings;
    
    /**
     * Constructor.
     * @param cache The IntegrityModel where the integrity data is stored.
     * @param report The report for missing files, where the files missing at all pillars explicit are marked
     * as deletable.
     * @param auditManager The audit trail manager.
     * @param settings The settings.
     */
    public RemoveDeletableFileIDsFromDatabaseStep(IntegrityModel cache, MissingFileReportModel report, 
            AuditTrailManager auditManager, Settings settings) {
        ArgumentValidator.checkNotNull(cache, "IntegrityModel cache");
        ArgumentValidator.checkNotNull(report, "MissingFileReportModel report");
        ArgumentValidator.checkNotNull(auditManager, "AuditTrailManager auditManager");
        ArgumentValidator.checkNotNull(settings,  "Settings settings");
        
        this.cache = cache;
        this.report = report;
        this.auditManager = auditManager;
        this.settings = settings;
    }
    
    @Override
    public String getName() {
        return "Remove deleted files";
    }
    
    @Override
    public void performStep() {
        for(String fileId : report.getDeleteableFiles()) {
            log.info("Removing entries for the file with id '" + fileId + "' from the database.");
            auditManager.addAuditEvent("test-collection", fileId, settings.getComponentID(),
                    "Deleting entry in database.", "The file has been reported missing at all pillars, and will thus "
                    + "be removed from integrity checking", FileAction.DELETE_FILE);
            cache.deleteFileIdEntry(fileId, report.getCollectionID());
        }
    }

    public static String getDescription() {
        return "Deletes all fileIDs from the database which appear to have been delete, " +
                "eg. which have disappeared from all pillars";
    }
}
