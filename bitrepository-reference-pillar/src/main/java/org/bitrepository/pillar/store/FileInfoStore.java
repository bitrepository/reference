package org.bitrepository.pillar.store;

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

    public boolean hasFileID(String fileID, String collectionID) {
        verfiyFileToCacheConsistencyIfRequired(fileID, collectionID);
        return cache.hasFile(fileID, collectionID);
    }

    /**
     * Retrieves the checksum for a given file with the given checksum specification.
     * If it is the default checksum specification, then the cached checksum is returned (though it is recalculated 
     * if it is too old).
     * A different checksum specification will cause the default checksum to be recalculated for the file and updated 
     * in the database, along with the calculation of the new checksum specification which will be returned. 
     * 
     * @param fileId The id of the file whose checksum is requested.
     * @param collectionId The id of the collection of the file.
     * @param csType The type of checksum.
     * @return The requested type of checksum for the given file.
     */
    public String getChecksumForFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
        verfiyFileToCacheConsistencyIfRequired(fileId, collectionId);
        if(csType == defaultChecksumSpec) {
            return cache.getChecksum(fileId, collectionId);            
        } else {
            return retrieveNonDefaultChecksum(fileId, collectionId, csType);
        }
    }

    /**
     * Retrieves the entry for the checksum for a given file with the given checksum specification.
     * If it is the default checksum specification, then the cached checksum is returned (though it is recalculated 
     * if it is too old).
     * A different checksum specification will cause the default checksum to be recalculated for the file and updated 
     * in the database, along with the calculation of the new checksum specification which will be returned. 
     * 
     * @param fileId The id of the file whose checksum is requested.
     * @param collectionId The id of the collection of the file.
     * @param csType The type of checksum.
     * @return The entry for the requested type of checksum for the given file.
     */
    public ChecksumEntry getChecksumEntryForFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
        verfiyFileToCacheConsistencyIfRequired(fileId, collectionId);
        if(csType == defaultChecksumSpec) {
            return cache.getEntry(fileId, collectionId);            
        } else {
            return retrieveNonDefaultChecksumEntry(fileId, collectionId, csType);
        }
    }

    /**
     * Retrieves the entry for a given file with a given checksumSpec in the ChecksumDataForFileTYPE format.
     * @param fileId The id of the file to retrieve the data from.
     * @param collectionId The id of the collection of the file.
     * @param csType The type of checksum to calculate.
     * @return The entry encapsulated in the ChecksumDataForFileTYPE data format.
     */
    public ChecksumDataForFileTYPE getChecksumDataForFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
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
        verfiyFileToCacheConsistencyOfAllDataIfRequired(collectionID);
        if(csSpec.equals(defaultChecksumSpec)) {
            return cache.getChecksumResults(minTimestamp, maxTimestamp, maxResults, collectionID);
        } else {
            log.info("Bulk-extraction of non-default checksums for spec: " + csSpec 
                    + ", on collection " + collectionID + ", with maximum " + maxResults + " results.");
            // We ignore minTimestamp and maxTimestamp when dealing with non-default checksums.
            return getNonDefaultChecksumResultSet(maxResults, collectionID, csSpec);
        }
    }

    public ExtractedChecksumResultSet getSingleChecksumResultSet(String fileID, String collectionID, 
            XMLGregorianCalendar minTimestamp, XMLGregorianCalendar maxTimestamp, ChecksumSpecTYPE csSpec) {
        verfiyFileToCacheConsistencyIfRequired(fileID, collectionID);
        return cache.getChecksumResult(minTimestamp, maxTimestamp, fileID, collectionID);
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

    public abstract void verfiyFileToCacheConsistencyOfAllDataIfRequired(String collectionID);
    protected abstract void verfiyFileToCacheConsistencyIfRequired(String fileID, String collectionID);
    protected abstract String retrieveNonDefaultChecksum(String fileId, String collectionID, ChecksumSpecTYPE csType);
    protected abstract ChecksumEntry retrieveNonDefaultChecksumEntry(String fileID, String collectionID, 
            ChecksumSpecTYPE csType);
    public abstract FileInfo getFileData(String fileID, String collectionID);
    public abstract ExtractedFileIDsResultSet getFileIDsResultSet(String fileID, XMLGregorianCalendar minTimestamp, 
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID);

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
//  ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
//
//  long i = 0;
//  for(String fileId : cache.getAllFileIDs(collectionID)) {
//      if(maxResults != null && i > maxResults) {
//          break;
//      }
//      i++;
//
//      String checksum = getChecksumForFile(fileId, collectionID, csSpec);
//      ChecksumEntry entry = new ChecksumEntry(fileId, checksum, new Date());
//      res.insertChecksumEntry(entry);
//  }
//    return res

    
    public abstract void checkWhetherFileExists(String fileID, String collectionID) throws RequestHandlerException;
//    {
//        if(!hasFileID(message.getFileID(), message.getCollectionID())) {
//            log.warn("The file '" + message.getFileID() + "' has been requested, but we do not have that file!");
//            // Then tell the mediator, that we failed.
//            ResponseInfo fri = new ResponseInfo();
//            fri.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
//            fri.setResponseText("The file '" + message.getFileID() + "' has been requested, but we do "
//                    + "not have that file!");
//            throw new InvalidMessageException(fri, message.getCollectionID());
//        }
//    }


    //
    //    
    //    /**
    //     * Recalculates the checksum of a given file based on the default checksum specification.
    //     * @param fileId The id of the file to recalculate its default checksum for.
    //     * @param collectionId The id of the collection of the file.
    //     */
    //    public void recalculateChecksum(String fileId, String collectionId) {
    //        log.info("Recalculating the checksum of file '" + fileId + "'.");
    //        FileInfo fi = fileArchive.getFileInfo(fileId, collectionId);
    //        String checksum = ChecksumUtils.generateChecksum(fi, defaultChecksumSpec);
    //        cache.insertChecksumCalculation(fileId, collectionId, checksum, new Date());
    //    }
    //    
    //    /**
    //     * Calculates the checksum of a file within the tmpDir.
    //     * @param fileId The id of the file to calculate the checksum for.
    //     * @param collectionId The id of the collection of the file.
    //     * @param csType The specification for the type of checksum to calculate.
    //     * @return The checksum of the given type for the file with the given id.
    //     */
    //    public String getChecksumForTempFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
    //        FileInfo fi = fileArchive.getFileInTmpDir(fileId, collectionId);
    //        return ChecksumUtils.generateChecksum(fi, csType);
    //    }
    //    
    //    /**
    //     * TODO this should be in the database instead.
    //     * @param minTimeStamp The minimum date for the timestamp of the extracted file ids entries.
    //     * @param maxTimeStamp The maximum date for the timestamp of the extracted file ids entries.
    //     * @param maxNumberOfResults The maximum number of results.
    //     * @param collectionId The id of the collection.
    //     * @return The requested file ids.
    //     */
    //    public ExtractedFileIDsResultSet getFileIds(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
    //            Long maxNumberOfResults, String collectionId) {
    //        ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
    //    
    //        Long minTime = 0L;
    //        if(minTimeStamp != null) {
    //            minTime = CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp).getTime();
    //        }
    //        Long maxTime = 0L;
    //        if(maxTimeStamp != null) {
    //            maxTime = CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp).getTime();
    //        }
    //    
    //        // Map between lastModifiedDate and fileInfo.
    //        ConcurrentSkipListMap<Long, FileInfo> sortedDateFileIDMap = new ConcurrentSkipListMap<Long, FileInfo>();
    //        for(String fileId : fileArchive.getAllFileIds(collectionId)) {
    //            FileInfo fi = fileArchive.getFileInfo(fileId, collectionId);
    //            if((minTimeStamp == null || minTime <= fi.getMdate()) &&
    //                    (maxTimeStamp == null || maxTime >= fi.getMdate())) {
    //                sortedDateFileIDMap.put(fi.getMdate(), fi);
    //            }
    //        }
    //    
    //        int i = 0;
    //        Map.Entry<Long, FileInfo> entry;
    //        while((entry = sortedDateFileIDMap.pollFirstEntry()) != null && 
    //                (maxNumberOfResults == null || i < maxNumberOfResults)) {
    //            res.insertFileInfo(entry.getValue());
    //            i++;
    //        }
    //    
    //        if(maxNumberOfResults != null && i >= maxNumberOfResults) {
    //            res.reportMoreEntriesFound();
    //        }
    //    
    //        return res;
    //    }
    //    
    //    /**
    //     * Validates that all files in the cache is also in the archive, and that all files in the archive
    //     * is also in the cache.
    //     * @param collectionId The id of the collection where the data should be ensured.
    //     */
    //    public void ensureStateOfAllData(String collectionId) {
    //        for(String fileId : cache.getAllFileIDs(collectionId)) {
    //            ensureFileState(fileId, collectionId);
    //        }
    //    
    //        for(String fileId : fileArchive.getAllFileIds(collectionId)) {
    //            ensureChecksumState(fileId, collectionId);
    //        }
    //    }
    //    
    //    /**
    //     * Checks the settings for whether to traverse and ensure all the data, when dealing with a message 
    //     * (unlike the scheduler). Default is not to ensure all data (thus if the setting is null).
    //     * @param collectionId The ID of the collection to ensure all data of - if the settings allows.
    //     */
    //    protected void checkWhetherToTraverseAllDataForMessage(String collectionId) {
    //        if(settings.getReferenceSettings().getPillarSettings().getVerifyAllData() != null &&
    //                settings.getReferenceSettings().getPillarSettings().getVerifyAllData() 
    //                == VerifyAllData.MESSAGES_AND_SCHEDULER) {
    //            ensureStateOfAllData(collectionId);
    //        }
    //    }
    //    
    //    /**
    //     * Ensures that a file id in the cache is also in the archive.
    //     * Will send an alarm, if the file is missing, then remove it from index.
    //     * @param fileId The id of the file.
    //     * @param collectionId The id of the collection of the file.
    //     */
    //    private void ensureFileState(String fileId, String collectionId) {
    //        if(!fileArchive.hasFile(fileId, collectionId)) {
    //            log.warn("The file '" + fileId + "' in the ChecksumCache is no longer in the archive. "
    //                    + "Dispatching an alarm, and removing it from the cache.");
    //            Alarm alarm = new Alarm();
    //            alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
    //            alarm.setAlarmText("The file '" + fileId + "' has been removed from the archive without it being removed "
    //                    + "from index. Removing it from index.");
    //            alarm.setFileID(fileId);
    //            alarmDispatcher.error(alarm);
    //    
    //            cache.deleteEntry(fileId, collectionId);
    //        }
    //    }
    //    
    //    /**
    //     * Ensures that the cache has an non-obsolete checksum for the given file.
    //     * Also validates, that the checksum is up to date with the file.
    //     * @param fileId The id of the file.
    //     * @param collectionId The id of the collection of the file.
    //     */
    //    private void ensureChecksumState(String fileId, String collectionId) {
    //        Long maxAgeForChecksums = settings.getReferenceSettings().getPillarSettings()
    //                .getMaxAgeForChecksums().longValue();
    //        if(!cache.hasFile(fileId, collectionId)) {
    //            log.debug("No checksum cached for file '" + fileId + "'. Calculating the checksum.");
    //            recalculateChecksum(fileId, collectionId);
    //        } else {
    //            long checksumDate = cache.getCalculationDate(fileId, collectionId).getTime();
    //            long minDateForChecksum = System.currentTimeMillis() - maxAgeForChecksums;
    //            if(checksumDate < minDateForChecksum) {
    //                log.info("The checksum for the file '" + fileId + "' is too old. Recalculating.");                
    //                recalculateChecksum(fileId, collectionId);
    //            } else if(checksumDate < fileArchive.getFileInfo(fileId, collectionId).getMdate()) {
    //                log.info("The last modified date for the file is newer than the latest checksum.");
    //                recalculateChecksum(fileId, collectionId);
    //            }
    //        }
    //    }

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
