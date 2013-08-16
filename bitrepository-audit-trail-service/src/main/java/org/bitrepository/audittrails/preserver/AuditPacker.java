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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the extraction and packaging of audit trails for preservation for a specific collection.
 * Only packs the audit trails with a larger sequence number than the reached sequence number for the given 
 * contributor.
 */
public class AuditPacker {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The audit trail store.*/
    private final AuditTrailStore store;
    /** The id of the collection to pack audits for.*/
    private final String collectionId;
    /** The list of contributors for this collection. */
    private final List<String> contributors = new ArrayList<String>();
    /** The directory where the temporary files are stored.*/
    private final File directory;
    /** Map between the contributor id and the reached preservation sequence number. */
    private Map<String, Long> seqReached = new HashMap<String, Long>();
        
    /** Whether the output stream should be appended to the file.*/
    private static final boolean APPEND = true;
    
    /**
     * Constructor.
     * @param store The audit trail store
     */
    public AuditPacker(AuditTrailStore store, AuditTrailPreservation settings, String collectionId) {
        this.store = store;
        this.collectionId = collectionId;
        this.directory = FileUtils.retrieveDirectory(settings.getAuditTrailPreservationTemporaryDirectory());
        this.contributors.addAll(SettingsUtils.getAuditContributorsForCollection(collectionId));
        
        initialiseReachedSequenceNumbers();
    }
    
    /**
     * Makes a copy of the map, which is returned.
     * @return A mapping between the contributor ids and their preservation sequence numbers.
     */
    public Map<String, Long> getSequenceNumbersReached() {
        return new HashMap<String, Long>(seqReached);
    }
    
    /**
     * Retrieves the preservation sequence number for each contributor and inserts it into the map.
     */
    private void initialiseReachedSequenceNumbers() {
        for(String contributor : contributors) {
            Long seq = store.getPreservationSequenceNumber(contributor, collectionId);
            seqReached.put(contributor, seq);
        }
    }
    
    /**
     * Creates a new package with all the newest audit trails from all contributors, e.g. all the audit trails with a 
     * larger sequence number than the reached.
     * Cleans up the temporary container afterwards.
     * @return A compressed file with all the audit trails. 
     */
    public synchronized File createNewPackage() {
        File container = new File(directory, collectionId + "-audit-trails-" + System.currentTimeMillis());
        try {
            container.createNewFile();
            packContributors(container);
            return createCompressedFile(container);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot package the newest audit trails.", e);
        } finally {
            // cleaning up.
            if(container.exists()) {
                FileUtils.delete(container);
            }
        }
    }
    
    /**
     * Packs all newest audit trails from every contributor into the given file.
     * @param container The file where the audit trails should be written.
     * @throws IOException If writing to the file somehow fails.
     */
    private void packContributors(File container) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(container, APPEND));
            for(String contributor : contributors) {
                Long seq = packContributor(contributor, writer);
                seqReached.put(contributor, seq);
            }
        } finally {
            if(writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }
    
    /**
     * Writes all the newest audit trails for a single contributor to the PrintWriter.
     * @param contributorId The id of the contributor to write the files for.
     * @param writer The PrinterWriter where the output will be written.
     * @return The sequence number reached +1 (to tell which sequence number is next).
     */
    private Long packContributor(String contributorId, PrintWriter writer) {
        long nextSeqNumber = store.getPreservationSequenceNumber(contributorId, collectionId);
        long largestSeqNumber = -1;
        long numPackedAudits = 0;
        log.debug("Starting to pack audittrails for contributor: " + contributorId + " for collection: " + collectionId);
        AuditEventIterator iterator = store.getAuditTrailsByIterator(null, null, contributorId, nextSeqNumber, 
                null, null, null, null, null, null);
        Long timeStart = System.currentTimeMillis();
        long interval = 1000;
        
        AuditTrailEvent event;
        log.debug("AuditEventIterator created");
        while((event = iterator.getNextAuditTrailEvent()) != null) {
            numPackedAudits++;
            if(largestSeqNumber < event.getSequenceNumber().longValue()) {
                largestSeqNumber = event.getSequenceNumber().longValue();
            }
            writer.println(event.toString());
            
            if((numPackedAudits % interval) == 0) {
                log.debug("Packed " + numPackedAudits + " audittrails in: " + (System.currentTimeMillis() - timeStart) + " ms");
            }
        }
        log.debug("Packed a total of: " + numPackedAudits + " audittrails in: " + (System.currentTimeMillis() - timeStart) + " ms");
        return largestSeqNumber + 1;
        /*
        Collection<AuditTrailEvent> events = store.getAuditTrails(null, null, contributorId, nextSeqNumber, 
                null, null, null, null, null, null);
        
        for(AuditTrailEvent event : events) {
            if(largestSeqNumber < event.getSequenceNumber().longValue()) {
                largestSeqNumber = event.getSequenceNumber().longValue();
            }
            writer.println(event.toString());            
        }
        
        return largestSeqNumber + 1;*/
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
