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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.audittrails.AuditTrailTaskStarter;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.audittrails.webservice.PreservationInfo;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.TimerTaskSchedule;
import org.bitrepository.common.exceptions.OperationFailedException;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.common.utils.XmlUtils;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles the preservation of audit trails to a collection defined for the local repository.
 * This means, that each set of audit trails will be preserved within its own collection.
 */
public class LocalAuditTrailPreserver extends AuditTrailTaskStarter implements AuditTrailPreserver {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BlockingPutFileClient client;
    private final Map<String, AuditPacker> auditPackers = new HashMap<>();
    private final AuditTrailPreservation preservationSettings;
    private final FileExchange exchange;
    private Timer timer;
    private AuditPreservationTimerTask preservationTask = null;
    private long preservedAuditCount = 0;

    /**
     * @param settings The preservationSettings for the audit trail service.
     * @param store    The storage of the audit trails, which should be preserved.
     * @param client   The PutFileClient for putting the audit trail packages to the collection.
     */
    public LocalAuditTrailPreserver(Settings settings, AuditTrailStore store, PutFileClient client, FileExchange exchange) {
        super(settings, store);
        ArgumentValidator.checkNotNull(client, "PutFileClient client");

        this.preservationSettings = settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation();
        this.client = new BlockingPutFileClient(client);
        this.exchange = exchange;
        for (String collectionID : SettingsUtils.getAllCollectionsIDs()) {
            this.auditPackers.put(collectionID, new AuditPacker(store, preservationSettings, collectionID));
        }
        initializeDatabaseEntries();
    }

    /**
     * Ensures that the collection and contributor entries exist in the database and adds a preservation entry
     * for each pairing.
     */
    private void initializeDatabaseEntries() {
        log.debug("Initializing collections and contributors in db.");
        List<String> collections = SettingsUtils.getAllCollectionsIDs();
        for (String collectionID : collections) {
            store.addCollection(collectionID);
            for (String contributorID : SettingsUtils.getAuditContributorsForCollection(collectionID)) {
                store.addContributor(contributorID);

                if (!store.hasPreservationKey(contributorID, collectionID)) {
                    store.setPreservationSequenceNumber(contributorID, collectionID, 0L);
                }
            }
        }
    }

    @Override
    public void start() {
        if (timer != null) {
            log.debug("Cancelling old timer.");
            timer.cancel();
        }
        javax.xml.datatype.Duration preservationIntervalXmlDur = preservationSettings.getAuditTrailPreservationInterval();
        Duration preservationInterval = XmlUtils.xmlDurationToDuration(preservationIntervalXmlDur);
        long timerCheckIntervalMillis = preservationInterval.dividedBy(10).toMillis();
        Duration preservationGracePeriod = getGracePeriod();
        log.info("Starting preservation of audit trails every {} after grace period of {}.",
                TimeUtils.durationToHuman(preservationInterval), TimeUtils.durationToHuman(preservationGracePeriod));
        timer = new Timer(true);
        preservationTask = new AuditPreservationTimerTask(preservationInterval.toMillis(),
                Math.toIntExact(preservationGracePeriod.toMillis()));
        timer.scheduleAtFixedRate(preservationTask, preservationGracePeriod.toMillis(), timerCheckIntervalMillis);
    }

    @Override
    public void close() {
        if (timer != null) {
            preservationTask.cancel();
            timer.cancel();
        }
    }

    @Override
    public void preserveRepositoryAuditTrails() {
        resetPreservedAuditCount();
        for (String collectionID : SettingsUtils.getAllCollectionsIDs()) {
            performAuditTrailPreservation(collectionID);
        }
    }

    /**
     * Resets the preserved audit trail count so newly preserved audits can be counted.
     */
    private void resetPreservedAuditCount() {
        preservedAuditCount = 0;
    }

    /**
     * Performs the audit trails preservation.
     * Uses the AuditPacker to pack the audit trails in a file, then uploads the file to the default file-server, and
     * finally use the PutFileClient to ingest the package into the collection.
     * When the 'put' has completed the Store is updated with the newest preservation sequence numbers for the
     * contributors.
     *
     * @param collectionID The id of the collection whose audit trails should be preserved.
     */
    private synchronized void performAuditTrailPreservation(String collectionID) {
        try {
            AuditPacker auditPacker = auditPackers.get(collectionID);
            File auditPackage = auditPacker.createNewPackage().toFile();

            if (auditPacker.getPackedAuditCount() > 0) {
                URL fileURL = uploadFile(auditPackage);
                log.info("Uploaded the file '{}' to '{}'", auditPackage, fileURL.toExternalForm());

                ChecksumDataForFileTYPE checksumData = getValidationChecksumDataForFile(auditPackage);

                EventHandler eventHandler = new AuditPreservationEventHandler(auditPacker.getSequenceNumbersReached(),
                        store, collectionID);
                client.putFile(preservationSettings.getAuditTrailPreservationCollection(), fileURL, auditPackage.getName(),
                        auditPackage.length(), checksumData, null, eventHandler,
                        "Preservation of audit trails from the AuditTrail service.");

                preservedAuditCount += auditPacker.getPackedAuditCount();

                try {
                    exchange.deleteFile(fileURL);
                } catch (IOException | URISyntaxException e) {
                    log.error("Failed cleaning up file '{}' at {}", auditPackage.getName(), fileURL.toExternalForm());
                }
            } else {
                log.info("No new audit trails to preserve for collection '{}'. No preservation file uploaded.",
                        collectionID);
            }

            log.debug("Cleanup of the audit trail package.");
            FileUtils.delete(auditPackage);
        } catch (IOException e) {
            throw new CoordinationLayerException("Cannot perform the preservation of audit trails.", e);
        } catch (OperationFailedException e) {
            throw new CoordinationLayerException("Failed to put the packed audit trails.", e);
        } catch (DecoderException e) {
            throw new CoordinationLayerException("Failed to encode the checksum.", e);
        }
    }

    /**
     * Helper method to make a checksum for the PutFile call.
     */
    private ChecksumDataForFileTYPE getValidationChecksumDataForFile(File file) throws DecoderException {
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
     *
     * @param file The file to upload.
     * @return The URL to the file.
     * @throws IOException If any issues occur with uploading the file.
     */
    private URL uploadFile(File file) throws IOException {
        URL uploadedFileURL = exchange.getURL(file.getName());
        exchange.putFile(new FileInputStream(file), uploadedFileURL);
        return uploadedFileURL;
    }

    @Override
    public PreservationInfo getPreservationInfo() {
        PreservationInfo info = new PreservationInfo();
        info.setCollectionID(preservationSettings.getAuditTrailPreservationCollection());

        Date lastStart = preservationTask.getLastPreservationStart();
        Date lastFinish = preservationTask.getLastPreservationFinish();
        Date nextStart = preservationTask.getNextScheduledRun();

        // Need to handle the case where preservation has not started/finished yet.
        if (lastStart != null) {
            info.setLastStart(TimeUtils.shortDate(lastStart));
            if (lastFinish != null) {
                long lastDurationMS = lastFinish.getTime() - lastStart.getTime();
                info.setLastDuration(TimeUtils.millisecondsToHuman(lastDurationMS));
            } else {
                info.setLastDuration("Preservation has not finished yet");
            }
        } else {
            info.setLastStart("Audit trail preservation has not started yet");
            info.setLastDuration("Not available");
        }
        info.setNextStart(TimeUtils.shortDate(nextStart));
        info.setPreservedAuditCount(preservedAuditCount);
        return info;
    }

    /**
     * Timer task for keeping track of the automated collecting of audit trails.
     */
    private class AuditPreservationTimerTask extends TimerTask {
        private final Logger log = LoggerFactory.getLogger(getClass());
        private final TimerTaskSchedule schedule;

        /**
         * @param interval The interval between running this timer task.
         */
        // TODO: Replace old time representation (https://sbforge.org/jira/browse/BITMAG-1180)
        private AuditPreservationTimerTask(long interval, int gracePeriod) {
            this.schedule = new TimerTaskSchedule(interval, gracePeriod);
        }

        public Date getNextScheduledRun() {
            return schedule.getNextRun();
        }

        public Date getLastPreservationStart() {
            return schedule.getLastStart();
        }

        public Date getLastPreservationFinish() {
            return schedule.getLastFinish();
        }

        @Override
        public void run() {
            if (getNextScheduledRun().getTime() < System.currentTimeMillis()) {
                log.info("Starting preservation of audit trails.");
                schedule.start();
                preserveRepositoryAuditTrails();
                schedule.finish();
                log.info("Finished preservation. Scheduled new preservation task to start {}", getNextScheduledRun());
            }
        }
    }
}
