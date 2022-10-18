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

import org.bitrepository.integrityservice.reports.IntegrityReportConstants.ReportPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Class to handle writing (streaming) report parts, and generation (writing) of the final report
 */
public class IntegrityReportWriter {
    private static final Logger log = LoggerFactory.getLogger(IntegrityReportWriter.class);
    private final IntegrityReportPartWriter missingFilesWriter;
    private final IntegrityReportPartWriter checksumIssuesWriter;
    private final IntegrityReportPartWriter missingChecksumsWriter;
    private final IntegrityReportPartWriter obsoleteChecksumsWriter;
    private final IntegrityReportPartWriter deletedFilesWriter2;
    private BufferedWriter reportFileWriter;
    private final File reportDir;

    public IntegrityReportWriter(File reportDir) {
        this.reportDir = reportDir;
        missingFilesWriter = new IntegrityReportPartWriter(ReportPart.MISSING_FILE, reportDir);
        checksumIssuesWriter = new IntegrityReportPartWriter(ReportPart.CHECKSUM_ERROR, reportDir);
        missingChecksumsWriter = new IntegrityReportPartWriter(ReportPart.MISSING_CHECKSUM, reportDir);
        obsoleteChecksumsWriter = new IntegrityReportPartWriter(ReportPart.OBSOLETE_CHECKSUM, reportDir);
        deletedFilesWriter2 = new IntegrityReportPartWriter(ReportPart.DELETED_FILE, reportDir);
    }

    /**
     * Method to retrieve the path to the report file
     *
     * @return the path to the report file
     */
    public String getReportFilePath() {
        File reportFile = new File(reportDir, IntegrityReportConstants.REPORT_FILE);
        log.debug("getReportFilePath: Report file located at: {}", reportFile.getAbsolutePath());
        return reportFile.getAbsolutePath();
    }

    public void writeDeletedFile(String pillarID, String fileID) throws IOException {
        deletedFilesWriter2.writeIssue(pillarID, fileID);
    }

    /**
     * Method to handle writing of a checksum issue for a file on a given pillar
     *
     * @param pillarID The ID of the pillar with a checksum issue
     * @param fileID   The ID of the file which have a checksum issue
     * @throws IOException if an I/O error occurs
     */
    public void writeChecksumIssue(String pillarID, String fileID) throws IOException {
        checksumIssuesWriter.writeIssue(pillarID, fileID);
    }

    /**
     * Method to handle writing of a missing file entry on a given pillar
     *
     * @param pillarID The ID of the pillar where the file is missing
     * @param fileID   The ID of the missing file
     * @throws IOException if an I/O error occurs
     */
    public void writeMissingFile(String pillarID, String fileID) throws IOException {
        missingFilesWriter.writeIssue(pillarID, fileID);
    }

    /**
     * Method to handle writing of a obsolete checksum entry for a given pillar
     *
     * @param pillarID The ID of the pillar with the obsolete checksum
     * @param fileID   The ID of the file which have an obsolete checksum
     * @throws IOException if an I/O error occurs
     */
    public void writeObsoleteChecksum(String pillarID, String fileID) throws IOException {
        obsoleteChecksumsWriter.writeIssue(pillarID, fileID);
    }

    /**
     * Method to handle writing of a missing checksum entry for a given pillar
     *
     * @param pillarID The ID of the pillar with the missing checksum
     * @param fileID   The ID of the file missing a checksum
     * @throws IOException if an I/O error occurs
     */
    public void writeMissingChecksum(String pillarID, String fileID) throws IOException {
        missingChecksumsWriter.writeIssue(pillarID, fileID);
    }

    /**
     * Method to write the full report. If a report already exists, the old file will be deleted
     * and a fresh one generated.
     *
     * @param reportHeader the report header
     * @throws IOException if an I/O error occurs
     */
    public void writeReport(String reportHeader) throws IOException {
        flushAll();
        if (reportFileWriter == null) {
            File reportFile = new File(reportDir, IntegrityReportConstants.REPORT_FILE);
            if (reportFile.exists()) {
                reportFile.delete();
            }
            reportFileWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(reportFile, true), StandardCharsets.UTF_8));
        }
        reportFileWriter.append(reportHeader);
        reportFileWriter.newLine();

        writeReportSection(reportFileWriter, deletedFilesWriter2.getSectionFiles(), "Deleted files",
                "No deleted files detected");
        writeReportSection(reportFileWriter, missingFilesWriter.getSectionFiles(), "Missing files",
                "No missing files detected");
        writeReportSection(reportFileWriter, checksumIssuesWriter.getSectionFiles(), "Inconsistent checksums",
                "No inconsistent checksums detected");
        writeReportSection(reportFileWriter, missingChecksumsWriter.getSectionFiles(), "Missing checksums",
                "No missing checksums detected");
        writeReportSection(reportFileWriter, obsoleteChecksumsWriter.getSectionFiles(), "Obsolete checksums",
                "No obsolete checksums detected");

        reportFileWriter.flush();

    }

    /**
     * Flushes all open files
     *
     * @throws IOException if an I/O error occurs
     */
    private void flushAll() throws IOException {
        deletedFilesWriter2.flushAll();
        missingChecksumsWriter.flushAll();
        checksumIssuesWriter.flushAll();
        missingFilesWriter.flushAll();
        obsoleteChecksumsWriter.flushAll();
    }

    /**
     * Method to close all open writers/streams
     * Only call close after finished using object.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        if (reportFileWriter != null) {
            reportFileWriter.close();
        }

        deletedFilesWriter2.closeAll();
        missingChecksumsWriter.closeAll();
        checksumIssuesWriter.closeAll();
        missingFilesWriter.closeAll();
        obsoleteChecksumsWriter.closeAll();
    }

    /**
     * Helper method to write the header of a report section
     *
     * @param report      the report writer
     * @param sectionName the section name
     */
    private void writeSectionHeader(BufferedWriter report, String sectionName) throws IOException {
        report.append(IntegrityReportConstants.SECTION_HEADER_START_STOP + " ").append(sectionName).append(" ")
                .append(IntegrityReportConstants.SECTION_HEADER_START_STOP);
        report.newLine();
    }

    /**
     * Helper method to write the header of a pillar part of a section
     *
     * @param report the report writer
     */
    private void writePillarHeader(BufferedWriter report, String pillarName) throws IOException {
        report.append(IntegrityReportConstants.PILLAR_HEADER_START_STOP + " ").append(pillarName).append(" ")
                .append(IntegrityReportConstants.PILLAR_HEADER_START_STOP);
        report.newLine();
    }

    /**
     * Helper method to write the no-issue header of a section
     *
     * @param report the report writer
     */
    private void writeNoIssueHeader(BufferedWriter report, String issueMessage) throws IOException {
        report.append(IntegrityReportConstants.NO_ISSUE_HEADER_START_STOP + " ").append(issueMessage).append(" ")
                .append(IntegrityReportConstants.NO_ISSUE_HEADER_START_STOP);
        report.newLine();

    }

    /**
     * Helper method to write the content of a report section
     *
     * @param report the report writer
     */
    private void writeReportSection(BufferedWriter report, Map<String, File> sectionData,
                                    String sectionName, String emptyMessage) throws IOException {
        writeSectionHeader(report, sectionName);
        if (!sectionData.isEmpty()) {
            for (String pillar : sectionData.keySet()) {
                log.debug("Writing part for pillar: {}", pillar);
                writePillarHeader(report, pillar);
                writeSectionPart(report, sectionData.get(pillar));
            }
        } else {
            writeNoIssueHeader(reportFileWriter, emptyMessage);
        }
    }

    /**
     * Helper method to read the actual content and write it back to the combined report file.
     *
     * @param report the report writer
     */
    private void writeSectionPart(BufferedWriter report, File partData) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(partData), StandardCharsets.UTF_8));
        String fileID;
        try {
            while ((fileID = br.readLine()) != null) {
                report.append(fileID);
                report.newLine();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
