/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.preserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.exceptions.OperationFailedException;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the preservation of audit trails to a collection defined for the local repository.
 * This means, that each set of audit trails will be preserved within its own collection.
 */
public class LocalAuditTrailPreserver implements AuditTrailPreserver {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The audit trails store, where the audit trails can be extracted.*/
    private final AuditTrailStore store;
    /** The put file client for sending the resulting files. */
    private final BlockingPutFileClient client;

    /** The timer for scheduling the preservation of audit trails.*/
    private Timer timer;
    /** The timertask for preserving the audit trails.*/
    private AuditPreservationTimerTask auditTask = null;
    /** The mapping between collection and the Audit trail packer for packing and compressing the audit trails.*/
    Map<String, AuditPacker> auditPackers = new HashMap<String, AuditPacker>();
    /** The preservationSettings for the local audit trail preserver.*/
    private final AuditTrailPreservation preservationSettings;
    /** The full settings (needed for checksum calculation) */
    private final Settings settings;
    /** The fileexchange to use for uploading files which should be put to the preservation collection */
    private final FileExchange exchange;
    
    /**
     * Constructor.
     * @param settings The preservationSettings for the audit trail service.
     * @param store The storage of the audit trails, which should be preserved.
     * @param client The PutFileClient for putting the audit trail packages to the collection.
     */
    public LocalAuditTrailPreserver(Settings settings, AuditTrailStore store, PutFileClient client,
                                    FileExchange exchange) {
        ArgumentValidator.checkNotNull(settings, "Settings preservationSettings");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");
        ArgumentValidator.checkNotNull(client, "PutFileClient client");
        
        this.settings = settings;
        this.preservationSettings = settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation();
        this.store = store;
        this.client = new BlockingPutFileClient(client);
        this.exchange = exchange;
        for(String collectionID : SettingsUtils.getAllCollectionsIDs()) {
            this.auditPackers.put(collectionID, new AuditPacker(store, preservationSettings, collectionID));
        }
    }
    
    @Override
    public void start() {
        if(timer != null) {
            log.debug("Cancelling old timer.");
            timer.cancel();
        }
        long preservationInterval = preservationSettings.getAuditTrailPreservationInterval();
        long timerCheckInterval = preservationInterval/10;
        log.info("Instantiating the preservation of audit trails every " +
                TimeUtils.millisecondsToHuman(preservationInterval));
        timer = new Timer();
        auditTask = new AuditPreservationTimerTask(preservationInterval);
        timer.scheduleAtFixedRate(auditTask, timerCheckInterval, timerCheckInterval);
    }

    @Override
    public void close() {
        if(timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void preserveRepositoryAuditTrails() {
        if(auditTask == null) {
            log.info("preserving the audit trails ");
        } else {
            auditTask.resetTime();
        }
        for(String collectionID : SettingsUtils.getAllCollectionsIDs()) {
            performAuditTrailPreservation(collectionID);
        }
    }
    
    /**
     * Performs the audit trails preservation.
     * Uses the AuditPacker to pack the audit trails in a file, then uploads the file to the default file-server, and
     * finally use the PutFileClient to ingest the package into the collection.
     * When the 'put' has completed the Store is updated with the newest preservation sequence numbers for the 
     * contributors.
     * @param collectionId The id of the collection whose audit trails should be preserved.
     */
    private synchronized void performAuditTrailPreservation(String collectionId) {
        try {
            File auditPackage = auditPackers.get(collectionId).createNewPackage();
            URL url = uploadFile(auditPackage);
            log.info("Uploaded the file '" + auditPackage + "' to '" + url.toExternalForm() + "'");
            
            ChecksumDataForFileTYPE checksumData = getValidationChecksumDataForFile(auditPackage);
            
            EventHandler eventHandler = new AuditPreservationEventHandler(
                    auditPackers.get(collectionId).getSequenceNumbersReached(), store, collectionId);
            client.putFile(preservationSettings.getAuditTrailPreservationCollection(), url,
                    auditPackage.getName(), auditPackage.length(), checksumData, null, eventHandler,
                    "Preservation of audit trails from the AuditTrail service.");

            log.debug("Cleanup of the uploaded audit trail package.");
            FileUtils.delete(auditPackage);
        } catch (IOException e) {
            throw new CoordinationLayerException("Cannot perform the preservation of audit trails.", e);
        } catch (OperationFailedException e) {
            throw new CoordinationLayerException("Failed to put the packed audit trails.", e);
        }
    }
    
    /**
     * Helper method to make a checksum for the putfile call. 
     */
    private ChecksumDataForFileTYPE getValidationChecksumDataForFile(File file) {
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settings);
        String checksum = ChecksumUtils.generateChecksum(file, csSpec);

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(csSpec);
        res.setChecksumValue(Base16Utils.encodeBase16(checksum));

        return res;
    } 
    
    /**
     * Uploads the file to a server.
     * @param file The file to upload.
     * @return The URL to the file.
     * @throws IOException If any issues occur with uploading the file.
     */
    private URL uploadFile(File file) throws IOException {
        URL uploadedFileURL = exchange.getURL(file.getName());
        exchange.putFile(new FileInputStream(file), exchange.getURL(file.getName()));
        return uploadedFileURL;
    }
    
    /**
     * Timer task for keeping track of the automated collecting of audit trails.
     */
    private class AuditPreservationTimerTask extends TimerTask {
        /** The interval between running this timer task.*/
        private final long interval;
        /** The date for the next run.*/
        private Date nextRun;
        
        /**
         * Constructor.
         * @param interval The interval between running this timer task.
         */
        private AuditPreservationTimerTask(long interval) {
            this.interval = interval;
            resetTime();
        }
        
        /**
         * Resets the date for next run.
         */
        private void resetTime() {
            nextRun = new Date(System.currentTimeMillis() + interval);
        }
        
        @Override
        public void run() {
            if(nextRun.getTime() < System.currentTimeMillis()) {
                try {
                    log.debug("Time to preserve the audit trails.");
                    preserveRepositoryAuditTrails();
                } catch (Exception e) {
                    log.error("Caught exception while attempting to preserve audittrails", e);
                }
                resetTime();
            }
        }
    }
}
