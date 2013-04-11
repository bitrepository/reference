/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.mocks;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;

/**
 * Wrapper of an integrity model and count how many times each method it used.
 */
public class MockIntegrityModel implements IntegrityModel {
    
    private final IntegrityModel integrityModel;
    
    /**
     * Constructor.
     */
    public MockIntegrityModel(IntegrityModel integrityModel) {
        this.integrityModel = integrityModel;
    }

    private int callsForAddfileIDs = 0;
    @Override
    public void addFileIDs(FileIDsData data, String pillarId, String collectionId) {
        callsForAddfileIDs++;
        integrityModel.addFileIDs(data, pillarId, collectionId);
    }
    public int getCallsForAddFileIDs() {
        return callsForAddfileIDs;        
    }

    private int callsForAddChecksums = 0;
    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
        callsForAddChecksums++;
        integrityModel.addChecksums(data, pillarId, collectionId);
    }
    public int getCallsForAddChecksums() {
        return callsForAddChecksums;
    }

    private int callsForGetFileInfos = 0;
    @Override
    public Collection<FileInfo> getFileInfos(String fileId, String collectionId) {
        callsForGetFileInfos++;
        return integrityModel.getFileInfos(fileId, collectionId);
    }
    public int getCallsForGetFileInfos() {
        return callsForGetFileInfos;
    }

    private int callsForGetAllFileIDs = 0;
    @Override
    public Collection<String> getAllFileIDs(String collectionId) {
        callsForGetAllFileIDs++;
        return integrityModel.getAllFileIDs(collectionId);
    }
    public int getCallsForGetAllFileIDs() {
        return callsForGetAllFileIDs;
    }

    private int callsForGetNumberOfFiles = 0;
    @Override
    public long getNumberOfFiles(String pillarId, String collectionId) {
        callsForGetNumberOfFiles++;
        return integrityModel.getNumberOfFiles(pillarId, collectionId);
    }
    public int getCallsForGetNumberOfFiles() {
        return callsForGetNumberOfFiles;
    }

    private int callsForGetNumberOfMissingFiles = 0;
    @Override
    public long getNumberOfMissingFiles(String pillarId, String collectionId) {
        callsForGetNumberOfMissingFiles++;
        return integrityModel.getNumberOfMissingFiles(pillarId, collectionId);
    }
    public int getCallsForGetNumberOfMissingFiles() {
        return callsForGetNumberOfMissingFiles;
    }

    private int callsForGetNumberOfChecksumErrors = 0;
    @Override
    public long getNumberOfChecksumErrors(String pillarId, String collectionId) {
        callsForGetNumberOfChecksumErrors++;
        return integrityModel.getNumberOfChecksumErrors(pillarId, collectionId);
    }
    public int getCallsForGetNumberOfChecksumErrors() {
        return callsForGetNumberOfChecksumErrors;
    }

    private int callsForSetFileMissing = 0;
    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds, String collectionId) {
        callsForSetFileMissing++;
        integrityModel.setFileMissing(fileId, pillarIds, collectionId);
    }
    public int getCallsForSetFileMissing() {
        return callsForSetFileMissing;
    }

    private int callsForSetChecksumError = 0;
    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds, String collectionId) {
        callsForSetChecksumError++;
        integrityModel.setChecksumError(fileId, pillarIds, collectionId);
    }
    public int getCallsForSetChecksumError() {
        return callsForSetChecksumError;
    }

    private int callsForSetChecksumAgreement = 0;
    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds, String collectionId) {
        callsForSetChecksumAgreement++;
        integrityModel.setChecksumAgreement(fileId, pillarIds, collectionId);
    }
    public int getCallsForSetChecksumAgreement() {
        return callsForSetChecksumAgreement;
    }
    
    private int callsForDeleteFileIdEntry = 0;
    @Override
    public void deleteFileIdEntry(String fileId, String collectionId) {
        callsForDeleteFileIdEntry++;
        integrityModel.deleteFileIdEntry(fileId, collectionId);
    }
    public int getCallsForDeleteFileIdEntry() {
        return callsForDeleteFileIdEntry;
    }
    
    private int callsForFindMissingChecksums = 0;
    @Override
    public List<String> findMissingChecksums(String collectionId) {
        callsForFindMissingChecksums++;
        return integrityModel.findMissingChecksums(collectionId);
    }
    public int getCallsForFindMissingChecksums() {
        return callsForFindMissingChecksums;
    }

    private int callsForFindMissingFiles = 0;
    @Override
    public List<String> findMissingFiles(String collectionId) {
        callsForFindMissingFiles++;
        return integrityModel.findMissingFiles(collectionId);
    }
    public int getCallsForFindMissingFiles() {
        return callsForFindMissingFiles;
    }
    
    private int callsForFindChecksumsOlderThan = 0;
    @Override
    public Collection<String> findChecksumsOlderThan(Date date, String pillarID, String collectionId) {
        callsForFindChecksumsOlderThan++;
        return integrityModel.findChecksumsOlderThan(date, pillarID, collectionId);
    }
    public int getCallsForFindChecksumsOlderThan() {
        return callsForFindChecksumsOlderThan;
    }
    
    private int callsForIsMissing = 0;
    @Override
    public List<String> getPillarsMissingFile(String fileId, String collectionId) {
        callsForIsMissing++;
        return integrityModel.getPillarsMissingFile(fileId, collectionId);
    }
    public int getCallsForIsMissing() {
        return callsForIsMissing;
    }
    
    private int callsForGetFilesWithDistinctChecksums = 0;
    @Override
    public List<String> getFilesWithInconsistentChecksums(String collectionId) {
        callsForGetFilesWithDistinctChecksums++;
        return integrityModel.getFilesWithInconsistentChecksums(collectionId);
    }
    public int getCallsForGetFilesWithDistinctChecksums() {
        return callsForGetFilesWithDistinctChecksums;
    }
    
    private int callsForSetFilesWithUnanimousChecksumToValid = 0;
    @Override
    public void setFilesWithConsistentChecksumToValid(String collectionId) {
        callsForSetFilesWithUnanimousChecksumToValid++;
        integrityModel.setFilesWithConsistentChecksumToValid(collectionId);
    }
    public int getCallsForSetFilesWithUnanimousChecksumToValid() {
        return callsForSetFilesWithUnanimousChecksumToValid;
    }
    
    private int callsForGetDateForNewestFileEntryForPillar = 0;
    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId) {
        callsForGetDateForNewestFileEntryForPillar++;
        return integrityModel.getDateForNewestFileEntryForPillar(pillarId, collectionId);
    }
    public int getCallsForGetDateForNewestFileEntryForPillar() {
        return callsForGetDateForNewestFileEntryForPillar;
    }
    
    private int callsForGetDateForNewestChecksumEntryForPillar = 0;
    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId) {
        callsForGetDateForNewestChecksumEntryForPillar++;
        return integrityModel.getDateForNewestChecksumEntryForPillar(pillarId, collectionId);
    }

    @Override
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getCallsForGetDateForNewestChecksumEntryForPillar() {
        return callsForGetDateForNewestChecksumEntryForPillar;
    }
    private int callsForSetAllFilesToUnknownFileState = 0;
    @Override
    public void setAllFilesToUnknownFileState(String collectionId) {
        callsForSetAllFilesToUnknownFileState++;
        integrityModel.setAllFilesToUnknownFileState(collectionId);
    }
    public int getCallsForSetAllFilesToUnknownFileState() {
        return callsForSetAllFilesToUnknownFileState;
    }
    private int callsForSetUnknownFilesToMissing = 0;
    @Override
    public void setOldUnknownFilesToMissing(String collectionId) {
        callsForSetUnknownFilesToMissing++;
        integrityModel.setOldUnknownFilesToMissing(collectionId);
    }
    public int getCallsForSetUnknownFilesToMissing() {
        return callsForSetUnknownFilesToMissing;
    }
    private int callsForGetFileOnPillar = 0;
    @Override
    public List<String> getFilesOnPillar(String pillarId, long minId, long maxId, String collectionId) {
        callsForGetFileOnPillar++;
        return integrityModel.getFilesOnPillar(pillarId, minId, maxId, collectionId);
    }
    public int getCallsForGetFileOnPillar() {
        return callsForGetFileOnPillar;
    }
    private int callsMissingFilesAtPillar = 0;
    @Override
    public List<String> getMissingFilesAtPillar(String pillarId, long minId, long maxId, String collectionId) {
        callsMissingFilesAtPillar++;
        return integrityModel.getMissingFilesAtPillar(pillarId, minId, maxId, collectionId);
    }
    public int getMissingFilesAtPillar() {
        return callsMissingFilesAtPillar;
    }
    private int callsForFilesWithChecksumErrorsAtPillar = 0;
    @Override
    public List<String> getFilesWithChecksumErrorsAtPillar(String pillarId, long minId, long maxId, String collectionId) {
        callsForFilesWithChecksumErrorsAtPillar++;
        return integrityModel.getFilesWithChecksumErrorsAtPillar(pillarId, minId, maxId, collectionId);
    }
    public int getFilesWithChecksumErrorsAtPillar() {
        return callsForFilesWithChecksumErrorsAtPillar;
    }
}
