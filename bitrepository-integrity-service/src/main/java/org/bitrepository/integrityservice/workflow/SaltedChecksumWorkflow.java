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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.workflow.step.GetChecksumForFileStep;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowContext;
import org.bitrepository.service.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple workflow for validating files with random salted checksum.
 * Only chooses one single file for each run.
 * <p>
 * The GetChecksumClient automatically ignores any checksum-pillars and other pillars,
 * who are not able to handle salted checksums.
 */
public class SaltedChecksumWorkflow extends Workflow {
    private final static Logger log = LoggerFactory.getLogger(SaltedChecksumWorkflow.class);
    /**
     * The context for the workflow.
     */
    protected IntegrityWorkflowContext context;
    protected String collectionID;
    protected IntegrityContributors integrityContributors;
    protected WorkflowStep step = null;

    /**
     * The current FileID to validate. Set to null, when not checking any file.
     */
    protected String currentFileID = null;
    /**
     * The current ChecksumSpec. Set to null, when not checking any file.
     */
    protected ChecksumSpecTYPE currentChecksumSpec = null;

    /**
     * Remember to call the initialize method needs to be called before the start method.
     */
    public SaltedChecksumWorkflow() {}

    @Override
    public void initialise(WorkflowContext context, String collectionID) {
        this.context = (IntegrityWorkflowContext) context;
        this.collectionID = collectionID;
        jobID = new JobID(getClass().getSimpleName(), collectionID);
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        integrityContributors = new IntegrityContributors(pillars, 0);
    }

    @Override
    public void start() {
        if (context == null) {
            throw new IllegalStateException("The workflow can not be started before the initialise method has been " + "called.");
        }
        super.start();

        try {
            currentChecksumSpec = getChecksumSpecWithRandomSalt();
            currentFileID = getRandomFileId();
            Map<String, String> checksums = requestSaltedChecksumForFileStep();
            validateChecksums(checksums);
        } catch (IllegalStateException e) {
            context.getAlerter().integrityFailed("Failed trying to check salted checksum: " + e.getMessage(), collectionID);
        } finally {
            finish();
        }
    }

    /**
     * Creates a random checksum spec.
     * Chooses the HMAC version of the default checksum algorithm.
     * Then generate a UUID and use it as the salt.
     *
     * @return The actual {@link ChecksumSpecTYPE}.
     */
    private ChecksumSpecTYPE getChecksumSpecWithRandomSalt() throws IllegalArgumentException {
        ChecksumType defaultChecksum = ChecksumType.valueOf(
                context.getSettings().getRepositorySettings().getProtocolSettings().getDefaultChecksumType());
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        switch (defaultChecksum) {
            case SHA1:
                res.setChecksumType(ChecksumType.HMAC_SHA1);
                break;
            case SHA256:
                res.setChecksumType(ChecksumType.HMAC_SHA256);
                break;
            case SHA384:
                res.setChecksumType(ChecksumType.HMAC_SHA384);
                break;
            case SHA512:
                res.setChecksumType(ChecksumType.HMAC_SHA512);
                break;
            default:
                res.setChecksumType(ChecksumType.HMAC_MD5);
                break;
        }

        String salt = UUID.randomUUID().toString().replace("-", "");
        try {
            res.setChecksumSalt(Base16Utils.encodeBase16(salt));
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Failed to set random salt.", e);
        }

        return res;
    }

    /**
     * Retrieves a random FileID from the integrity database.
     *
     * @return The randomly found FileID.
     */
    private String getRandomFileId() {
        long numberOfFiles = context.getStore().getNumberOfFilesInCollection(collectionID);
        if (numberOfFiles <= 0L) {
            throw new IllegalStateException("No files in collection '" + collectionID + "'.");
        }
        long randomFileIndex = ThreadLocalRandom.current().nextLong(numberOfFiles);
        return context.getStore().getFileIDAtPosition(collectionID, randomFileIndex);
    }

    /**
     * Performs the conversation with the pillars to retrieve the checksums.
     *
     * @return The map between pillars and checksums.
     */
    private Map<String, String> requestSaltedChecksumForFileStep() {
        log.info("Request the file '" + currentFileID + "' with the checksumSpecTYPE ' " + currentChecksumSpec + "'.");
        GetChecksumForFileStep step = new GetChecksumForFileStep(context.getCollector(), context.getAlerter(), currentChecksumSpec,
                currentFileID, context.getSettings(), collectionID, integrityContributors);
        performStep(step);
        return step.getResults();
    }

    /**
     * Validates the map of the checksums to ensure, that they all align.
     *
     * @param checksums The map of checksums for
     */
    private void validateChecksums(Map<String, String> checksums) {
        if (checksums == null || checksums.isEmpty()) {
            sendFailure("No checksums with checksumSpec '" + currentChecksumSpec + "' received for file '" + currentFileID + "'.");
            return;
        }
        List<String> cs = new ArrayList<>();
        for (Map.Entry<String, String> entry : checksums.entrySet()) {
            if (!cs.contains(entry.getValue())) {
                cs.add(entry.getValue());
            }
        }
        if (cs.size() > 1) {
            sendFailure("Inconsistent salted checksum found for file '" + currentFileID + "' with checksumSpecTYPE '" +
                    currentChecksumSpec.getChecksumType() + "' and salt '" +
                    Base16Utils.decodeBase16(currentChecksumSpec.getChecksumSalt()) + "'. The pillars had the checksums: " + checksums);
        } else {
            String audit = "Validated salted checksum for file '" + currentFileID + "' with checksumSpecTYPE '" +
                    currentChecksumSpec.getChecksumType() + "' and salt '" +
                    Base16Utils.decodeBase16(currentChecksumSpec.getChecksumSalt()) + "' for pillars: " + checksums.keySet();
            log.info(audit);
            context.getAuditManager()
                    .addAuditEvent(collectionID, currentFileID, "IntegrityServiceWorkflow: " + this.getClass().getName(), audit,
                            "Integrity salted checksum check", FileAction.INTEGRITY_CHECK, null, null);
        }
    }

    /**
     * Log, audit and send an alarm about the failure.
     *
     * @param failureMessage The failure message.
     */
    private void sendFailure(String failureMessage) {
        log.warn("Failure in checksum salted checksum: " + failureMessage);
        context.getAuditManager()
                .addAuditEvent(collectionID, currentFileID, "IntegrityServiceWorkflow: " + this.getClass().getName(), failureMessage,
                        "Integrity salted checksum check", FileAction.INTEGRITY_CHECK, null, null);
        context.getAlerter().integrityFailed(failureMessage, collectionID);
    }

    @Override
    public String getDescription() {
        if (currentFileID == null) {
            return "Can check a randomly selected file against a random salted checksum";
        } else {
            return "Is currently checking the file '" + currentFileID + "' with checksum '" + currentChecksumSpec + "'";
        }
    }
}
