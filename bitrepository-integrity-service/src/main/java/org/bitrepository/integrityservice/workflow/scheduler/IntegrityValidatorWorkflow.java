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
package org.bitrepository.integrityservice.workflow.scheduler;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.IntegrityReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for validating the integrity of the files.
 * Will no collect any data from the pillars, only go through the already collected integrity data.
 */
public class IntegrityValidatorWorkflow extends IntervalWorkflow {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The checker for checking the integrity of the data.*/
    private final IntegrityChecker checker;
    
    /**
     * Constructor.
     * @param interval The interval for this workflow.
     * @param name The name of this workflow.
     * @param checker The checker for validating the integrity of the data. 
     */
    public IntegrityValidatorWorkflow(long interval, String name, IntegrityChecker checker) {
        super(interval, name);
        this.checker = checker;
    }

    @Override
    public void runWorkflow() {
        FileIDs allFileIDs = getAllFileIDs();
        
        IntegrityReport integrityReport = checker.checkFileIDs(allFileIDs);
        integrityReport.combineWithReport(checker.checkChecksum(allFileIDs));
        
        // TODO perhaps send alarm?
        if(integrityReport.hasIntegrityIssues()) {
            log.warn(integrityReport.generateReport());
        } else {
            log.info(integrityReport.generateReport());            
        }
    }

    /**
     * @return A FileIDs object for all file ids.
     */
    private FileIDs getAllFileIDs() {
        FileIDs res = new FileIDs();
        res.setAllFileIDs("true");
        return res;
    }
}
