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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.bitrepository.settings.referencesettings.ChecksumPillarFileDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * The storage model for a pillar without a file store.
 */
public class ChecksumStorageModel extends StorageModel {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param cache           The storage for the checksums.
     * @param alarmDispatcher The alarm dispatcher.
     * @param settings        The configuration to use.
     * @param fileExchange    The file exchange.
     */
    public ChecksumStorageModel(ChecksumStore cache, AlarmDispatcher alarmDispatcher, Settings settings, FileExchange fileExchange) {
        super(null, cache, alarmDispatcher, settings, fileExchange);
    }

    @Override
    public ChecksumSpecTYPE getChecksumPillarSpec() {
        return defaultChecksumSpec;
    }

    @Override
    public void verifyEnoughFreeSpaceLeftForFile(Long fileSize, String collectionID) {
        // Does not require the actual file. 
    }

    @Override
    public void replaceFile(String fileID, String collectionID, String fileAddress, ChecksumDataForFileTYPE expectedChecksum)
            throws RequestHandlerException {
        String calculatedChecksum = retrieveChecksumFromDownloadOrVerificationData(fileID, collectionID, fileAddress, expectedChecksum);
        cache.insertChecksumCalculation(fileID, collectionID, calculatedChecksum, new Date());
    }

    @Override
    public void putFile(String collectionID, String fileID, String fileAddress, ChecksumDataForFileTYPE expectedChecksum)
            throws RequestHandlerException {
        String calculatedChecksum = retrieveChecksumFromDownloadOrVerificationData(fileID, collectionID, fileAddress, expectedChecksum);
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
    protected String getNonDefaultChecksum(String fileID, String collectionID, ChecksumSpecTYPE csType) throws RequestHandlerException {
        throw new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED,
                "The ChecksumPillar cannot handle a " + "non-default checksum specification '" + csType + "'.'");
    }

    @Override
    public FileInfo getFileInfoForActualFile(String fileID, String collectionID) throws RequestHandlerException {
        throw new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED,
                "This is a checksum pillar and it does " + "not have the actual file. Only it's checksum.");
    }

    @Override
    public ExtractedFileIDsResultSet getFileIDsResultSet(String fileID, XMLGregorianCalendar minTimestamp,
                                                         XMLGregorianCalendar maxTimestamp, Long maxResults, String collectionID) {
        return cache.getFileIDs(minTimestamp, maxTimestamp, maxResults, fileID, collectionID);
    }

    @Override
    protected ExtractedChecksumResultSet getNonDefaultChecksumResultSet(Long maxResults, String collectionID, ChecksumSpecTYPE csSpec)
            throws RequestHandlerException {
        throw new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED,
                "This is a checksum pillar and it does " + "not have the actual file. Only it's checksum.");
    }

    @Override
    public void verifyFileExists(String fileID, String collectionID) throws RequestHandlerException {
        throw new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED,
                "This is a checksum pillar and it does " + "not have the actual file. Only it's checksum.");
    }

    /**
     * Retrieves the checksum either by downloading the file from the file-address and calculating the checksum, or
     * by extracting it from the verification data.
     * Strategy is decided by the settings for this ChecksumPillar.
     *
     * @param fileID           The ID of the file.
     * @param collectionID     The ID of the collection.
     * @param fileAddress      The address where the file can be downloaded from, if necessary.
     * @param expectedChecksum The expected results for calculation the checksum of the file. The checksum can be
     *                         extracted from here instead.
     * @return The checksum.
     * @throws RequestHandlerException If something goes wrong with extracting the checksum, e.g. it is missing from
     *                                 the 'expectedChecksum' part, it is not possible to download, or the downloaded file does not have
     *                                 the expected
     *                                 checksum.
     */
    private String retrieveChecksumFromDownloadOrVerificationData(String fileID, String collectionID, String fileAddress,
                                                                  ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        switch (getChecksumPillarFileDownload()) {
            case ALWAYS_DOWNLOAD:
                return downloadFileAndCalculateChecksum(fileID, collectionID, fileAddress, expectedChecksum);
            case NEVER_DOWNLOAD:
                return extractChecksum(expectedChecksum);
            default:
                if (expectedChecksum != null) {
                    return extractChecksum(expectedChecksum);
                } else {
                    return downloadFileAndCalculateChecksum(fileID, collectionID, fileAddress, null);
                }
        }
    }

    /**
     * Retrieves the ChecksumPillarFileDownload from settings. If not set, then the default value is
     * 'DOWNLOAD_WHEN_MISSING_FROM_MESSAGE'.
     *
     * @return Whether the ChecksumPillar should download the files regarding PutFileRequests.
     */
    protected ChecksumPillarFileDownload getChecksumPillarFileDownload() {
        if (settings.getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload() == null) {
            return ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE;
        } else {
            return settings.getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload();
        }
    }

    /**
     * Extracts the checksum from ChecksumDataForFileTYPE.
     *
     * @param checksumData The message to extract the checksum from.
     * @return The checksum from the message.
     * @throws RequestHandlerException If the checksum data does not contain the checksum.
     */
    private String extractChecksum(ChecksumDataForFileTYPE checksumData) throws RequestHandlerException {
        if (checksumData != null) {
            return Base16Utils.decodeBase16(checksumData.getChecksumValue());
        } else {
            throw new InvalidMessageException(ResponseCode.NEW_FILE_CHECKSUM_FAILURE,
                    "The checksum should have been " + "provided with the message");
        }
    }

    /**
     * Retrieves the actual data, validates it (if possible).
     *
     * @param fileID       The ID of the file.
     * @param collectionID The collection ID in where the file is located.
     * @param fileAddress  The address where the file can be found at.
     * @return The checksum of the retrieved file.
     * @throws RequestHandlerException If the operation fails.
     */
    private String downloadFileAndCalculateChecksum(String fileID, String collectionID, String fileAddress,
                                                    ChecksumDataForFileTYPE expectedChecksum) throws RequestHandlerException {
        String calculatedChecksum = calculateChecksumForFileAtURL(fileAddress);
        if (!validChecksum(calculatedChecksum, expectedChecksum)) {
            String givenChecksum = Base16Utils.decodeBase16(expectedChecksum.getChecksumValue());
            log.error("Wrong checksum for file '" + fileID + "' at '" + collectionID + "'! Expected: [" + givenChecksum +
                    "], but calculated: [" + calculatedChecksum + "]");
            throw new IllegalOperationException(ResponseCode.NEW_FILE_CHECKSUM_FAILURE,
                    "Expected checksums '" + givenChecksum + "' but the checksum was '" + calculatedChecksum + "'.", fileID);
        }

        return calculatedChecksum;
    }

    /**
     * Calculates the checksum for a file at a given URL.
     *
     * @param fileAddress The URL for the file.
     * @return The checksum for the file.
     * @throws RequestHandlerException If data for the file could not be retrieved.
     */
    private String calculateChecksumForFileAtURL(String fileAddress) throws RequestHandlerException {
        log.debug("Retrieving the data from URL: '" + fileAddress + "'");

        try {
            return ChecksumUtils.generateChecksum(fileExchange.getFile(new URL(fileAddress)), defaultChecksumSpec);
        } catch (IOException e) {
            String errMsg = "Could not retrieve the file from '" + fileAddress + "'";
            log.error(errMsg, e);
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE, errMsg, null);
        }
    }

    /**
     * Checks whether a calculated checksum is identical to a given checksum.
     *
     * @param calculatedChecksum The calculated checksum.
     * @param expectedChecksum   The expected checksum, may be null (will give a 'true' answer).
     * @return Whether the calculated checksum is the same as the expected one (or true, if nothing is expected).
     */
    private boolean validChecksum(String calculatedChecksum, ChecksumDataForFileTYPE expectedChecksum) {
        if (expectedChecksum != null) {
            String givenChecksum = Base16Utils.decodeBase16(expectedChecksum.getChecksumValue());
            return calculatedChecksum.equals(givenChecksum);
        }
        return true;
    }

    @Override
    public void verifyFileToCacheConsistencyOfAllData(String collectionID) {
        log.warn("Should not make a call to verify all data on a ChecksumPillar.");
    }
}
