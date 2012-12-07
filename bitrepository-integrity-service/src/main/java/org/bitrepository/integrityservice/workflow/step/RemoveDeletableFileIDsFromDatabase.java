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

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes all entries for the deletable files from the database. 
 * Uses the results of a MissingFileReport for identifying the files, which should be deleted.
 */
public class RemoveDeletableFileIDsFromDatabase implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** Checker for performing the integrity checks.*/
    private final IntegrityModel cache;
    /** The report which contains the list of file ids to remove from the database.*/
    private final MissingFileReportModel report;
    
    public RemoveDeletableFileIDsFromDatabase(IntegrityModel cache, MissingFileReportModel report) {
        ArgumentValidator.checkNotNull(cache, "IntegrityModel cache");
        ArgumentValidator.checkNotNull(report, "MissingFileReportModel report");
        
        this.cache = cache;
        this.report = report;
    }
    
    @Override
    public String getName() {
        return "Remove deletable file id entries from the database.";
    }
    
    @Override
    public void performStep() {
        for(String fileId : report.getDeleteableFiles()) {
            log.info("Removing entries for the file with id '" + fileId + "' from the database.");
            cache.deleteFileIdEntry(fileId);
        }
    }
}
