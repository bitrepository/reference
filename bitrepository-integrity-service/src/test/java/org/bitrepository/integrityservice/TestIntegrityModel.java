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
package org.bitrepository.integrityservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple integrity model which stores its data in the memory.
 *
 * NOT INTENDED FOR PRODUCTION!!!
 *
 * This does not handle anything about CollectionID.
 */
public class TestIntegrityModel implements IntegrityModel {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    private final List<String> pillarIds;

    /**
     * The memory cache for containing the information about the files in the system.
     * Synchronized for avoiding threading problems.
     */
    private Map<String, CollectionFileIDInfo> cache = Collections.synchronizedMap(new HashMap<String,
        CollectionFileIDInfo>());

    /**
     * Constructor.
     */
    public TestIntegrityModel(List<String> pillarIds) {
        this.pillarIds = pillarIds;
    }

    @Override
    public void addFileIDs(FileIDsData data, FileIDs expectedFileIDs, String pillarId) {
        for(FileIDsDataItem fileId : data.getFileIDsDataItems().getFileIDsDataItem()) {
            log.debug("Adding/updating fileId '" + fileId.getFileID() + "' for the pillar '" + pillarId + "'");
            if(!cache.containsKey(fileId.getFileID())) {
                instantiateFileInfoListForFileId(fileId.getFileID());
            }
            updateFileId(fileId, pillarId);
        }
    }

    /**
     * Updates the file info for a given file id.
     * @param fileIdData The file id data to update with.
     * @param pillarId The id of pillar who delivered these file id data.
     */
    private void updateFileId(FileIDsDataItem fileIdData, String pillarId) {
        CollectionFileIDInfo fileInfos = cache.get(fileIdData.getFileID());
        if(fileInfos == null) {
            fileInfos = new CollectionFileIDInfo(fileIdData.getFileID());
        }

        fileInfos.updateFileIDs(fileIdData, pillarId);
        cache.put(fileIdData.getFileID(), fileInfos);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId) {
        for(ChecksumDataForChecksumSpecTYPE checksumResult : data) {
            log.debug("Adding/updating checksums for file '" + checksumResult.getFileID() + "' for pillar '"
                + pillarId + "'");

            if(!cache.containsKey(checksumResult.getFileID())) {
                instantiateFileInfoListForFileId(checksumResult.getFileID());
            }
            updateChecksum(checksumResult, pillarId);
        }
    }

    /**
     * Updates a file info with checksum results.
     * @param checksumData The results of a checksum calculation.
     * @param pillarId The id of the pillar, where it has been calculated.
     */
    private void updateChecksum(ChecksumDataForChecksumSpecTYPE checksumData, String pillarId) {
        CollectionFileIDInfo fileInfos = cache.get(checksumData.getFileID());

        fileInfos.updateChecksums(checksumData, pillarId);
        cache.put(checksumData.getFileID(), fileInfos);
    }

    /**
     * Instantiates a new List for the file id infos for a given file id.
     * @param fileId The id of the file to be inserted into the cache. 
     */
    private synchronized void instantiateFileInfoListForFileId(String fileId) {
        if(cache.containsKey(fileId)) {
            log.warn("Attempt to instantiate file, which already exists, averted");
            return;
        }
        CollectionFileIDInfo fileIdInfo = new CollectionFileIDInfo(fileId);
        cache.put(fileId, fileIdInfo);
    }

    @Override
    public List<FileInfo> getFileInfos(String fileId) {
        if(cache.containsKey(fileId)) {
            return cache.get(fileId).getFileIDInfos();
        } else {
            return new ArrayList<FileInfo>();
        }
    }

    @Override
    public Collection<String> getAllFileIDs() {
        return cache.keySet();
    }

    /**
     * Clean the cache for test purposes only!
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Container for the collection of FileIDInfos for a single file.
     *
     * All functions are package-protected.
     */
    private class CollectionFileIDInfo {
        /** The collection of FileIDInfos.*/
        private List<FileInfo> fileIDInfos = new ArrayList<FileInfo>();

        /**
         * Constructor. Initializes and empty list of FileIDInfos.
         */
        CollectionFileIDInfo(String fileId) {
            for(String pillarId : pillarIds) {
                fileIDInfos.add(new FileInfo(fileId, pillarId));
            }
        }

        /**
         * Updates the FileIDInfo for a given pillar based on the results of a GetFileIDs operation.
         * @param fileIdData The resulting date from the GetFileIDs operation.
         * @param pillarId The id of the pillar.
         */
        void updateFileIDs(FileIDsDataItem fileIdData, String pillarId) {
            // Extract current file info or create a new one.
            FileInfo currentInfo = null;
            for(int i = 0; i < fileIDInfos.size(); i++) {
                FileInfo fileInfo = fileIDInfos.get(i);
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileIDInfos.remove(i);
                    i--; // make up for removal
                }
            }
            if(currentInfo == null) {
                currentInfo = new FileInfo(fileIdData.getFileID(), pillarId);
            }

            currentInfo.setDateForLastFileIDCheck(fileIdData.getLastModificationTime());
            currentInfo.setFileState(FileState.EXISTING);
            currentInfo.setChecksumState(ChecksumState.UNKNOWN);

            // put it back into the list and that back into the cache.
            fileIDInfos.add(currentInfo);
        }

        /**
         * Updates the FileIDInfo for a given pillar based on the results of a GetChecksums operation.
         * Also updates the 'latestChecksumUpdate'.
         *
         * @param checksumData The results of the GetChecksumOperation for this given file.
         * @param pillarId The id of the pillar.
         */
        void updateChecksums(ChecksumDataForChecksumSpecTYPE checksumData, String pillarId) {

            // Extract current file info and update it or create a new one.
            FileInfo currentInfo = null;
            Iterator<FileInfo> it = fileIDInfos.iterator();
            while(it.hasNext()) {
                FileInfo fi = it.next();
                if(fi.getPillarId().equals(pillarId)) {
                    currentInfo = fi;
                    it.remove();
                }
            }
            if(currentInfo == null) {
                // create a new file info
                currentInfo = new FileInfo(checksumData.getFileID(), CalendarUtils.getEpoch(),
                    Base16Utils.decodeBase16(checksumData.getChecksumValue()),
                    checksumData.getCalculationTimestamp(), pillarId, FileState.EXISTING, ChecksumState.UNKNOWN);
            } else {
                // Update the existing file info
                currentInfo.setDateForLastChecksumCheck(checksumData.getCalculationTimestamp());
                currentInfo.setChecksum(Base16Utils.decodeBase16(checksumData.getChecksumValue()));
                currentInfo.setFileState(FileState.EXISTING);
            }
            // put it back into the list and that back into the cache.
            fileIDInfos.add(currentInfo);
        }

        /**
         * @return All the FileIDInfos for this given file.
         */
        List<FileInfo> getFileIDInfos() {
            return fileIDInfos;
        }
    }

    @Override
    public long getNumberOfFiles(String pillarId) {
        return cache.size();
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId) {
        long res = 0L;
        for(CollectionFileIDInfo fileinfos : cache.values()) {
            for(FileInfo fi : fileinfos.fileIDInfos) {
                if((fi.getPillarId() == pillarId) && (fi.getFileState() == FileState.MISSING)) {
                    res++;
                }
            }
        }
        return res;
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId) {
        long res = 0L;
        for(CollectionFileIDInfo fileinfos : cache.values()) {
            for(FileInfo fi : fileinfos.fileIDInfos) {
                if((fi.getPillarId() == pillarId) && (fi.getChecksumState() == ChecksumState.ERROR)) {
                    res++;
                }
            }
        }
        return res;
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds) {
        CollectionFileIDInfo fileinfos = cache.get(fileId);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(pillarIds.contains(fi.getPillarId())) {
                fi.setFileState(FileState.MISSING);
            }
        }
        cache.put(fileId, fileinfos);
    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds) {
        CollectionFileIDInfo fileinfos = cache.get(fileId);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(pillarIds.contains(fi.getPillarId())) {
                fi.setChecksumState(ChecksumState.ERROR);
            }
        }
        cache.put(fileId, fileinfos);
    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds) {
        CollectionFileIDInfo fileinfos = cache.get(fileId);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(pillarIds.contains(fi.getPillarId())) {
                fi.setChecksumState(ChecksumState.VALID);
            }
        }
        cache.put(fileId, fileinfos);
    }

    @Override
    public void deleteFileIdEntry(String fileId) {
        cache.remove(fileId);
    }

    @Override
    public List<String> findMissingChecksums() {
        List<String> res = new ArrayList<String>();
        for(CollectionFileIDInfo fileinfos : cache.values()) {
            for(FileInfo fi : fileinfos.fileIDInfos) {
                if((fi.getFileState() == FileState.EXISTING) && (fi.getChecksumState() == ChecksumState.UNKNOWN)) {
                    res.add(fi.getFileId());
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public List<String> findMissingFiles() {
        List<String> res = new ArrayList<String>();
        for(CollectionFileIDInfo fileinfos : cache.values()) {
            for(FileInfo fi : fileinfos.fileIDInfos) {
                if(fi.getFileState() == FileState.MISSING) {
                    res.add(fi.getFileId());
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public Collection<String> findChecksumsOlderThan(Date date, String pillarID) {
        List<String> res = new ArrayList<String>();
        for(CollectionFileIDInfo fileinfos : cache.values()) {
            for(FileInfo fi : fileinfos.fileIDInfos) {
                if (pillarID.equals(fi.getPillarId())) {
                    if(CalendarUtils.convertFromXMLGregorianCalendar(fi.getDateForLastChecksumCheck()).getTime()
                        < date.getTime()) {
                        res.add(fi.getFileId());
                        break;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public List<String> getPillarsMissingFile(String fileId) {
        if(!cache.containsKey(fileId))  {
            return new ArrayList<String>();
        }

        List<String> res = new ArrayList<String>(pillarIds);
        CollectionFileIDInfo fileinfos = cache.get(fileId);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(fi.getFileState() == FileState.EXISTING) {
                res.remove(fi.getPillarId());
            }
        }
        return res;
    }

    @Override
    public List<String> getFilesWithInconsistentChecksums() {
        List<String> res = new ArrayList<String>();
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            HashSet<String> checksums = new HashSet<String>();
            for(FileInfo fileinfo : collectionInfo.getValue().getFileIDInfos()) {
                if(fileinfo.getChecksum() != null && fileinfo.getFileState() != FileState.MISSING) {
                    checksums.add(fileinfo.getChecksum());
                }
            }
            // more than 1 checksum, means disagreements.
            if(checksums.size() > 1) {
                res.add(collectionInfo.getKey());
            }
        }
        return res;
    }

    @Override
    public void setFilesWithConsistentChecksumToValid() {
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            HashSet<String> checksums = new HashSet<String>();
            for(FileInfo fileinfo : collectionInfo.getValue().getFileIDInfos()) {
                if(fileinfo.getChecksum() != null && fileinfo.getFileState() != FileState.MISSING) {
                    checksums.add(fileinfo.getChecksum());
                }
            }
            // 1 checksum means unanimous checksum.
            if(checksums.size() == 1) {
                for(FileInfo fileinfo : collectionInfo.getValue().getFileIDInfos()) {
                    if(fileinfo.getChecksum() != null && fileinfo.getFileState() != FileState.MISSING) {
                        fileinfo.setChecksumState(ChecksumState.VALID);
                    }
                }
            }
        }
    }
}
