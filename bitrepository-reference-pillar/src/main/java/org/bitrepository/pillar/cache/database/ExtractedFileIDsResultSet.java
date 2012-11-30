package org.bitrepository.pillar.cache.database;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Container of the results of a checksum database extraction for file ids. 
 */
public class ExtractedFileIDsResultSet {
    /** The results of the file ids extraction.*/
    protected final FileIDsData results;
    /** Whether more results has been found.*/
    protected boolean moreEntriesReported;
    
    /**
     * Constructor.
     */
    public ExtractedFileIDsResultSet() {
        results = new FileIDsData();
        results.setFileIDsDataItems(new FileIDsDataItems());
        moreEntriesReported = false;
    }
    
    /**
     * Adds a file id to this result set. 
     * @param fileID The id of the file to add.
     * @param lastModified The last modified date for the file.
     */
    public void insertFileID(String fileID, Date lastModified) {
        insertFileID(fileID, CalendarUtils.getXmlGregorianCalendar(lastModified));
    }
    
    /**
     * Adds a file id to this result set. 
     * @param fileID The id of the file to add.
     * @param lastModified The last modified date for the file.
     */
    public void insertFileID(String fileID, XMLGregorianCalendar lastModified) {
        FileIDsDataItem item = new FileIDsDataItem();
        item.setFileID(fileID);
        item.setLastModificationTime(lastModified);
        results.getFileIDsDataItems().getFileIDsDataItem().add(item);
    }
    
    /**
     * @return The data results of the extraction.
     */
    public FileIDsData getEntries() {
        return results;
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
