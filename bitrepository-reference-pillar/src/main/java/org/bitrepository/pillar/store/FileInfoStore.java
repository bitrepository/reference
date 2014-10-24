package org.bitrepository.pillar.store;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common interface for both ChecksumPillar and full ReferencePillar. 
 */
public abstract class FileInfoStore {

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The storage of checksums.*/
    protected final ChecksumStore cache;
    /** The file archives for the different collections.*/
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
    protected FileInfoStore(FileStore archives, ChecksumStore cache, AlarmDispatcher alarmDispatcher, 
            Settings settings) {
        this.cache = cache;
        this.fileArchive = archives;
        this.alarmDispatcher = alarmDispatcher;
        this.settings = settings;
        this.defaultChecksumSpec = ChecksumUtils.getDefault(settings);
    }

    /**
     * Checks whether the pillar has an entry with the file-id for the given collection.
     * It will potentially verify that the actual file is consistent with the data in the cache.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @return Whether the pillar has an entry for the file.
     */
    public boolean hasFileID(String fileID, String collectionID) {
        verifyFileToCacheConsistencyIfRequired(fileID, collectionID);
        return cache.hasFile(fileID, collectionID);
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
        if(csType == defaultChecksumSpec) {
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
        if(csType == defaultChecksumSpec) {
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
     */
    public ExtractedChecksumResultSet getChecksumResultSet(XMLGregorianCalendar minTimestamp, 
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID, ChecksumSpecTYPE csSpec) {
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
     * @throws RequestHandlerException
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
     * @param fileID The id of the file to remove.
     * @param collectionID The id of the collection of the file.
     */
    public void deleteFile(String fileID, String collectionID) {
        if(fileArchive != null) {
            fileArchive.deleteFile(fileID, collectionID);
        }
        cache.deleteEntry(fileID, collectionID);
    }

    public abstract void verifyFileToCacheConsistencyOfAllDataIfRequired(String collectionID);
    protected abstract void verifyFileToCacheConsistencyIfRequired(String fileID, String collectionID);
    protected abstract String getNonDefaultChecksum(String fileId, String collectionID, ChecksumSpecTYPE csType) 
            throws RequestHandlerException;
    protected abstract ChecksumEntry retrieveNonDefaultChecksumEntry(String fileID, String collectionID, 
            ChecksumSpecTYPE csType);
    public abstract FileInfo getFileData(String fileID, String collectionID);
    public abstract ExtractedFileIDsResultSet getFileIDsResultSet(String fileID, XMLGregorianCalendar minTimestamp, 
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID);
    public abstract void putFile(String collectionID, String fileID, String fileAddress, 
            ChecksumDataForFileTYPE expectedChecksum);
//    log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
//    FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(getSettings());
//
//    try {
//        getFileInfoStore().downloadFileForValidation(message.getFileID(), message.getCollectionID(),
//                fe.downloadFromServer(new URL(message.getFileAddress())));
//    } catch (IOException e) {
//        String errMsg = "Could not retrieve the file from '" + message.getFileAddress() + "'";
//        log.error(errMsg, e);
//        ResponseInfo ri = new ResponseInfo();
//        ri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
//        ri.setResponseText(errMsg);
//        throw new InvalidMessageException(ri, message.getCollectionID());
//    }
//    
//    if(message.getChecksumDataForNewFile() != null) {
//        ChecksumDataForFileTYPE csType = message.getChecksumDataForNewFile();
//        String calculatedChecksum = getFileInfoStore().getChecksumForTempFile(message.getFileID(), 
//                message.getCollectionID(), csType.getChecksumSpec());
//        String expectedChecksum = Base16Utils.decodeBase16(csType.getChecksumValue());
//        if(!calculatedChecksum.equals(expectedChecksum)) {
//            ResponseInfo responseInfo = new ResponseInfo();
//            responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
//            responseInfo.setResponseText("Wrong checksum! Expected: [" + expectedChecksum 
//                    + "], but calculated: [" + calculatedChecksum + "]");
//            throw new IllegalOperationException(responseInfo, message.getCollectionID());
//        }
//    } else {
//        // TODO is such a checksum required?
//        log.warn("No checksums for validating the retrieved file.");
//    }
//    getArchives().moveToArchive(message.getFileID(), message.getCollectionID());
//    getCsManager().recalculateChecksum(message.getFileID(), message.getCollectionID());


    public abstract boolean verifyEnoughFreeSpaceLeftForFile(Long fileSize, String collectionID) throws RequestHandlerException;
    //    {
    //        long useableSizeLeft = fileArchive.sizeLeftInArchive(collectionID) 
    //                - settings.getReferenceSettings().getPillarSettings().getMinimumSizeLeft();
    //
    //        if(useableSizeLeft < fileSize) {
    //            ResponseInfo irInfo = new ResponseInfo();
    //            irInfo.setResponseCode(ResponseCode.FAILURE);
    //            irInfo.setResponseText("Not enough space left in this pillar. Requires '" 
    //                    + fileSize + "' but has only '" + useableSizeLeft + "'");
    //
    //            throw new IdentifyContributorException(irInfo, collectionID);
    //        }
    //    }
    protected abstract ExtractedChecksumResultSet getNonDefaultChecksumResultSet(Long maxResults, String collectionID, 
            ChecksumSpecTYPE csSpec);


    
    public abstract void checkWhetherFileExists(String fileID, String collectionID) throws RequestHandlerException;

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
}
