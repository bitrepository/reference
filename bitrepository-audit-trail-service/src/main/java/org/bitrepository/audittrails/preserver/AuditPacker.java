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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.common.utils.FileUtils;

/**
 * Performs the extraction and packaging of audit trails into a file.
 * Most of the methods are package protected.
 * 
 * The name of the file with the packed audit trails is 'audit-trail-' and then the timestamp in millis.
 * It has the extension '.zip'.
 */
public class AuditPacker {
    /** The audit trail store.*/
    private final AuditTrailStore store;
    /** The container for the audit trails.*/
    private final File container;
    /** The compressed file. */
    private final File zippedFile ;
    /** The writer for the container.*/
    private final PrintWriter writer;
    
    /** Whether the output stream should be appended to the file.*/
    private static final boolean APPEND = true;
    
    /**
     * Constructor.
     * @param store The audit trail store
     */
    public AuditPacker(AuditTrailStore store) {
        this.store = store;
        container = new File("audit-trails-" + System.currentTimeMillis());
        zippedFile = new File(container.getName() + ".zip");

        try {
            container.createNewFile();
            OutputStream outStream = new FileOutputStream(container, APPEND);
            writer = new PrintWriter(outStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot instantiate l", e);
        }
    }
    
    /**
     * @param contributorId The id of the contributor.
     * @return The sequence number of the next audit trail to be preserved (e.g. largest of the packed
     * sequence numbers plus one).
     */
    long packContributor(String contributorId) {
        
        long nextSeqNumber = store.getPreservationSequenceNumber(contributorId);
        long largestSeqNumber = -1;
        
        Collection<AuditTrailEvent> events = store.getAuditTrails(null, contributorId, nextSeqNumber, 
                null, null, null, null, null);
        
        for(AuditTrailEvent event : events) {
            if(largestSeqNumber < event.getSequenceNumber().longValue()) {
                largestSeqNumber = event.getSequenceNumber().longValue();
            }
            
            writer.print(event.toString());            
        }
        
        return largestSeqNumber + 1;
    }
    
    /**
     * Performs the compression of the file and returns it, ready for ingest.
     * @return The package
     */
    File compressFile() {
        try {
            writer.flush();
            writer.close();
            
            FileUtils.zipFile(container, zippedFile);
            
            return zippedFile; 
        } catch (IOException e) {
            throw new IllegalStateException("", e);
        }
    }
    
    /**
     * Cleans up after the packaging by deleting the local files.
     */
    public void cleanUp() {
        if(container.exists()) {
            FileUtils.delete(container);
        }
        if(zippedFile.exists()) {
            FileUtils.delete(zippedFile);
        }
    }
}
