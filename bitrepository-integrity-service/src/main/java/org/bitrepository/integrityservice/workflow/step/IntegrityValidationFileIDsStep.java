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

import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.reports.IntegrityReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The step for validating the integrity of the file ids.
 * Based on this integrity report, it is decided whether to dispatch an alarm.
 */
public class IntegrityValidationFileIDsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** Checker for performing the integrity checks.*/
    private final IntegrityChecker checker;
    /** The dispatcher of alarms.*/
    private final IntegrityAlerter dispatcher;
    
    /**
     * Constructor.
     * @param checker The checker for performing the integrity checks.
     * @param alarmDispatcher The dispatcher of alarms.
     */
    public IntegrityValidationFileIDsStep(IntegrityChecker checker, IntegrityAlerter alarmDispatcher) {
        this.checker = checker;
        this.dispatcher = alarmDispatcher;
    }
    
    @Override
    public String getName() {
        return "Validating the integrity of the file ids.";
    }

    @Override
    public void performStep() {
        IntegrityReport report = checker.checkFileIDs(FileIDsUtils.getAllFileIDs());
        
        if(report.hasIntegrityIssues()) {
            log.warn("Integrity issues found: " + report.generateReport());
            dispatcher.integrityFailed(report);
        } else {
            log.info("No integrity issues found: " + report.generateReport());
        }
    }
}
