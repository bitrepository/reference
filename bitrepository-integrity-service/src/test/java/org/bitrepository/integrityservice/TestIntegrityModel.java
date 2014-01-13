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
import java.util.Set;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.PillarStat;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
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

    private String makeCacheKey(String fileId, String collectionId) {
        return fileId + "-" + collectionId;
    }
    
    /**
     * Constructor.
     */
    public TestIntegrityModel(List<String> pillarIds) {
        this.pillarIds = pillarIds;
    }

    @Override
    public void addFileIDs(FileIDsData data, String pillarId, String collectionId) {
        for(FileIDsDataItem fileId : data.getFileIDsDataItems().getFileIDsDataItem()) {
            log.debug("Adding/updating fileId '" + fileId.getFileID() + "' for the pillar '" + pillarId + "'" +
            		" in collection '" + collectionId + "'");
            String cacheKey = makeCacheKey(fileId.getFileID(), collectionId);
            if(!cache.containsKey(cacheKey)) {
                instantiateFileInfoListForFileId(fileId.getFileID(), collectionId);
            }
            updateFileId(fileId, pillarId, collectionId);
        }
    }

    /**
     * Updates the file info for a given file id.
     * @param fileIdData The file id data to update with.
     * @param pillarId The id of pillar who delivered these file id data.
     */
    private void updateFileId(FileIDsDataItem fileIdData, String pillarId, String collectionId) {
        String cacheKey = makeCacheKey(fileIdData.getFileID(), collectionId);
        CollectionFileIDInfo fileInfos = cache.get(cacheKey);
        if(fileInfos == null) {
            fileInfos = new CollectionFileIDInfo(fileIdData.getFileID());
        }

        fileInfos.updateFileIDs(fileIdData, pillarId);
        cache.put(cacheKey, fileInfos);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
        for(ChecksumDataForChecksumSpecTYPE checksumResult : data) {
            log.debug("Adding/updating checksums for file '" + checksumResult.getFileID() + "' for pillar '"
                + pillarId + "' in collection '" + collectionId + "'");
            String cacheKey = makeCacheKey(checksumResult.getFileID(), collectionId);
            if(!cache.containsKey(cacheKey)) {
                instantiateFileInfoListForFileId(checksumResult.getFileID(), collectionId);
            }
            updateChecksum(checksumResult, pillarId, collectionId);
        }
    }

    /**
     * Updates a file info with checksum results.
     * @param checksumData The results of a checksum calculation.
     * @param pillarId The id of the pillar, where it has been calculated.
     */
    private void updateChecksum(ChecksumDataForChecksumSpecTYPE checksumData, String pillarId, String collectionId) {
        String cacheKey = makeCacheKey(checksumData.getFileID(), collectionId);
        CollectionFileIDInfo fileInfos = cache.get(cacheKey);

        fileInfos.updateChecksums(checksumData, pillarId);
        cache.put(cacheKey, fileInfos);
    }

    /**
     * Instantiates a new List for the file id infos for a given file id.
     * @param fileId The id of the file to be inserted into the cache. 
     */
    private synchronized void instantiateFileInfoListForFileId(String fileId, String collectionId) {
        String cacheKey = makeCacheKey(fileId, collectionId);
        if(cache.containsKey(cacheKey)) {
            log.warn("Attempt to instantiate file, which already exists, averted");
            return;
        }
        CollectionFileIDInfo fileIdInfo = new CollectionFileIDInfo(fileId);
        cache.put(cacheKey, fileIdInfo);
    }

    @Override
    public List<FileInfo> getFileInfos(String fileId, String collectionId) {
        String cacheKey = makeCacheKey(fileId, collectionId);
        if(cache.containsKey(cacheKey)) {
            return cache.get(cacheKey).getFileIDInfos();
        } else {
            return new ArrayList<FileInfo>();
        }
    }
    

    @Override
    public long getNumberOfFilesInCollection(String collectionId) {
        long numberOfFiles = 0;
        for(String file : cache.keySet()) {
            if(file.endsWith("-" + collectionId)) {
                numberOfFiles++;
            }
        }
        
        return numberOfFiles;
    }

    @Override
    public Collection<String> getAllFileIDs(String collectionId) {
        Set<String> files = new HashSet<String>();
        for(String file : cache.keySet()) {
            if(file.endsWith("-" + collectionId)) {
                files.add(cache.get(file).getFileIDInfos().get(0).getFileId());
            }
        }
        return files;
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
                    Base16Utils.decodeBase16(checksumData.getChecksumValue()), null,
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
        
        void insertFileInfo(FileInfo fileInfo, String pillarID) {
            FileInfo oldFi = null;
            for(FileInfo fi : fileIDInfos) {
                if(fi.getPillarId() == pillarID) {
                    oldFi = fi;
                    break;
                }
            }
            if(oldFi != null) {
                fileIDInfos.remove(oldFi);
            }
            
            fileIDInfos.add(fileInfo);
        }

        /**
         * @return All the FileIDInfos for this given file.
         */
        List<FileInfo> getFileIDInfos() {
            return fileIDInfos;
        }
    }

    @Override
    public long getNumberOfFiles(String pillarId, String collectionID) {
        long res = 0L;
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionID)) {
                CollectionFileIDInfo fileinfos = cache.get(key);
                for(FileInfo fi : fileinfos.fileIDInfos) {
                    if((fi.getPillarId() == pillarId) && (fi.getFileState() == FileState.EXISTING)) {
                        res++;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId, String collectionID) {
        long res = 0L;
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionID)) {
                CollectionFileIDInfo fileinfos = cache.get(key);
                for(FileInfo fi : fileinfos.fileIDInfos) {
                    if((fi.getPillarId() == pillarId) && (fi.getFileState() == FileState.MISSING)) {
                        res++;
                    }
                }    
            }
        }
        return res;
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId, String collectionID) {
        long res = 0L;
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionID)) {
                CollectionFileIDInfo fileinfos = cache.get(key);
                for(FileInfo fi : fileinfos.fileIDInfos) {
                    if((fi.getPillarId() == pillarId) && (fi.getChecksumState() == ChecksumState.ERROR)) {
                        res++;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds, String collectionId) {
        String cacheKey = makeCacheKey(fileId, collectionId);
        CollectionFileIDInfo fileinfos = cache.get(cacheKey);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(pillarIds.contains(fi.getPillarId())) {
                fi.setFileState(FileState.MISSING);
            }
        }
        cache.put(cacheKey, fileinfos);
    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds, String collectionId) {
        String cacheKey = makeCacheKey(fileId, collectionId);
        CollectionFileIDInfo fileinfos = cache.get(cacheKey);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(pillarIds.contains(fi.getPillarId())) {
                fi.setChecksumState(ChecksumState.ERROR);
            }
        }
        cache.put(cacheKey, fileinfos);
    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds, String collectionId) {
        String cacheKey = makeCacheKey(fileId, collectionId);
        CollectionFileIDInfo fileinfos = cache.get(cacheKey);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(pillarIds.contains(fi.getPillarId())) {
                fi.setChecksumState(ChecksumState.VALID);
            }
        }
        cache.put(cacheKey, fileinfos);
    }

    @Override
    public void deleteFileIdEntry(String fileId, String collectionId) {
        cache.remove(makeCacheKey(fileId, collectionId));
    }

    @Override
    public List<String> findMissingChecksums(String collectionId) {
        List<String> res = new ArrayList<String>();
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionId)) {
                CollectionFileIDInfo fileinfos = cache.get(key);
                for(FileInfo fi : fileinfos.fileIDInfos) {
                    if((fi.getFileState() == FileState.EXISTING) && (fi.getChecksumState() == ChecksumState.UNKNOWN)) {
                        res.add(fi.getFileId());
                        break;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public List<String> findMissingFiles(String collectionId) {
        List<String> res = new ArrayList<String>();
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionId)) {
                CollectionFileIDInfo fileinfos = cache.get(key);
                for(FileInfo fi : fileinfos.fileIDInfos) {
                    if(fi.getFileState() == FileState.MISSING) {
                        res.add(fi.getFileId());
                        break;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public Collection<String> findChecksumsOlderThan(Date date, String pillarID, String collectionId) {
        List<String> res = new ArrayList<String>();
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionId)) {
                CollectionFileIDInfo fileinfos = cache.get(key);
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
        }
        return res;
    }

    @Override
    public List<String> getPillarsMissingFile(String fileId, String collectionId) {
        String cacheKey = makeCacheKey(fileId, collectionId);
        if(!cache.containsKey(cacheKey))  {
            return new ArrayList<String>();
        }

        List<String> res = new ArrayList<String>(pillarIds);
        CollectionFileIDInfo fileinfos = cache.get(cacheKey);
        for(FileInfo fi : fileinfos.fileIDInfos)  {
            if(fi.getFileState() == FileState.EXISTING) {
                res.remove(fi.getPillarId());
            }
        }
        return res;
    }

    @Override
    public List<String> getFilesWithInconsistentChecksums(String collectionId) {
        List<String> res = new ArrayList<String>();
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            if(collectionInfo.getKey().endsWith("-" + collectionId)) {
                HashSet<String> checksums = new HashSet<String>();
                for(FileInfo fileinfo : collectionInfo.getValue().getFileIDInfos()) {
                    if(fileinfo.getChecksum() != null && fileinfo.getFileState() != FileState.MISSING) {
                        checksums.add(fileinfo.getChecksum());
                    }
                }
                // more than 1 checksum, means disagreements.
                if(checksums.size() > 1) {
                    res.add(collectionInfo.getValue().getFileIDInfos().get(0).getFileId());
                }
            }
        }
        return res;
    }

    @Override
    public void setFilesWithConsistentChecksumToValid(String collectionId) {
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            if(collectionInfo.getKey().endsWith("-" + collectionId)) {
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

    @Override
    public Date getDateForNewestFileEntryForCollection(String collectionId) {
        XMLGregorianCalendar res = CalendarUtils.getEpoch();
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionId)) {
                CollectionFileIDInfo collectionInfo = cache.get(key);
                for(FileInfo fileInfo : collectionInfo.getFileIDInfos()) {
                    if(fileInfo.getDateForLastFileIDCheck().compare(res) == DatatypeConstants.GREATER) {
                        res = fileInfo.getDateForLastFileIDCheck();
                    }
                }
            }
        }
        return CalendarUtils.convertFromXMLGregorianCalendar(res);
    }
    
    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId) {
        XMLGregorianCalendar res = CalendarUtils.getEpoch();
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionId)) {
                CollectionFileIDInfo collectionInfo = cache.get(key);
                for(FileInfo fileInfo : collectionInfo.getFileIDInfos()) {
                    if(fileInfo.getPillarId().equals(pillarId)) {
                        if(fileInfo.getDateForLastFileIDCheck().compare(res) == DatatypeConstants.GREATER) {
                            res = fileInfo.getDateForLastFileIDCheck();
                        }
                    }
                }
            }
        }
        return CalendarUtils.convertFromXMLGregorianCalendar(res);
    }

    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId) {
        XMLGregorianCalendar res = CalendarUtils.getEpoch();
        for(String key : cache.keySet()) {
            if(key.endsWith("-" + collectionId)) {
                CollectionFileIDInfo collectionInfo = cache.get(key);
                for(FileInfo fileInfo : collectionInfo.getFileIDInfos()) {
                    if(fileInfo.getPillarId().equals(pillarId)) {
                        if(fileInfo.getDateForLastChecksumCheck().compare(res) == DatatypeConstants.GREATER) {
                            res = fileInfo.getDateForLastChecksumCheck();
                        }
                    }
                }    
            }
        }
        return CalendarUtils.convertFromXMLGregorianCalendar(res);
    }

    @Override
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setExistingFilesToPreviouslySeenFileState(String collectionId) {
        for(String s : cache.keySet()) {
            if(s.endsWith("-" + collectionId)) {
                CollectionFileIDInfo collectionInfo = cache.get(s);
                CollectionFileIDInfo info = new CollectionFileIDInfo(s);            
                for(FileInfo fileinfo : collectionInfo.getFileIDInfos()) {
                    fileinfo.setFileState(FileState.UNKNOWN);
                    info.insertFileInfo(fileinfo, fileinfo.getPillarId());
                }
                cache.put(s, info);
            }
        }
    }

    @Override
    public void setOldUnknownFilesToMissing(String collectionId) {
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            if(collectionInfo.getKey().endsWith("-" + collectionId)) {
                for(FileInfo fileinfo : collectionInfo.getValue().getFileIDInfos()) {
                    if(fileinfo.getFileState() == FileState.UNKNOWN) {
                        fileinfo.setFileState(FileState.MISSING);
                    }
                }
            }
        }
    }

    @Override
    public IntegrityIssueIterator getFilesOnPillar(String pillarId, long minId, long maxId, String collectionId) {
        return null;
    }

    @Override
    public List<String> getFilesWithChecksumErrorsAtPillar(String pillarId, long minId, long maxId, String collectionId) {
        ArrayList<String> res = new ArrayList<String>();
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            if(collectionInfo.getKey().endsWith("-" + collectionId)) {
                for(FileInfo fileinfo : collectionInfo.getValue().getFileIDInfos()) {
                    if(fileinfo.getPillarId().equals(pillarId) && fileinfo.getChecksumState() == ChecksumState.ERROR) {
                        res.add(collectionInfo.getKey());
                    }
                }
            }
        }
        
        return res.subList((int) minId, (int) maxId);
    }


    @Override
    public Long getCollectionFileSize(String collectionId) {
        long summedSize = 0;
        for(Map.Entry<String, CollectionFileIDInfo> collectionInfo : cache.entrySet()) {
            if(collectionInfo.getKey().endsWith("-" + collectionId)) {
                FileInfo fileinfo = collectionInfo.getValue().getFileIDInfos().get(0);
                summedSize += fileinfo.getFileSize();
            }
        }
        return summedSize;
    }

    @Override
    public List<CollectionStat> getLatestCollectionStat(String collectionID, int count) {
        // TODO fix this...
        return null;
    }
    
    @Override 
    public List<PillarStat> getLatestPillarStats(String collectionId) {
        // TODO fix this...
        return null;
    }

    @Override
    public void makeStatisticsForCollection(String collectionID) {
        // TODO Fix this...
    }

    @Override
    public Long getPillarDataSize(String pillarID) {
        // TODO Fix this
        return null;
    }
    
    @Override
    public void setPreviouslySeenFilesToMissing(String collectionId) {
        // TODO Fix this
    }
    
    @Override
    public void setPreviouslySeenToExisting(String collectionId, String pillarId) {
        // TODO Fix this
    }

    @Override
    public boolean hasFile(String fileId, String collectionId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> findOrphanFiles(String collectionID) {
        // TODO Fix this
        return null;
    }

    @Override
    public IntegrityIssueIterator getMissingFilesAtPillarByIterator(
            String pillarId, long minId, long maxId, String collectionId) {
        return null;
    }
}
