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
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.workflow.step.GetFileStep;
import org.bitrepository.integrityservice.workflow.step.PutFileStep;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowContext;
import org.bitrepository.service.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple workflow for repairing missing files.
 * Repairs 100 files per pillars. 
 */
public class SaltedChecksumWorkflow extends Workflow {
    private final static Logger log = LoggerFactory.getLogger(SaltedChecksumWorkflow.class);
    /** The context for the workflow.*/
    protected IntegrityWorkflowContext context;
    protected String collectionID;
    protected IntegrityContributors integrityContributors;
    protected Date workflowStart;
    protected WorkflowStep step = null;
    
    /** The current FileID to validate. Set to null, when not checking any file.*/
    protected String currentFileID = null;
    /** The current ChecksumSpec. Set to null, when not checking any file.*/
    protected ChecksumSpecTYPE currentChecksumSpec = null;
    
    /**
     * Remember to call the initialize method needs to be called before the start method.
     */
    public SaltedChecksumWorkflow() { }

    @Override
    public void initialise(WorkflowContext context, String collectionID) {
        this.context = (IntegrityWorkflowContext)context;
        this.collectionID = collectionID;
        jobID = new JobID(getClass().getSimpleName(), collectionID);
    }
    
    @Override
    public void start() {
        if (context == null) {
            throw new IllegalStateException("The workflow can not be started before the initialise method has been " +
                    "called.");
        }
        super.start();

        try {
            currentChecksumSpec = getRandomChecksumSpec();
            currentFileID = getRandomFileId();
            Map<String, String> checksums = requestSaltedChecksumForFileStep();
            validateChecksums(checksums);
        } finally {
            finish();
        }
    }
    
    /**
     * Creates a random checksum spec.
     * Chooses the algorithm by a random number between 0 and 4 - one for each of the 5 HMAC algorithms.
     * Then generate a UUID and use it as the salt.
     * @return The checksumspec.
     */
    private ChecksumSpecTYPE getRandomChecksumSpec() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        int csType = (new Random()).nextInt() % 5;
        switch(csType) {
        case 1:
            res.setChecksumType(ChecksumType.HMAC_SHA1);
            break;
        case 2:
            res.setChecksumType(ChecksumType.HMAC_SHA256);
            break;
        case 3:
            res.setChecksumType(ChecksumType.HMAC_SHA384);
            break;
        case 4:
            res.setChecksumType(ChecksumType.HMAC_SHA512);
            break;
        default:
            res.setChecksumType(ChecksumType.HMAC_MD5);
            break;
        }
        
        String salt = UUID.randomUUID().toString();
        res.setChecksumSalt(Base16Utils.encodeBase16(salt));
        
        return res;
    }
    
    /**
     * Retrieves a random FileID from the integrity database.
     * @return The randomly found FileID.
     */
    private String getRandomFileId() {
        long numberOfFiles = context.getStore().getNumberOfFilesInCollection(collectionID);
        long randomFileIndex = (new Random()).nextLong() % numberOfFiles;
        return context.getStore().getFileIDAtPosition(collectionID, randomFileIndex);
    }
    
    /**
     * 
     * @return
     */
    private Map<String, String> requestSaltedChecksumForFileStep() {
        
        return null;
    }
    
    /**
     * Validates the map of the checksums to ensure, that they all align.
     * @param checksums The map of checksums for 
     * @return
     */
    private boolean validateChecksums(Map<String, String> checksums) {
        String firstCs = null;
        String firstPillar = null;
        for(Map.Entry<String, String> entry : checksums.entrySet()) {
            if(firstCs == null) {
                firstCs = entry.getValue();
                firstPillar = entry.getKey();
            } else {
                if(!entry.getValue().equalsIgnoreCase(firstCs)) {
                    log.warn("The pillars '" + firstPillar + "' and '" + entry.getKey() + "' "
                            + "does not have the same checksums.");
                    return false;
                }
            }
        }
        return true;
    }

	@Override
	public String getDescription() {
	    if(currentFileID == null) {
	        return "Can check a randomly selected file against a random salted checksum";
	    } else {
	        return "Is currently checking the file '" + currentFileID + "' with checksum '"
	                + currentChecksumSpec + "'";
	    }
	}
}
