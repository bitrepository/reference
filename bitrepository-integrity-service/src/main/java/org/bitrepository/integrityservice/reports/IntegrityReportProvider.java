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
package org.bitrepository.integrityservice.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle access to integrity reports
 */
public class IntegrityReportProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, IntegrityReportReader> reports = new HashMap<>();
    private final File reportsDir;

    public IntegrityReportProvider(File reportsDir) {
        this.reportsDir = reportsDir;
    }

    /**
     * Get the latest access to the latest Integrity report using an IntegrityReportReader
     *
     * @param collectionID The collection to retrieve the latest report for
     * @return IntegrityReportReader with the latest integrity report
     * @throws FileNotFoundException if no report could be found
     */
    public synchronized IntegrityReportReader getLatestIntegrityReportReader(String collectionID) throws FileNotFoundException {
        IntegrityReportReader reader = reports.get(collectionID);
        if (reader == null) {
            log.info("Trying to lookup the latest report on disk for collection {}", collectionID);
            File latestReportDir = getLatestReportFromDisk(collectionID);
            if (latestReportDir != null) {
                reader = new IntegrityReportReader(latestReportDir);
                reports.put(collectionID, reader);
            } else {
                throw new FileNotFoundException("Could not find latest integrity report");
            }
        }
        return reader;
    }

    /**
     * Get the latest integrity report of a collection, for a specific report part and specific pillar.
     *
     * @param collectionID The specific collectionID.
     * @param pillarID     The specific pillarID.
     * @param reportPart   The specific ReportPart.
     * @return Returns the given report part {@link File} if it exists.
     * @throws FileNotFoundException If no such report part can be found for the given pillar and collection.
     */
    public synchronized File getIntegrityReportPart(String collectionID, String pillarID, String reportPart)
            throws FileNotFoundException {
        log.info("Trying to lookup the '{}' report on disk for collection '{}'", reportPart, collectionID);
        File latestReportPart = getReportPartFromDisk(collectionID, pillarID, reportPart);
        if (latestReportPart != null) {
            return latestReportPart;
        } else {
            throw new FileNotFoundException("Could not find latest integrity report");
        }
    }

    /**
     * Register the latest integrity report for a given collection
     *
     * @param collectionID The collection to register the report for
     * @param reportDir    The directory containing the integrity report and report parts
     */
    public synchronized void setLatestReport(String collectionID, File reportDir) {
        reports.put(collectionID, new IntegrityReportReader(reportDir));
    }

    private File getLatestReportFromDisk(String collectionID) {
        File collectionReports = new File(reportsDir, collectionID);
        log.info("Looking for latest report dir in '{}'", collectionReports);
        File[] reports = collectionReports.listFiles(File::isDirectory);
        long lastModification = Long.MIN_VALUE;
        File latestReport = null;
        assert reports != null;
        for (File reportDir : reports) {
            if (reportDir.lastModified() > lastModification) {
                File reportFile = new File(reportDir, IntegrityReportConstants.REPORT_FILE);
                if (reportFile.exists()) {
                    latestReport = reportDir;
                    lastModification = latestReport.lastModified();
                } else {
                    log.debug("Candidate report dir '{}' had no report file", reportDir);
                }
            }
        }

        return latestReport;
    }

    /**
     * Returns the latest report part for the given collection and pillar.
     *
     * @param collectionID The collection ID.
     * @param pillarID     The pillar ID.
     * @param reportPart   The wanted report part as {@link String}.
     * @return Returns a {@link File} containing the latest report part for the given collection and pillar if such a file exists.
     */
    private File getReportPartFromDisk(String collectionID, String pillarID, String reportPart) {
        File collectionReports = new File(reportsDir, collectionID);
        log.info("Looking for latest report dir in '{}'", collectionReports);
        File[] reports = collectionReports.listFiles(File::isDirectory);
        long lastModification = Long.MIN_VALUE;
        File latestReport = null;
        assert reports != null;
        for (File reportDir : reports) {
            if (reportDir.lastModified() > lastModification) {
                File reportFile = new File(reportDir, reportPart + "-" + pillarID);
                if (reportFile.exists()) {
                    latestReport = reportFile;
                    lastModification = latestReport.lastModified();
                } else {
                    log.debug("Report dir '{}' had no '{}'-report for pillar '{}'", reportDir, reportPart, pillarID);
                }
            }
        }

        return latestReport;
    }
}
