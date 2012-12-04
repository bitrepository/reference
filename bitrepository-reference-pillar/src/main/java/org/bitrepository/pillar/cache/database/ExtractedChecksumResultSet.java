package org.bitrepository.pillar.cache.database;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;

/**
 * Container of the results of a checksum database exctraction.
 */
public class ExtractedChecksumResultSet {
    /** The list of checksum entries.*/
    protected final List<ChecksumDataForChecksumSpecTYPE> entries;
    /** Whether more results has been found.*/
    protected boolean moreEntriesReported;
    
    /**
     * Constructor.
     */
    public ExtractedChecksumResultSet() {
        entries = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        moreEntriesReported = false;
    }
    
    /**
     * Adds an entry to this result set. 
     * @param entry The entry to add.
     */
    public void insertChecksumEntry(ChecksumDataForChecksumSpecTYPE entry) {
        entries.add(entry);
    }
    
    /**
     * Adds an entry to this result set. 
     * @param entry The entry to add.
     */
    public void insertChecksumEntry(ChecksumEntry entry) {
        ChecksumDataForChecksumSpecTYPE res = new ChecksumDataForChecksumSpecTYPE();
        res.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
        res.setChecksumValue(Base16Utils.encodeBase16(entry.getChecksum()));
        res.setFileID(entry.getFileId());
        entries.add(res);
    }
    
    /**
     * @return A list with all the reported entries.
     */
    public List<ChecksumDataForChecksumSpecTYPE> getEntries() {
        return new ArrayList<ChecksumDataForChecksumSpecTYPE>(entries);
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
