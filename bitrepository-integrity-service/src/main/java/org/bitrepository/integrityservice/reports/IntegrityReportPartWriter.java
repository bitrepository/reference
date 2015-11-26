package org.bitrepository.integrityservice.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bitrepository.integrityservice.reports.IntegrityReportConstants.ReportPart;

/**
 * Class to write files for a specific part of the report. 
 * Files are written as list of fileIDs per component (one file per component) 
 */
public class IntegrityReportPartWriter {

    private final ReportPart part;
    private final File reportDir;
    private final Map<String, BufferedWriter> pillarParts = new TreeMap<String, BufferedWriter>();

    /**
     * Constructor
     * @param part the part to write
    * @param reportDir The directory to store the files in
     */
    public IntegrityReportPartWriter(ReportPart part, File reportDir) {
        this.part = part;
        this.reportDir = reportDir;
    }
    
    /**
     * Add an issue for a given pillar to its file
     * @param pillarID The ID of the pillar 
     * @param fileID The ID of the file that have issues
     * @throws IOException if an I/O error occurs
     */
    public void writeIssue(String pillarID, String fileID) throws IOException {
        BufferedWriter issueWriter;
        if(!pillarParts.containsKey(pillarID)) {
            File checksumIssueFile = ReportWriterUtils.makeEmptyFile(reportDir, part.getPartname() + "-" + pillarID);
            issueWriter = new BufferedWriter(new FileWriter(checksumIssueFile, true));
            pillarParts.put(pillarID, issueWriter);
        } else {
           issueWriter = pillarParts.get(pillarID); 
        }
        ReportWriterUtils.addLine(issueWriter, fileID);  
    }
    
    /**
     * Flushes all open files
     * @throws IOException if an I/O error occurs
     */
    public void flushAll() throws IOException {
        for(BufferedWriter writer : pillarParts.values()) {
            writer.flush();
        } 
    }
    
    /**
     * Closes all open files
     * @throws IOException if an I/O error occurs
     */
    public void closeAll() throws IOException {
        for(BufferedWriter writer : pillarParts.values()) {
            writer.close();
        } 
    }
    
    /**
     * Get mapping from pillar to the file with the issues.  
     * @return Mapping between pillarID and the file with the issues for the given pillar.
     */
    public Map<String, File> getSectionFiles() {
        Map<String, File> files = new HashMap<String, File>();
        for(String part : pillarParts.keySet()) {
            File f = new File(reportDir, this.part.getPartname() + "-" + part);
            files.put(part, f);
        }
        return files;
    }
}
