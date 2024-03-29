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

import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the extraction and packaging of audit trails for preservation for a specific collection.
 * Only packs the audit trails with a larger sequence number than the reached sequence number for the given
 * contributor.
 */
public class AuditPacker {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AuditTrailStore store;
    private final String collectionID;
    private final List<String> contributors = new ArrayList<>();
    /**
     * The directory where the temporary files are stored.
     */
    private final Path directory;
    /**
     * Map between the contributor id and the reached preservation sequence number.
     */
    private final Map<String, Long> seqNumsReached = new HashMap<>();

    private long packedAuditCount = 0;

    /**
     * Constructor.
     *
     * @param store        The audit trail store
     * @param settings     the settings
     * @param collectionID the collection ID
     */
    public AuditPacker(AuditTrailStore store, AuditTrailPreservation settings, String collectionID) {
        this.store = store;
        this.collectionID = collectionID;
        this.directory = FileUtils.retrieveDirectory(settings.getAuditTrailPreservationTemporaryDirectory()).toPath();
        this.contributors.addAll(SettingsUtils.getAuditContributorsForCollection(collectionID));

        initializeReachedSequenceNumbers();
    }

    /**
     * Makes a copy of the map, which is returned.
     *
     * @return A mapping between the contributor ids and their preservation sequence numbers.
     */
    public Map<String, Long> getSequenceNumbersReached() {
        return new HashMap<>(seqNumsReached);
    }

    /**
     * Retrieves the preservation sequence number for each contributor and inserts it into the map.
     */
    private void initializeReachedSequenceNumbers() {
        for (String contributor : contributors) {
            Long seqNum = store.getPreservationSequenceNumber(contributor, collectionID);
            seqNumsReached.put(contributor, seqNum);
        }
    }

    /**
     * Creates a new package with all the newest audit trails from all contributors, e.g. all the audit trails with a
     * larger sequence number than the reached.
     * Cleans up the temporary container afterwards.
     *
     * @return A compressed file with all the audit trails.
     */
    public synchronized Path createNewPackage() throws IOException {
        resetPackedAuditCount();
        Path auditTrailsFile = directory.resolve(collectionID + "-audit-trails-" + System.currentTimeMillis());
        try {
            Files.createFile(auditTrailsFile);
            packContributors(auditTrailsFile);
            return createCompressedFile(auditTrailsFile);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot package the newest audit trails.", e);
        } finally {
            Files.deleteIfExists(auditTrailsFile);
        }
    }

    /**
     * Resets {@link #packedAuditCount}.
     * Done before creating a new package to ensure only new packed audits are counted.
     */
    private void resetPackedAuditCount() {
        packedAuditCount = 0;
    }

    /**
     * Packs all newest audit trails from every contributor into the given file.
     *
     * @param container The file where the audit trails should be written.
     * @throws IOException If writing to the file somehow fails.
     */
    private void packContributors(Path container) throws IOException {
        try (OutputStream os = Files.newOutputStream(container, StandardOpenOption.APPEND);
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            for (String contributor : contributors) {
                packContributor(contributor, writer);
            }
        }
    }

    /**
     * Writes all the newest audit trails for a single contributor to the PrintWriter.
     *
     * @param contributorID The id of the contributor to write the files for.
     * @param writer        The PrinterWriter where the output will be written.
     */
    private void packContributor(String contributorID, PrintWriter writer) {
        long nextSeqNumber = store.getPreservationSequenceNumber(contributorID, collectionID) + 1;
        long largestSeqNumber = -1;
        long numPackedAudits = 0;
        log.debug("Starting to pack AuditTrails at seq-number {} for contributor: {} for collection: {}",
                nextSeqNumber, contributorID, collectionID);
        AuditEventIterator iterator = store.getAuditTrailsByIterator(null, collectionID, contributorID, nextSeqNumber, null,
                null, null, null, null, null, null);
        long timeStart = System.currentTimeMillis();
        long logInterval = 1000;

        AuditTrailEvent event;
        log.debug("AuditEventIterator created");
        while ((event = iterator.getNextAuditTrailEvent()) != null) {
            numPackedAudits++;
            if (largestSeqNumber < event.getSequenceNumber().longValue()) {
                largestSeqNumber = event.getSequenceNumber().longValue();
            }
            writer.println(event);

            if ((numPackedAudits % logInterval) == 0) {
                log.debug("Packed {} AuditTrails in: {} ms", numPackedAudits, System.currentTimeMillis() - timeStart);
            }
        }
        log.debug("Packed a total of: {} AuditTrails in: {} ms",
                numPackedAudits, System.currentTimeMillis() - timeStart);

        if (numPackedAudits > 0) {
            packedAuditCount += numPackedAudits;
            seqNumsReached.put(contributorID, largestSeqNumber);
        }
    }

    /**
     * Compresses the given file into a zip-file.
     * It will have the same name as the file to compress, just with the extension '.zip', and it will
     * be placed in the directory defined in settings.
     *
     * @param fileToCompress The file to compress.
     * @return The compressed file.
     * @throws IOException If anything goes wrong.
     */
    private Path createCompressedFile(Path fileToCompress) throws IOException {
        Path zippedFile = directory.resolve(fileToCompress.getFileName() + ".zip");
        FileUtils.zipFile(fileToCompress, zippedFile);
        return zippedFile;
    }

    /**
     * Get the last count of packed audits i.e. count of new audits from all contributors for the collection.
     *
     * @return Count of packed audits.
     */
    public long getPackedAuditCount() {
        return packedAuditCount;
    }
}
