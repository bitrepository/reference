package org.bitrepository.integrityservice.reports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle writing (streaming) report parts, and generation (writing) of the final report 
 */
public class IntegrityReportWriter {
    private static final Logger log = LoggerFactory.getLogger(IntegrityReportWriter.class);

    private static final String DELETED_FILE = "deletedFile";
    private static final String CHECKSUM_ISSUE = "checksumIssue";
    private static final String MISSING_CHECKSUM = "missingChecksum";
    private static final String OBSOLETE_CHECKSUM = "obsoleteChecksum";
    private static final String MISSING_FILE = "missingFile";
    private static final String REPORT_FILE = "report";
    
    private static final String SECTION_HEADER_START_STOP = "========";
    private static final String PILLAR_HEADER_START_STOP = "--------";
    private static final String NOISSUE_HEADER_START_STOP = "++++++++";
    
    private final IntegrityReportPartWriter missingFilesWriter;
    private final IntegrityReportPartWriter checksumIssuesWriter;
    private final IntegrityReportPartWriter missingChecksumsWriter;
    private final IntegrityReportPartWriter obsoleteChecksumsWriter;
    
    private BufferedWriter deletedFilesWriter;
    private BufferedWriter reportFileWriter;
    private final File reportDir;
    
    public IntegrityReportWriter(File reportDir) {
        this.reportDir = reportDir;
        missingFilesWriter = new IntegrityReportPartWriter(MISSING_FILE, reportDir);
        checksumIssuesWriter = new IntegrityReportPartWriter(CHECKSUM_ISSUE, reportDir);
        missingChecksumsWriter = new IntegrityReportPartWriter(MISSING_CHECKSUM, reportDir);
        obsoleteChecksumsWriter = new IntegrityReportPartWriter(OBSOLETE_CHECKSUM, reportDir);
    }
    
    /**
     * Method to retrieve the path to the report file 
     */
    public String getReportFilePath() {
        File reportFile = new File(reportDir, REPORT_FILE);
        log.debug("getReportFilePath: Report file located at: " + reportFile.getAbsolutePath());
        return reportFile.getAbsolutePath();
    }
        
    /**
     * Method to handle writing of a deleted file entry to on-disk storage 
     * @param fileID The ID of the file to be added to the report as deleted from system.
     */
    public void writeDeletedFile(String fileID) throws IOException {
        if(deletedFilesWriter == null) {
            File deletedFilesFile = ReportWriterUtils.makeEmptyFile(reportDir, DELETED_FILE);
            deletedFilesWriter = new BufferedWriter(new FileWriter(deletedFilesFile, true));
        }
        ReportWriterUtils.addLine(deletedFilesWriter, fileID);
    }
    
    /**
     * Method to handle writing of a checksum issue for a file on a given pillar
     * @param pillarID The ID of the pillar with a checksum issue
     * @param fileID The ID of the file which have a checksum issue 
     */
    public void writeChecksumIssue(String pillarID, String fileID) throws IOException {
        checksumIssuesWriter.writeIssue(pillarID, fileID);    
    }
    
    /**
     * Method to handle writing of a missing file entry on a given pillar
     * @param pillarID The ID of the pillar where the file is missing
     * @param fileID The ID of the missing file
     */
    public void writeMissingFile(String pillarID, String fileID) throws IOException {
        missingFilesWriter.writeIssue(pillarID, fileID);
    }
    
    /**
     * Method to handle writing of a obsolete checksum entry for a given pillar
     * @param pillarID The ID of the pillar with the obsolete checksum
     * @param fileID The ID of the file which have an obsolete checksum 
     */
    public void writeObsoleteChecksum(String pillarID, String fileID) throws IOException {
        obsoleteChecksumsWriter.writeIssue(pillarID, fileID);
    }
    
    /**
     * Method to handle writing of a missing checksum entry for a given pillar
     * @param pillarID The ID of the pillar with the missing checksum
     * @param fileID The ID of the file missing a checksum 
     */
    public void writeMissingChecksum(String pillarID, String fileID) throws IOException {
        missingChecksumsWriter.writeIssue(pillarID, fileID);
    }
    
    /**
     * Method to write the full report. If a report already exists, the old file will be deleted 
     * and a fresh one generated.   
     */
    public void writeReport(String reportHeader) throws IOException {
        flushAll();
        if(reportFileWriter == null) {
            File reportFile = new File(reportDir, REPORT_FILE);
            if(reportFile.exists()) {
                reportFile.delete();
            }
            reportFileWriter = new BufferedWriter(new FileWriter(reportFile, true));
        }
        reportFileWriter.append(reportHeader);
        reportFileWriter.newLine();
        
        writeSectionHeader(reportFileWriter, "Deleted files");
        if(deletedFilesWriter != null) {
            writeSectionPart(reportFileWriter, new File(reportDir, DELETED_FILE));
        } else {
            writeNoIssueHeader(reportFileWriter, "No deleted files detected");
        }
        
        writeReportSection(reportFileWriter, missingFilesWriter.getSectionFiles(), "Missing files", 
                "No missing files detected");
        writeReportSection(reportFileWriter, checksumIssuesWriter.getSectionFiles(), "Checksum issues", 
                "No checksum issues detected");
        writeReportSection(reportFileWriter, missingChecksumsWriter.getSectionFiles(), "Missing checksums", 
                "No missing checksums detected");
        writeReportSection(reportFileWriter, obsoleteChecksumsWriter.getSectionFiles(), "Obsolete checksums", 
                "No obsolete checksums detected");
        
        reportFileWriter.flush();
        
    }
    
    /**
     * Flushes all open files 
     */
    private void flushAll() throws IOException {
        if(deletedFilesWriter != null) {
            deletedFilesWriter.flush();
        }
        missingChecksumsWriter.flushAll();
        checksumIssuesWriter.flushAll();
        missingFilesWriter.flushAll();
        obsoleteChecksumsWriter.flushAll();
    }
    
    /**
     * Method to close all open writers/streams
     * Only call close after finished using object.  
     */
    public void close() throws IOException {
        if(reportFileWriter != null) {
            reportFileWriter.close();
        }
        
        if(deletedFilesWriter != null) {
            deletedFilesWriter.close();
        }
        
        missingChecksumsWriter.closeAll();
        checksumIssuesWriter.closeAll();
        missingFilesWriter.closeAll();
        obsoleteChecksumsWriter.closeAll();
    }
    
    /**
     * Helper method to write the header of a report section 
     */
    private void writeSectionHeader(BufferedWriter report, String sectionName) throws IOException {
        report.append(SECTION_HEADER_START_STOP + " " + sectionName + " " + SECTION_HEADER_START_STOP);
        report.newLine();
    }
    
    /**
     * Helper method to write the header of a pillar part of a section 
     */
    private void writePillarHeader(BufferedWriter report, String pillarName) throws IOException {
        report.append(PILLAR_HEADER_START_STOP + " " + pillarName + " " + PILLAR_HEADER_START_STOP);
        report.newLine();
    }
    
    /**
     * Helper method to write the no-issue header of a section 
     */
    private void writeNoIssueHeader(BufferedWriter report, String issueMessage) throws IOException {
        report.append(NOISSUE_HEADER_START_STOP + " " + issueMessage + " " + NOISSUE_HEADER_START_STOP);
        report.newLine();

    }

    /**
     * Helper method to write the content of a report section 
     */
    private void writeReportSection(BufferedWriter report, Map<String, File> sectionData, 
            String sectionName, String emptyMessage) throws IOException {
        writeSectionHeader(report, sectionName);
        if(!sectionData.isEmpty()) {
            for(String pillar : sectionData.keySet()) {
                log.debug("Writing part for pillar: " + pillar);
                writePillarHeader(report, pillar);
                writeSectionPart(report, sectionData.get(pillar));
            }
        } else {
            writeNoIssueHeader(reportFileWriter, emptyMessage);
        }
    }
    
    /**
     * Helper method to read the actual content and write it back to the combined report file. 
     */
    private void writeSectionPart(BufferedWriter report, File partData) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(partData));
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
