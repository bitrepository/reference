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

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private final File directory;
    /**
     * Map between the contributor id and the reached preservation sequence number.
     */
    private final Map<String, Long> seqReached = new HashMap<>();
    /**
     * Whether the output stream should be appended to the file.
     */
    private static final boolean APPEND = true;

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
        this.directory = FileUtils.retrieveDirectory(settings.getAuditTrailPreservationTemporaryDirectory());
        this.contributors.addAll(SettingsUtils.getAuditContributorsForCollection(collectionID));

        initialiseReachedSequenceNumbers();
    }

    /**
     * Makes a copy of the map, which is returned.
     *
     * @return A mapping between the contributor ids and their preservation sequence numbers.
     */
    public Map<String, Long> getSequenceNumbersReached() {
        return new HashMap<>(seqReached);
    }

    /**
     * Retrieves the preservation sequence number for each contributor and inserts it into the map.
     */
    private void initialiseReachedSequenceNumbers() {
        for (String contributor : contributors) {
            Long seq = store.getPreservationSequenceNumber(contributor, collectionID);
            seqReached.put(contributor, seq);
        }
    }

    /**
     * Creates a new package with all the newest audit trails from all contributors, e.g. all the audit trails with a
     * larger sequence number than the reached.
     * Cleans up the temporary container afterwards.
     *
     * @return A compressed file with all the audit trails.
     */
    public synchronized File createNewPackage() {
        File container = new File(directory, collectionID + "-audit-trails-" + System.currentTimeMillis());
        try {
            if (container.createNewFile()) {
                packContributors(container);
                return createCompressedFile(container);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot package the newest audit trails.", e);
        } finally {
            // cleaning up.
            if (container.exists()) {
                FileUtils.delete(container);
            }
        }
        return null;
    }

    /**
     * Packs all newest audit trails from every contributor into the given file.
     *
     * @param container The file where the audit trails should be written.
     * @throws IOException If writing to the file somehow fails.
     */
    private void packContributors(File container) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(container, APPEND), StandardCharsets.UTF_8));
            for (String contributor : contributors) {
                packContributor(contributor, writer);
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    /**
     * Writes all the newest audit trails for a single contributor to the PrintWriter.
     *
     * @param contributorId The id of the contributor to write the files for.
     * @param writer        The PrinterWriter where the output will be written.
     */
    private void packContributor(String contributorId, PrintWriter writer) {
        long nextSeqNumber = store.getPreservationSequenceNumber(contributorId, collectionID);
        long largestSeqNumber = -1;
        long numPackedAudits = 0;
        log.debug("Starting to pack AuditTrails for contributor: " + contributorId + " for collection: " + collectionID);
        AuditEventIterator iterator = store.getAuditTrailsByIterator(null, collectionID, contributorId, nextSeqNumber, null,
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
                log.debug("Packed " + numPackedAudits + " AuditTrails in: " + (System.currentTimeMillis() - timeStart) + " ms");
            }
        }
        log.debug("Packed a total of: " + numPackedAudits + " AuditTrails in: " + (System.currentTimeMillis() - timeStart) + " ms");
        if (numPackedAudits > 0) {
            seqReached.put(contributorId, largestSeqNumber);
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
    private File createCompressedFile(File fileToCompress) throws IOException {
        File zippedFile = new File(directory, fileToCompress.getName() + ".zip");
        FileUtils.zipFile(fileToCompress, zippedFile);
        return zippedFile;
    }
}
