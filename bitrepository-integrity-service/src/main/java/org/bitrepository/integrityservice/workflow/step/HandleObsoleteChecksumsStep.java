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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.checking.MaxChecksumAgeProvider;
import org.bitrepository.integrityservice.checking.reports.IntegrityReporter;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleObsoleteChecksumsStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The settings */
    private final Settings settings;
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The report model to populate */
    private final IntegrityReporter reporter;
    /** A year */
    public static final long DEFAULT_MAX_CHECKSUM_AGE = TimeUtils.MS_PER_YEAR;
    
    public HandleObsoleteChecksumsStep(Settings settings, IntegrityModel store, IntegrityReporter reporter) {
        this.settings = settings;
        this.store = store;
        this.reporter = reporter;
    }
    
    @Override
    public String getName() {
        return "Handle obsolete checksums reporting.";
    }

    /**
     * Queries the IntegrityModel for files with obsolete checksums. Reports them if any is returned.
     */
    @Override
    public synchronized void performStep() {
        MaxChecksumAgeProvider maxChecksumAgeProvider = new MaxChecksumAgeProvider(DEFAULT_MAX_CHECKSUM_AGE,
                settings.getReferenceSettings().getIntegrityServiceSettings().getObsoleteChecksumSettings());
        
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());
        
        for(String pillar : pillars) {
            Date outDated = new Date(System.currentTimeMillis() - maxChecksumAgeProvider.getMaxChecksumAge(pillar));
            Set<String> filesWithObsoleteChecksums = 
                    new HashSet<String>(store.findChecksumsOlderThan(outDated, pillar, reporter.getCollectionID()));
            for(String file : filesWithObsoleteChecksums) {
                for(FileInfo fileinfo : store.getFileInfos(file, reporter.getCollectionID())) {
                    if(!fileinfo.getPillarId().equals(pillar)) {
                        continue;
                    }
                    if(fileinfo.getFileState() != FileState.EXISTING) {
                        continue;
                    }
                    if(CalendarUtils.convertFromXMLGregorianCalendar(
                        fileinfo.getDateForLastChecksumCheck()).before(outDated)) {
                        reporter.reportObsoleteChecksum(file, pillar);
                    }
                }
            }
        }
    }

    public static String getDescription() {
        return "Detects and reports files that have obsolete a checksum from one or more pillars in the collection.";
    }
}
