/*
 * #%L
 * Bitmagasin
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.store;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage model for the file data.
 * Involves having the checksums in a checksum store (database), 
 * and potentially having the actual files in a file store (file system).
 * Handles all requests or operations regarding files.
 */
public abstract class StorageModel {

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The storage of checksums.*/
    protected final ChecksumStore cache;
    /** The file archives for the different collections. Can be null, if the pillar has no actual files 
     * (e.g. ChecksumPillar). */
    protected FileStore fileArchive;
    /** The default checksum specification.*/
    protected final ChecksumSpecTYPE defaultChecksumSpec;
    /** The dispatcher of alarms.*/
    protected final AlarmDispatcher alarmDispatcher;
    /** The settings.*/
    protected final Settings settings;

    /**
     * @param archives The archive with the data.
     * @param cache The storage for the checksums.
     * @param alarmDispatcher The alarm dispatcher.
     * @param settings The configuration to use.
     */
    protected StorageModel(FileStore archives, ChecksumStore cache, AlarmDispatcher alarmDispatcher, 
            Settings settings) {
        this.cache = cache;
        this.fileArchive = archives;
        this.alarmDispatcher = alarmDispatcher;
        this.settings = settings;
        this.defaultChecksumSpec = ChecksumUtils.getDefault(settings);
    }
    
    /**
     * Retrieval of the pillar id.
     * @return The id of the pillar.
     */
    public String getPillarID() {
        return settings.getComponentID();
    }

    /**
     * Checks whether the pillar has an entry with the file-id for the given collection.
     * It will potentially verify that the actual file is consistent with the data in the cache.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @return Whether the pillar has an entry for the file.
     */
    public boolean hasFileID(String fileID, String collectionID) {
        if(cache.hasFile(fileID, collectionID)) {
            verifyFileToCacheConsistencyIfRequired(fileID, collectionID);
            return true;
        } 
        return false;
    }

    /**
     * Retrieves the checksum for a given file at the given collection with the given checksum specification.
     * If it is the default checksum specification, then the cached checksum is returned (though it might verify the 
     * consistency between the actual file and the cache before).
     * If it otherwise requests a non-default checksum specification, then it will be handled differently for the 
     * Full ReferencePillar and the ChecksumPillar.
     * The Full ReferencePillar will recalculated the default checksum for the file and updated in the database, along 
     * with calculating the new checksum specification for the file which will be returned.
     * 
     * @param fileID The id of the file whose checksum is requested.
     * @param collectionID The id of the collection of the file.
     * @param csType The type of checksum.
     * @return The requested type of checksum for the given file.
     * @throws RequestHandlerException If a non-default checksum is requested for a checksum-pillar.
     */
    public String getChecksumForFile(String fileID, String collectionID, ChecksumSpecTYPE csType) 
            throws RequestHandlerException {
        if(csType.equals(defaultChecksumSpec)) {
            verifyFileToCacheConsistencyIfRequired(fileID, collectionID);
            return cache.getChecksum(fileID, collectionID);            
        } else {
            return getNonDefaultChecksum(fileID, collectionID, csType);
        }
    }

    /**
     * Retrieves the checksum entry for a given file at the given collection with the given checksum specification.
     * If it is the default checksum specification, then the entry in the case is returned (though it might verify the 
     * consistency between the actual file and the cache before).
     * If it otherwise requests a non-default checksum specification, then it will be handled differently for the 
     * Full ReferencePillar and the ChecksumPillar.
     * 
     * @param fileId The id of the file whose checksum is requested.
     * @param collectionId The id of the collection of the file.
     * @param csType The type of checksum.
     * @return The entry for the requested type of checksum for the given file.
     * @return {@link RequestHandlerException} If a non-default checksum is requested from a ChecksumPillar.
     */
    public ChecksumEntry getChecksumEntryForFile(String fileId, String collectionId, ChecksumSpecTYPE csType) 
            throws RequestHandlerException {
        if(csType.equals(defaultChecksumSpec)) {
            verifyFileToCacheConsistencyIfRequired(fileId, collectionId);
            return cache.getEntry(fileId, collectionId);            
        } else {
            String checksum = getNonDefaultChecksum(fileId, collectionId, csType);
            return new ChecksumEntry(fileId, checksum, new Date());
        }
    }

    /**
     * Retrieves the entry for a given file with a given checksumSpec in the ChecksumDataForFileTYPE format.
     * @param fileId The id of the file to retrieve the data from.
     * @param collectionId The id of the collection of the file.
     * @param csType The type of checksum to calculate.
     * @return The entry encapsulated in the ChecksumDataForFileTYPE data format.
     */
    public ChecksumDataForFileTYPE getChecksumDataForFile(String fileId, String collectionId, ChecksumSpecTYPE csType) 
            throws RequestHandlerException {
        ChecksumEntry entry = getChecksumEntryForFile(fileId, collectionId, csType);
        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
        res.setChecksumSpec(csType);
        res.setChecksumValue(Base16Utils.encodeBase16(entry.getChecksum()));
        return res;
    }
    
    /**
     * Ensures, that all files are up to date, and retrieves the requested entries.
     * @param minTimestamp The minimum date for the timestamp of the extracted checksum entries.
     * @param maxTimestamp The maximum date for the timestamp of the extracted checksum entries.
     * @param maxResults The maximum number of results.
     * @param collectionID The id of the collection.
     * @param csSpec The checksum specification.
     * @return If default checksum, then the checksum entries from the store. Otherwise calculate all the requested
     * checksums.
     * @throws RequestHandlerException If it is a non-default checksum specification, which is not supported (e.g. if
     * it is a ChecksumPillar).
     */
    public ExtractedChecksumResultSet getChecksumResultSet(XMLGregorianCalendar minTimestamp, 
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID, ChecksumSpecTYPE csSpec) 
                    throws RequestHandlerException {
        verifyFileToCacheConsistencyOfAllDataIfRequired(collectionID);
        if(csSpec.equals(defaultChecksumSpec)) {
            return cache.getChecksumResults(minTimestamp, maxTimestamp, maxResults, collectionID);
        } else {
            log.info("Bulk-extraction of non-default checksums for spec: " + csSpec 
                    + ", on collection " + collectionID + ", with maximum " + maxResults + " results.");
            // We ignore minTimestamp and maxTimestamp when dealing with non-default checksums.
            return getNonDefaultChecksumResultSet(maxResults, collectionID, csSpec);
        }
    }
    
    /**
     * Extracts the results set for a given checksum entry.
     * If it has the file, but its calculation date is not within the timestamp restrictions, then an empty 
     * resultset is returned.
     * @param fileID The ID of the file.
     * @param collectionID The id of the collection.
     * @param minTimestamp Optional restrictions for the minimum timestamp for the checksum calculation date.
     * @param maxTimestamp Optional restrictions for the maximum timestamp for the checksum calculation date.
     * @param csSpec The checksum specification.
     * @return The extracted checksum result set.
     * @throws RequestHandlerException If it is not possible to extract the checksum result, e.g. due to unsupported 
     * checksum specification.
     */
    public ExtractedChecksumResultSet getSingleChecksumResultSet(String fileID, String collectionID, 
            XMLGregorianCalendar minTimestamp, XMLGregorianCalendar maxTimestamp, ChecksumSpecTYPE csSpec) 
                    throws RequestHandlerException {
        ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
        ChecksumEntry entry = getChecksumEntryForFile(fileID, collectionID, csSpec);
        if((minTimestamp == null || CalendarUtils.convertFromXMLGregorianCalendar(minTimestamp).getTime() 
                <= entry.getCalculationDate().getTime()) && (maxTimestamp == null || 
                CalendarUtils.convertFromXMLGregorianCalendar(maxTimestamp).getTime() 
                >= entry.getCalculationDate().getTime())) {
            res.insertChecksumEntry(entry);
        }
        return res;
    }
    
    /**
     * Removes the entry for the given file.
     * Both from the cache, and if an archive exists, then also removed/deprecated the actual file.
     * @param fileID The id of the file to remove.
     * @param collectionID The id of the collection of the file.
     */
    public void deleteFile(String fileID, String collectionID) {
        if(fileArchive != null) {
            fileArchive.deleteFile(fileID, collectionID);
        }
        cache.deleteEntry(fileID, collectionID);
    }
    
    /**
     * Ensuring that the file is not in tmpDir is only relevant, if the file-archives exists.
     * @param fileID The id of the file to ensure not exist in tmpDir.
     * @param collectionID The id of the collection
     */
    public void ensureFileNotInTmpDir(String fileID, String collectionID) {
        if(fileArchive != null) {
            fileArchive.ensureFileNotInTmpDir(fileID, collectionID);
        }
    }
    
    /**
     * Verifies the handling of a specific checksum algorithm.
     * 
     * @param checksumSpec
     * @param collectionID
     * @throws RequestHandlerException
     */
    public void verifyChecksumAlgorithm(ChecksumSpecTYPE checksumSpec, String collectionID) 
            throws RequestHandlerException{
        if(checksumSpec == null) {
            return;
        }
        
        // Validate against ChecksumPillar specific algorithm (if is a ChecksumPillar).
        if(getChecksumPillarSpec() != null && !(getChecksumPillarSpec().equals(checksumSpec))) {
            throw new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED, "Cannot handle the checksum "
                    + "specification '" + checksumSpec + "'.This checksum pillar can only handle '" 
                    + getChecksumPillarSpec() + "'", collectionID);
        }

        try {
            ChecksumUtils.verifyAlgorithm(checksumSpec);
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidMessageException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, e.getMessage(), 
                    collectionID, e);
        }
    }

    /**
     * Retrieves the ChecksumEntry for the given file in the given collection, where the checksum is calculated with the
     * non-default checksum specification.
     * 
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @param csType The non-defaults checksum specification.
     * @return The requested checksum entry.
     * @throws RequestHandlerException If the checksum specification cannot be handled (e.g. if this is a 
     * ChecksumPillar). 
     */
    protected ChecksumEntry retrieveNonDefaultChecksumEntry(String fileID, String collectionID, 
            ChecksumSpecTYPE csType) throws RequestHandlerException {
        String checksum = getNonDefaultChecksum(fileID, collectionID, csType);
        return new ChecksumEntry(fileID, checksum, new Date());
    }

    /**
     * Retrieves the checksum specification, if it is a ChecksumPillar.
     * A Full ReferencePillar must return null here.
     * @return The checksum specification if it is a ChecksumPillar, otherwise it returns null.
     */
    public abstract ChecksumSpecTYPE getChecksumPillarSpec();

    /**
     * Verifies that there is enough space left for the given collection for a file with the given fileSize.
     * @param fileSize The size for the file to have enough space left for.
     * @param collectionID The id of the collection.
     * @throws RequestHandlerException If it is not possible to store a file with the given size within the
     * archive of the given collection. 
     */
    public abstract void verifyEnoughFreeSpaceLeftForFile(Long fileSize, String collectionID) 
            throws RequestHandlerException;

    /**
     * Handles the ReplaceFile operation.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @param fileAddress The address to retrieve the file from.
     * @param validationChecksum The expected checksum for the file (validate when it has been downloaded).
     * @throws RequestHandlerException If something goes wrong, e.g. the downloaded file does not have the expected
     * checksum.
     */
    public abstract void replaceFile(String fileID, String collectionID, String fileAddress, 
            ChecksumDataForFileTYPE validationChecksum) throws RequestHandlerException;
    
    /**
     * Handles the PutFile operation.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @param fileAddress The address to retrieve the file from.
     * @param validationChecksum The expected checksum for the file (validate when it has been downloaded).
     * @throws RequestHandlerException If something goes wrong, e.g. the downloaded file does not have the expected
     * checksum.
     */
    public abstract void putFile(String collectionID, String fileID, String fileAddress, 
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException;

    /**
     * Verify the consistency between all the data in the file archive and in the cache, if it is required by the 
     * settings.
     * This will not be performed by ChecksumPillars, since they do not have file archive.
     * @param collectionID The id of the collection to verify the consistency for.
     */
    public abstract void verifyFileToCacheConsistencyOfAllDataIfRequired(String collectionID);
    
    /**
     * Verify the consistency for a given file in the file archive and in the cache, if it is required by the settings.
     * This will not be performed by ChecksumPillars, since they do not have file archive.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection to verify the consistency for.
     */
    protected abstract void verifyFileToCacheConsistencyIfRequired(String fileID, String collectionID);

    /**
     * Validates that all files in the cache is also in the archive, and that all files in the archive
     * is also in the cache.
     * @param collectionId The id of the collection where the data should be ensured.
     */
    public abstract void verifyFileToCacheConsistencyOfAllData(String collectionID);

    /**
     * Retrieves the non-default checksum for a file, thus calculating the checksum of the file with the new 
     * checksum specification.
     * It will also recalculate the default checksum and update the cache with it.
     * A ChecksumPillar will throw an exception, since it does not have the actual files to calculate checksums with.
     * @param fileId The id of the file.
     * @param collectionID The id of the collection.
     * @param csType The checksum specification to calculate the checksum with.
     * @return The checksum of the file calculated with the given checksum specification.
     * @throws RequestHandlerException If it is a ChecksumPillar.
     */
    protected abstract String getNonDefaultChecksum(String fileId, String collectionID, ChecksumSpecTYPE csType) 
            throws RequestHandlerException;
    
    /**
     * Retrieves the FileInfo for the actual file.
     * Will throw an exception for the ChecksumPillar.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @return The fileInfo for the file.
     * @throws RequestHandlerException If it is a ChecksumPillar.
     */
    public abstract FileInfo getFileInfoForActualFile(String fileID, String collectionID) 
            throws RequestHandlerException;
    
    /**
     * Extracts a set of file ids according to the given restrictions.
     * @param fileID The id of the file (thus only the id of a specific file).
     * @param minTimestamp The minimum date for the LastModifiedDate of the files.
     * @param maxTimestamp The maximum date for the LastModifiedDate of the files.
     * @param maxResults The maximum number of results.
     * @param collectionID The id of the collection.
     * @return The extracted file ids.
     */
    public abstract ExtractedFileIDsResultSet getFileIDsResultSet(String fileID, XMLGregorianCalendar minTimestamp, 
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID);
    
    /**
     * Retrieves the checksums with a non-default checksum specification for some files.
     * @param maxResults The maximum number of results.
     * @param collectionID The id of the collection.
     * @param csSpec The checksum specification.
     * @return A set of checksum-results for the non-default checksum specification.
     * @throws RequestHandlerException If the non-default checksum specification is not supported, e.g. if it is a
     * ChecksumPillar.
     */
    protected abstract ExtractedChecksumResultSet getNonDefaultChecksumResultSet(Long maxResults, String collectionID, 
            ChecksumSpecTYPE csSpec) throws RequestHandlerException;

    /**
     * Throws an exception unless the actual file exists and is available.
     * Thus the ChecksumPillar will always throw an exception.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @throws RequestHandlerException If it is a ChecksumPillar, or if the file does not exist.
     */
    public abstract void verifyFileExists(String fileID, String collectionID) throws RequestHandlerException;

    /**
     * Closes the pillar model.
     */
    public void close() {
        cache.close();
        if(fileArchive != null) {
            fileArchive.close();
        }
    }
}
