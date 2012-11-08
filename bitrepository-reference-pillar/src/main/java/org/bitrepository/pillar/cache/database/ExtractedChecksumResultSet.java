package org.bitrepository.pillar.cache.database;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.pillar.cache.ChecksumEntry;

/**
 * Container of the results of a checksum database exctraction.
 */
public class ExtractedChecksumResultSet {
    /** The list of checksum entries.*/
    protected final List<ChecksumEntry> entries;
    /** Whether more results has been found.*/
    protected boolean moreEntriesReported;
    
    /**
     * Constructor.
     */
    public ExtractedChecksumResultSet() {
        entries = new ArrayList<ChecksumEntry>();
        moreEntriesReported = false;
    }
    
    /**
     * Adds an entry to this result set. 
     * @param entry The entry to add.
     */
    public void insertChecksumEntry(ChecksumEntry entry) {
        entries.add(entry);
    }
    
    /**
     * @return A list with all the reported entries.
     */
    public List<ChecksumEntry> getEntries() {
        return new ArrayList<ChecksumEntry>(entries);
    }
    
    /**
     * Set that more entries has been found. 
     */
    public void reportMoreEntriesFound() {
        moreEntriesReported = true;
    }
    
    /**
     * @return Whether it has been reported, that more results exists.
     */
    public boolean hasMoreEntries() {
        return moreEntriesReported;
    }
}
