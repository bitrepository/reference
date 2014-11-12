package org.bitrepository.pillar.store;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
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
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.bitrepository.settings.referencesettings.VerifyAllData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullReferencePillarModel extends PillarModel {

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    public FullReferencePillarModel(FileStore archives, ChecksumStore cache, AlarmDispatcher alarmDispatcher,
            Settings settings) {
        super(archives, cache, alarmDispatcher, settings);
        log.info("Instantiating the FullReferencePillar: " + getPillarID());
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
    protected String getNonDefaultChecksum(String fileID,
            String collectionID, ChecksumSpecTYPE csType) {
        FileInfo fi = fileArchive.getFileInfo(fileID, collectionID);
        return ChecksumUtils.generateChecksum(fi, csType);
    }

    @Override
    public FileInfo getFileInfoForActualFile(String fileID, String collectionID) {
        return fileArchive.getFileInfo(fileID, collectionID);
    }

    @Override
    public ExtractedFileIDsResultSet getFileIDsResultSet(String fileID, XMLGregorianCalendar minTimestamp,
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID) {
        Long minTime = null;
        if(minTimestamp != null) {
            minTime = CalendarUtils.convertFromXMLGregorianCalendar(minTimestamp).getTime();
        }
        Long maxTime = null;
        if(maxTimestamp != null) {
            maxTime = CalendarUtils.convertFromXMLGregorianCalendar(maxTimestamp).getTime();
        }
        
        if(fileID == null) {
            return getFileIds(minTime, maxTime, maxResults, collectionID);
        }
        
        ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
        FileInfo entry = fileArchive.getFileInfo(fileID, collectionID);
        if((minTime == null || minTime <= entry.getLastModifiedDate()) &&
                (maxTime == null || maxTime >= entry.getLastModifiedDate())) {
            res.insertFileInfo(entry);
        }
        return res;

    }

    @Override
    public void verifyEnoughFreeSpaceLeftForFile(Long fileSize,
            String collectionID) throws RequestHandlerException {
        long useableSizeLeft = fileArchive.sizeLeftInArchive(collectionID) 
                - settings.getReferenceSettings().getPillarSettings().getMinimumSizeLeft();

        if(useableSizeLeft < fileSize) {
            throw new IdentifyContributorException(ResponseCode.FAILURE, "Not enough space left in this pillar. "
                    + "Requires '" + fileSize + "' but has only '" + useableSizeLeft + "'", collectionID);
        }
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
    public void verifyFileExists(String fileID, String collectionID)
            throws RequestHandlerException {
        if(!hasFileID(fileID, collectionID)) {
            log.warn("The file '" + fileID + "' has been requested, but we do not have that file in collection '" 
                    + collectionID + "'!");
            throw new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.", collectionID);
        }
    }

    @Override
    public ChecksumSpecTYPE getChecksumPillarSpec() {
        // Is not a checksum-pillar, thus no required checksum spec.
        return null;
    }

    @Override
    public void putFile(String collectionID, String fileID, String fileAddress,
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        downloadFileToTmpAndVerify(fileID, collectionID, fileAddress, expectedChecksum);
        fileArchive.moveToArchive(fileID, collectionID);
        verifyFileToCacheConsistency(fileID, collectionID);
    }

    @Override
    public void replaceFile(String fileID, String collectionID, String fileAddress,
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        downloadFileToTmpAndVerify(fileID, collectionID, fileAddress, expectedChecksum);
        fileArchive.replaceFile(fileID, collectionID);
        verifyFileToCacheConsistency(fileID, collectionID);
    }

    /**
     * Recalculates the checksum of a given file based on the default checksum specification.
     * @param fileId The id of the file to recalculate its default checksum for.
     * @param collectionId The id of the collection of the file.
     */
    public void verifyFileToCacheConsistency(String fileId, String collectionId) {
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
    private String getChecksumForTempFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
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
    private ExtractedFileIDsResultSet getFileIds(Long minTime, Long maxTime, Long maxNumberOfResults, String collectionId) {
        ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();

        // Map between lastModifiedDate and fileInfo.
        ConcurrentSkipListMap<Long, FileInfo> sortedDateFileIDMap = new ConcurrentSkipListMap<Long, FileInfo>();
        for(String fileId : fileArchive.getAllFileIds(collectionId)) {
            FileInfo fi = fileArchive.getFileInfo(fileId, collectionId);
            if((minTime == null || minTime <= fi.getLastModifiedDate()) &&
                    (maxTime == null || maxTime >= fi.getLastModifiedDate())) {
                sortedDateFileIDMap.put(fi.getLastModifiedDate(), fi);
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

    @Override
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
            } else if(checksumDate < fileArchive.getFileInfo(fileId, collectionId).getLastModifiedDate()) {
                log.info("The last modified date for the file is newer than the latest checksum.");
                verifyFileToCacheConsistency(fileId, collectionId);
            }
        }
    }

    /**
     * Downloads the file to temporary area and verifies, that it has the expected checksum.
     * 
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @param fileAddress The address to download the file from.
     * @param expectedChecksum The expected checksum for the downloaded file.
     * @throws RequestHandlerException If 
     */
    private void downloadFileToTmpAndVerify(String fileID, String collectionID, String fileAddress, 
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        log.debug("Retrieving the data to be stored from URL: '" + fileAddress + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(settings);

        try {
            fileArchive.downloadFileForValidation(fileID, collectionID,
                    fe.downloadFromServer(new URL(fileAddress)));
        } catch (IOException e) {
            String errMsg = "Could not retrieve the file from '" + fileAddress + "'";
            log.error(errMsg, e);
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE, errMsg, collectionID, e);
        }

        if(expectedChecksum != null) {
            String calculatedChecksum = getChecksumForTempFile(fileID, collectionID, expectedChecksum.getChecksumSpec());
            String expectedChecksumValue = Base16Utils.decodeBase16(expectedChecksum.getChecksumValue());
            log.debug("Validating newly downloaded file, '" + fileID + "', against expected checksum '" + expectedChecksumValue + "'.");
            if(!calculatedChecksum.equals(expectedChecksumValue)) {
                log.warn("Wrong checksum! Expected: [" + expectedChecksumValue 
                        + "], but calculated: [" + calculatedChecksum + "]");
                throw new IllegalOperationException(ResponseCode.NEW_FILE_CHECKSUM_FAILURE, "The downloaded file does "
                        + "not have the expected checksum", collectionID, fileID);
            }
        } else {
            log.debug("No checksums for validating the newly downloaded file '" + fileID + "'.");
        }
    }
}
