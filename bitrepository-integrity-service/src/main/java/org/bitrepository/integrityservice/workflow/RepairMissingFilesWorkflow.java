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
package org.bitrepository.integrityservice.workflow;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.workflow.step.GetFileStep;
import org.bitrepository.integrityservice.workflow.step.PutFileStep;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowContext;
import org.bitrepository.service.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple workflow for repairing missing files.
 * Repairs at most 100 files across all pillars. 
 */
public class RepairMissingFilesWorkflow extends Workflow {
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The context for the workflow.*/
    protected IntegrityWorkflowContext context;
    protected String collectionID;
    protected IntegrityContributors integrityContributors;
    protected Date workflowStart;
    protected String description;
    protected WorkflowStep step = null;
    
    private static final int MAX_RESULTS = 100;
    /** List to keep track over the files, which have been */
    protected List<String> repairedFiles = new ArrayList<String>(MAX_RESULTS);
    
    /**
     * Remember to call the initialize method needs to be called before the start method.
     */
    public RepairMissingFilesWorkflow() {
    	description = "Not initialized";
    }

    @Override
    public void initialise(WorkflowContext context, String collectionID) {
        this.context = (IntegrityWorkflowContext)context;
        this.collectionID = collectionID;
        jobID = new JobID(getClass().getSimpleName(), collectionID);
        description = "Awaiting first run.";
    }
    
    @Override
    public void start() {
        if (context == null) {
            throw new IllegalStateException("The workflow can not be started before the initialise method has been " +
                    "called.");
        }
        super.start();
        
        try {
            for(String pillarId : SettingsUtils.getPillarIDsForCollection(collectionID)) {
                repairMissingFilesForPillar(pillarId);
            }
        } finally {
            finish();
            description = "Not running.";
        }
    }
    
    /**
     * Repairs the missing files on the pillar.
     * @param pillarId The pillar to repair.
     */
    private void repairMissingFilesForPillar(String pillarId) {
        List<String> filesNotRepaired = new ArrayList<String>();
        IntegrityIssueIterator iterator = context.getStore().getMissingFilesAtPillarByIterator(pillarId, 0L, 
                MAX_RESULTS, collectionID);
        String fileId;
        while((fileId = iterator.getNextIntegrityIssue()) != null && repairedFiles.size() < MAX_RESULTS) {
            // Do not try to repair file, which has already been repaired.
            if(repairedFiles.contains(fileId)) {
                continue;
            }
            repairedFiles.add(fileId);
            
            description = "Repairing the missing file '" + fileId + "'.";
            try {
                String checksum = getChecksumForFile(fileId);
                URL url = createURL(fileId);
                getFileStep(fileId, url);
                putFileStep(fileId, url, checksum);
                deleteUrl(url);
            } catch (Exception e) {
                // Fault barrier. Just try to continue
                log.warn("Error occured during repair of missing file, '" + fileId + "'. Tries to continue.", e);
                filesNotRepaired.add(fileId);
            }
        }
        
        if(!filesNotRepaired.isEmpty()) {
            context.getAlerter().operationFailed("Failed to repair the files '" + filesNotRepaired + "'.", collectionID);
        }
    }
    
    /**
     * Extracts the checksum for the file at the collection.
     * Will throw an exception, if no unanimous checksum is found - or no checksum at all.
     * @param fileId The id of the file, whose checksum should be extracted.
     * @return The checksum for the file.
     */
    private String getChecksumForFile(String fileId) {
        String res = null;
        for(FileInfo fi : context.getStore().getFileInfos(fileId, collectionID)) {
            if(res == null) {
                res = fi.getChecksum();
            } else {
                // validate, so we do not encounter an integrity issue.
                if(fi.getChecksum() != null && !res.equals(fi.getChecksum())) {
                    throw new IllegalStateException("Cannot extract an unanimous checksum for file '" + fileId 
                            + "' at collection '" + collectionID + "'.");
                }
            }
        }
        if(res == null) {
            throw new IllegalStateException("No checksum found for file '" + fileId + "' at collection '" 
                    + collectionID + "'");
        }
        return res;
    }
    
    /**
     * Creates a URL a given file id at our file-exchange.
     * @param fileId The id of the file to get the URL for.
     * @return The URL for the file.
     * @throws MalformedURLException If a wellformed URL cannot be created.
     */
    private URL createURL(String fileId) throws MalformedURLException {
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(context.getSettings());
        return fe.getURL(fileId);
    }
    
    /**
     * Performs the GetFile operation for the fileId for having the file delivered at the given URL.
     * @param fileId The id of the file.
     * @param url The URL 
     */
    private void getFileStep(String fileId, URL url) {
        try {
            step = new GetFileStep(context, collectionID, fileId, url);
            performStep(step);
        } finally {
            step = null;
        }
    }
    
    /**
     * Performs the put file step, where the file at the URL at put at all the pillars, who are missing it.
     * @param fileId The id of the file.
     * @param url The URL for the put file.
     * @param checksum The checksum of the file.
     */
    private void putFileStep(String fileId, URL url, String checksum) {
        try {
            step = new PutFileStep(context, collectionID, fileId, url, checksum);
            performStep(step);
        } finally {
            step = null;
        }
    }
    
    /**
     * Deletes the file at the file-exchange.
     * @param url The URL for the file to delete.
     * @throws IOException If an issue occur while deleting the file.
     * @throws URISyntaxException If the URL is invalid.
     */
    private void deleteUrl(URL url) throws URISyntaxException {
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(context.getSettings());
        try {
            fe.deleteFile(url);
        } catch(IOException e) {
            // Not critical, if we cannot remove the file.
            log.warn("Issue deleting file from FileExchange", e);
        }
    }

	@Override
	public String getDescription() {
	    if(step != null) {
	        return step.getName();
	    }
		return description;
	}
}
