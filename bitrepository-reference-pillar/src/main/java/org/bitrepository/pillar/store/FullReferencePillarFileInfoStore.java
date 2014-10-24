package org.bitrepository.pillar.store;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.bitrepository.settings.referencesettings.VerifyAllData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullReferencePillarFileInfoStore extends FileInfoStore {

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    public FullReferencePillarFileInfoStore(FileStore archives, ChecksumStore cache, AlarmDispatcher alarmDispatcher,
            Settings settings) {
        super(archives, cache, alarmDispatcher, settings);
    }

    @Override
    public void verifyFileToCacheConsistencyOfAllDataIfRequired(
            String collectionID) {
        if(settings.getReferenceSettings().getPillarSettings().getVerifyAllData() != null &&
                settings.getReferenceSettings().getPillarSettings().getVerifyAllData() 
                == VerifyAllData.MESSAGES_AND_SCHEDULER) {
            verifyFileToCacheConsistencyOfAllData(collectionID);
        }
    }

    @Override
    protected void verifyFileToCacheConsistencyIfRequired(String fileID,
            String collectionID) {
        if(settings.getReferenceSettings().getPillarSettings().getVerifyAllData() != null &&
                settings.getReferenceSettings().getPillarSettings().getVerifyAllData() 
                == VerifyAllData.MESSAGES_AND_SCHEDULER) {
            verifyFileToCacheConsistency(fileID, collectionID);
        }
    }

    @Override
    protected String getNonDefaultChecksum(String fileId,
            String collectionID, ChecksumSpecTYPE csType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ChecksumEntry retrieveNonDefaultChecksumEntry(String fileID,
            String collectionID, ChecksumSpecTYPE csType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileInfo getFileData(String fileID, String collectionID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExtractedFileIDsResultSet getFileIDsResultSet(String fileID,
            XMLGregorianCalendar minTimestamp,
            XMLGregorianCalendar maxTimestamp, Long maxResults,
            String collectionID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putFile(String collectionID, String fileID, String fileAddress,
            ChecksumDataForFileTYPE expectedChecksum) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean verifyEnoughFreeSpaceLeftForFile(Long fileSize,
            String collectionID) throws RequestHandlerException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected ExtractedChecksumResultSet getNonDefaultChecksumResultSet(
            Long maxResults, String collectionID, ChecksumSpecTYPE csSpec) {
        ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();

        long i = 0;
        for(String fileId : cache.getAllFileIDs(collectionID)) {
            if(maxResults != null && i > maxResults) {
                break;
            }
            i++;

            String checksum = getNonDefaultChecksum(fileId, collectionID, csSpec);
            ChecksumEntry entry = new ChecksumEntry(fileId, checksum, new Date());
            res.insertChecksumEntry(entry);
        }
        return res;
    }

    @Override
    public void checkWhetherFileExists(String fileID, String collectionID)
            throws RequestHandlerException {
        if(!hasFileID(fileID, collectionID)) {
            String errMsg = "The file '" + fileID + "' has been requested, but we do not have that file in collection '" 
                    + collectionID + "'!";
            log.warn(errMsg);
            // Then tell the mediator, that we failed.
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            fri.setResponseText(errMsg);
            throw new InvalidMessageException(fri, collectionID);
        }
    }

    /**
     * Recalculates the checksum of a given file based on the default checksum specification.
     * @param fileId The id of the file to recalculate its default checksum for.
     * @param collectionId The id of the collection of the file.
     */
    private void verifyFileToCacheConsistency(String fileId, String collectionId) {
        log.info("Recalculating the checksum of file '" + fileId + "'.");
        FileInfo fi = fileArchive.getFileInfo(fileId, collectionId);
        String checksum = ChecksumUtils.generateChecksum(fi, defaultChecksumSpec);
        cache.insertChecksumCalculation(fileId, collectionId, checksum, new Date());
    }

    /**
     * Calculates the checksum of a file within the tmpDir.
     * @param fileId The id of the file to calculate the checksum for.
     * @param collectionId The id of the collection of the file.
     * @param csType The specification for the type of checksum to calculate.
     * @return The checksum of the given type for the file with the given id.
     */
    public String getChecksumForTempFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
        FileInfo fi = fileArchive.getFileInTmpDir(fileId, collectionId);
        return ChecksumUtils.generateChecksum(fi, csType);
    }

    /**
     * TODO this should be in the database instead.
     * @param minTimeStamp The minimum date for the timestamp of the extracted file ids entries.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted file ids entries.
     * @param maxNumberOfResults The maximum number of results.
     * @param collectionId The id of the collection.
     * @return The requested file ids.
     */
    public ExtractedFileIDsResultSet getFileIds(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionId) {
        ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();

        Long minTime = 0L;
        if(minTimeStamp != null) {
            minTime = CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp).getTime();
        }
        Long maxTime = 0L;
        if(maxTimeStamp != null) {
            maxTime = CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp).getTime();
        }

        // Map between lastModifiedDate and fileInfo.
        ConcurrentSkipListMap<Long, FileInfo> sortedDateFileIDMap = new ConcurrentSkipListMap<Long, FileInfo>();
        for(String fileId : fileArchive.getAllFileIds(collectionId)) {
            FileInfo fi = fileArchive.getFileInfo(fileId, collectionId);
            if((minTimeStamp == null || minTime <= fi.getMdate()) &&
                    (maxTimeStamp == null || maxTime >= fi.getMdate())) {
                sortedDateFileIDMap.put(fi.getMdate(), fi);
            }
        }

        int i = 0;
        Map.Entry<Long, FileInfo> entry;
        while((entry = sortedDateFileIDMap.pollFirstEntry()) != null && 
                (maxNumberOfResults == null || i < maxNumberOfResults)) {
            res.insertFileInfo(entry.getValue());
            i++;
        }

        if(maxNumberOfResults != null && i >= maxNumberOfResults) {
            res.reportMoreEntriesFound();
        }

        return res;
    }

    /**
     * Validates that all files in the cache is also in the archive, and that all files in the archive
     * is also in the cache.
     * @param collectionId The id of the collection where the data should be ensured.
     */
    public void verifyFileToCacheConsistencyOfAllData(String collectionId) {
        for(String fileId : cache.getAllFileIDs(collectionId)) {
            ensureFileState(fileId, collectionId);
        }

        for(String fileId : fileArchive.getAllFileIds(collectionId)) {
            ensureChecksumState(fileId, collectionId);
        }
    }

    /**
     * Ensures that a file id in the cache is also in the archive.
     * Will send an alarm, if the file is missing, then remove it from index.
     * @param fileId The id of the file.
     * @param collectionId The id of the collection of the file.
     */
    private void ensureFileState(String fileId, String collectionId) {
        if(!fileArchive.hasFile(fileId, collectionId)) {
            log.warn("The file '" + fileId + "' in the ChecksumCache is no longer in the archive. "
                    + "Dispatching an alarm, and removing it from the cache.");
            Alarm alarm = new Alarm();
            alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
            alarm.setAlarmText("The file '" + fileId + "' has been removed from the archive without it being removed "
                    + "from index. Removing it from index.");
            alarm.setFileID(fileId);
            alarmDispatcher.error(alarm);

            cache.deleteEntry(fileId, collectionId);
        }
    }

    /**
     * Ensures that the cache has an non-obsolete checksum for the given file.
     * Also validates, that the checksum is up to date with the file.
     * @param fileId The id of the file.
     * @param collectionId The id of the collection of the file.
     */
    private void ensureChecksumState(String fileId, String collectionId) {
        Long maxAgeForChecksums = settings.getReferenceSettings().getPillarSettings()
                .getMaxAgeForChecksums().longValue();
        if(!cache.hasFile(fileId, collectionId)) {
            log.debug("No checksum cached for file '" + fileId + "'. Calculating the checksum.");
            verifyFileToCacheConsistency(fileId, collectionId);
        } else {
            long checksumDate = cache.getCalculationDate(fileId, collectionId).getTime();
            long minDateForChecksum = System.currentTimeMillis() - maxAgeForChecksums;
            if(checksumDate < minDateForChecksum) {
                log.info("The checksum for the file '" + fileId + "' is too old. Recalculating.");                
                verifyFileToCacheConsistency(fileId, collectionId);
            } else if(checksumDate < fileArchive.getFileInfo(fileId, collectionId).getMdate()) {
                log.info("The last modified date for the file is newer than the latest checksum.");
                verifyFileToCacheConsistency(fileId, collectionId);
            }
        }
    }
}
