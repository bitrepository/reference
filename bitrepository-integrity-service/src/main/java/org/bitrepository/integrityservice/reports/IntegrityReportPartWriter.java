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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class to write files for a specific part of the report.
 * Files are written as list of fileIDs per component (one file per component)
 */
public class IntegrityReportPartWriter {

    private final ReportPart part;
    private final File reportDir;
    private final Map<String, BufferedWriter> pillarParts = new TreeMap<>();

    /**
     * @param part      the part to write
     * @param reportDir The directory to store the files in
     */
    public IntegrityReportPartWriter(ReportPart part, File reportDir) {
        this.part = part;
        this.reportDir = reportDir;
    }

    /**
     * Add an issue for a given pillar to its file
     *
     * @param pillarID The ID of the pillar
     * @param fileID   The ID of the file that have issues
     * @throws IOException if an I/O error occurs
     */
    public void writeIssue(String pillarID, String fileID) throws IOException {
        BufferedWriter issueWriter;
        if (!pillarParts.containsKey(pillarID)) {
            File checksumIssueFile = ReportWriterUtils.makeEmptyFile(reportDir, part.getPartName() + "-" + pillarID);
            issueWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(checksumIssueFile, true), StandardCharsets.UTF_8));
            pillarParts.put(pillarID, issueWriter);
        } else {
            issueWriter = pillarParts.get(pillarID);
        }
        ReportWriterUtils.addLine(issueWriter, fileID);
    }

    /**
     * Flushes all open files
     *
     * @throws IOException if an I/O error occurs
     */
    public void flushAll() throws IOException {
        for (BufferedWriter writer : pillarParts.values()) {
            writer.flush();
        }
    }

    /**
     * Closes all open files
     *
     * @throws IOException if an I/O error occurs
     */
    public void closeAll() throws IOException {
        for (BufferedWriter writer : pillarParts.values()) {
            writer.close();
        }
    }

    /**
     * Get mapping from pillar to the file with the issues.
     *
     * @return Mapping between pillarID and the file with the issues for the given pillar.
     */
    public Map<String, File> getSectionFiles() {
        Map<String, File> files = new HashMap<>();
        for (String part : pillarParts.keySet()) {
            File f = new File(reportDir, this.part.getPartName() + "-" + part);
            files.put(part, f);
        }
        return files;
    }
}
