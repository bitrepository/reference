package org.bitrepository.pillar.store;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.bitrepository.settings.referencesettings.ChecksumPillarFileDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecksumPillarModel extends PillarModel {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The checksum specification for this checksum pillar.*/
    protected final ChecksumSpecTYPE pillarSpecificChecksumType;
    
    protected ChecksumPillarModel(ChecksumStore cache, AlarmDispatcher alarmDispatcher, Settings settings) {
        super(null, cache, alarmDispatcher, settings);
        this.pillarSpecificChecksumType = ChecksumUtils.getDefault(settings);
    }
    
    @Override
    public ChecksumSpecTYPE getChecksumPillarSpec() {
        return pillarSpecificChecksumType;
    }

    @Override
    public void verifyEnoughFreeSpaceLeftForFile(Long fileSize, String collectionID) throws RequestHandlerException {
        // Does not require the actual file. 
    }

    @Override
    public void replaceFile(String fileID, String collectionID, String fileAddress, 
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        String calculatedChecksum = retrieveChecksumFromDownloadOrVerificationData(fileID, collectionID, fileAddress, 
                expectedChecksum);
        cache.insertChecksumCalculation(fileID, collectionID, calculatedChecksum, new Date());
    }

    @Override
    public void putFile(String collectionID, String fileID, String fileAddress, 
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        String calculatedChecksum = retrieveChecksumFromDownloadOrVerificationData(fileID, collectionID, fileAddress, 
                expectedChecksum);
        cache.insertChecksumCalculation(fileID, collectionID, calculatedChecksum, new Date());
    }

    @Override
    public void verifyFileToCacheConsistencyOfAllDataIfRequired(String collectionID) {
        // Cannot verify, since we do not have any files.
    }

    @Override
    protected void verifyFileToCacheConsistencyIfRequired(String fileID, String collectionID) {
        // Cannot verify, since we do not have the file.
    }

    @Override
    protected String getNonDefaultChecksum(String fileId, String collectionID, ChecksumSpecTYPE csType) 
            throws RequestHandlerException {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
        ri.setResponseText("Cannot handle the checksum specification '" + csType + "'."
                + "This is a checksum pillar, which only can handle '" + pillarSpecificChecksumType + "'");
        throw new InvalidMessageException(ri, collectionID);
    }

    @Override
    public FileInfo getFileInfoForActualFile(String fileID, String collectionID)
            throws RequestHandlerException {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
        ri.setResponseText("This is a checksum pillar and it does not have the actual file. Only it's checksum.");
        throw new InvalidMessageException(ri, collectionID);
    }

    @Override
    public ExtractedFileIDsResultSet getFileIDsResultSet(String fileID, XMLGregorianCalendar minTimestamp, 
            XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID) {
        return cache.getFileIDs(minTimestamp, maxTimestamp, maxResults, collectionID);
    }

    @Override
    protected ExtractedChecksumResultSet getNonDefaultChecksumResultSet(Long maxResults, String collectionID, 
            ChecksumSpecTYPE csSpec) throws RequestHandlerException {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.REQUEST_NOT_SUPPORTED);
        ri.setResponseText("This is a checksum pillar and it does not have the actual file. Only it's checksum.");
        throw new InvalidMessageException(ri, collectionID);
    }

    @Override
    public void verifyFileExists(String fileID, String collectionID) throws RequestHandlerException {
        // Fail similarly to retrieving the file info for a file.
        getFileInfoForActualFile(fileID, collectionID);
    }
    
    /**
     * Retrieves the checksum either by downloading the file from the file-address and calculating the checksum, or
     * by extracting it from the verification data.
     * Strategy is decided by the settings for this ChecksumPillar.
     * @param fileID The ID of the file.
     * @param collectionID The ID of the collection.
     * @param fileAddress The address where the file can be download from, if necessary.
     * @param expectedChecksum The expected results for calculation the checksum of the file. The checksum can be 
     * extracted from here instead.
     * @return The checksum.
     * @throws RequestHandlerException If something goes wrong with extracting the checksum, e.g. it is missing from
     * the 'expectedChecksum' part, it is not possible to download, or the downloaded file does not have the expected 
     * checksum.
     */
    private String retrieveChecksumFromDownloadOrVerificationData(String fileID, String collectionID, 
            String fileAddress, ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        switch(getChecksumPillarFileDownload()) {
        case ALWAYS_DOWNLOAD:
            return downloadeFileAndCalculateChecksum(fileID, collectionID, fileAddress, expectedChecksum);
        case NEVER_DOWNLOAD:
            return extractChecksum(expectedChecksum, collectionID);
        default:
            if(expectedChecksum != null) {
                return extractChecksum(expectedChecksum, collectionID);
            } else {
                return downloadeFileAndCalculateChecksum(fileID, collectionID, fileAddress, expectedChecksum);
            }
        }
    }

    /**
     * Retrieves the ChecksumPillarFileDownload from settings. If not set, then the default value is
     * 'DOWNLOAD_WHEN_MISSING_FROM_MESSAGE'.
     * @return Whether the ChecksumPillar should download the files regarding PutFileRequests.
     */
    protected ChecksumPillarFileDownload getChecksumPillarFileDownload() {
        if(settings.getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload() 
                == null) {
            return ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE;
        } else {
            return settings.getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload();
        }
    }    
    
    /**
     * Extracts the checksum from ChecksumDataForFileTYPE. 
     * @param checksumData The message to extract the checksum from.
     * @param collectionID The id of the collection.
     * @return The checksum from the message.
     * @throws RequestHandlerException If the checksum data does not contain the checksum.
     */
    private String extractChecksum(ChecksumDataForFileTYPE checksumData, String collectionID) 
            throws RequestHandlerException {
        if(checksumData != null) {
            return Base16Utils.decodeBase16(checksumData.getChecksumValue());
        } else {
            ResponseInfo fi = new ResponseInfo();
            fi.setResponseText("The checksum should have been provided with the message");
            fi.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
            throw new InvalidMessageException(fi, collectionID);
        }
    }

    /**
     * Retrieves the actual data, validates it (if possible).
     * @param message The request to for the file to put.
     * @return The checksum of the retrieved file.
     * @throws RequestHandlerException If the operation fails.
     */
    private String downloadeFileAndCalculateChecksum(String fileID, String collectionID, String fileAddress, 
            ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        log.debug("Retrieving the data to be stored from URL: '" + fileAddress + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(settings);

        String calculatedChecksum = null;
        try {
            calculatedChecksum = ChecksumUtils.generateChecksum(fe.downloadFromServer(new URL(fileAddress)),
                    pillarSpecificChecksumType);
        } catch (IOException e) {
            String errMsg = "Could not retrieve the file from '" + fileAddress + "'";
            log.error(errMsg, e);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
            ri.setResponseText(errMsg);
            throw new InvalidMessageException(ri, collectionID);
        }
        
        if(expectedChecksum != null) {
            String givenChecksum = Base16Utils.decodeBase16(expectedChecksum.getChecksumValue());
            if(!calculatedChecksum.equals(givenChecksum)) {
                log.error("Wrong checksum! Expected: [" + givenChecksum 
                        + "], but calculated: [" + calculatedChecksum + "]");
                ResponseInfo ri = new ResponseInfo();
                ri.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
                ri.setResponseText("Expected checksums '" + givenChecksum + "' but the checksum was '" 
                        + calculatedChecksum + "'.");
                throw new IllegalOperationException(ri, collectionID);
            }
        } else {
            log.debug("No checksums for validating the retrieved file.");
        }
        
        return calculatedChecksum;
    }
}
