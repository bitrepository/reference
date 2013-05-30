/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.cache.database;

import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.filestore.FileInfo;
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
     * @param fileInfo The fileinfo for the file to insert.
     */
    public void insertFileInfo(FileInfo fileInfo) {
        insertFileID(fileInfo.getFileID(), BigInteger.valueOf(fileInfo.getSize()), 
                CalendarUtils.getFromMillis(fileInfo.getMdate()));
    }
    
    /**
     * Inserts a file id with date, but without the size.
     * Intended for the ChecksumPillar, which cannot deliver any size.
     * @param fileId The id of the file.
     * @param lastModified The last modified timestamp.
     */
    public void insertFileID(String fileId, Date lastModified) {
        insertFileID(fileId, null, CalendarUtils.getXmlGregorianCalendar(lastModified));
    }
    
    /**
     * Adds a file id to this result set. 
     * @param fileID The id of the file to add.
     * @param size The size of the file.
     * @param lastModified The last modified date for the file.
     */
    public void insertFileID(String fileID, BigInteger size, XMLGregorianCalendar lastModified) {
        FileIDsDataItem item = new FileIDsDataItem();
        item.setFileID(fileID);
        item.setFileSize(size);
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
