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

package org.bitrepository.integrityservice.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds caching functionality for all the simple read methods. Read operations returning set or lists of results
 * are still delegated directly to the actual model.
 */
public class IntegrityCache implements IntegrityModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final IntegrityModel integrityModel;
    private JCS missingFilesCache;
    private JCS filesCache;
    private JCS corruptFilesCache;
    /** Seconds to wait from a cache elements has been updated to the elements is reloaded from the model **/
    protected int refreshPeriodAfterDirtyMark = 5;

    public IntegrityCache(IntegrityModel integrityModel) {
        this.integrityModel = integrityModel;
        try {
            missingFilesCache = JCS.getInstance("MissingFilesCache");
            filesCache = JCS.getInstance("FilesCache");
            corruptFilesCache = JCS.getInstance("CorruptFilesCache");
        } catch (CacheException ce) {
            throw new IllegalStateException("Failed to initialise caches", ce);
        }
    }

    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        markPillarsDirty(filesCache, Arrays.asList(new String[]{pillarId}));
        integrityModel.addFileIDs(data, pillarId);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId) {
        integrityModel.addChecksums(data, pillarId);
    }

    @Override
    public Collection<FileInfo> getFileInfos(String fileId) {
        return integrityModel.getFileInfos(fileId);
    }

    @Override
    public Collection<String> getAllFileIDs() {
        return integrityModel.getAllFileIDs();
    }

    @Override
    public long getNumberOfFiles(String pillarId) {
        Long numberOfFiles = (Long)filesCache.get(getCacheID(pillarId));
        if (numberOfFiles == null) {
            numberOfFiles = integrityModel.getNumberOfFiles(getCacheID(pillarId));
            updateCache(filesCache, pillarId, Long.valueOf(numberOfFiles));
        }
        return numberOfFiles.longValue();
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId) {
        Long numberOfMissingFiles = (Long)missingFilesCache.get(getCacheID(pillarId));
        if (numberOfMissingFiles == null) {
            numberOfMissingFiles = integrityModel.getNumberOfMissingFiles(getCacheID(pillarId));
            updateCache(missingFilesCache, pillarId, Long.valueOf(numberOfMissingFiles));
        }
        return numberOfMissingFiles.longValue();
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId) {
        Long numberOfCorruptFiles = (Long)corruptFilesCache.get(getCacheID(pillarId));
        if (numberOfCorruptFiles == null) {
            numberOfCorruptFiles = integrityModel.getNumberOfChecksumErrors(getCacheID(pillarId));
            updateCache(corruptFilesCache, pillarId, Long.valueOf(numberOfCorruptFiles));
        }
        return numberOfCorruptFiles.longValue();
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds) {
        markPillarsDirty(missingFilesCache, pillarIds);
        markPillarsDirty(filesCache, pillarIds);
        integrityModel.setFileMissing(fileId, pillarIds);
    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds) {
        markPillarsDirty(corruptFilesCache, pillarIds);
        integrityModel.setChecksumError(fileId, pillarIds);
    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds) {
        markPillarsDirty(corruptFilesCache, pillarIds);
        integrityModel.setChecksumAgreement(fileId, pillarIds);
    }

    @Override
    public void deleteFileIdEntry(String fileId) {
        try {
            missingFilesCache.clear();
            filesCache.clear();
            corruptFilesCache.clear();
        } catch (CacheException ce) {
            log.warn("Failed to clear cache.", ce);
        }
        integrityModel.deleteFileIdEntry(fileId);
    }

    @Override
    public List<String> findMissingChecksums() {
        return integrityModel.findMissingChecksums();
    }

    @Override
    public Collection<String> findChecksumsOlderThan(Date date, String pillarID) {
        return integrityModel.findChecksumsOlderThan(date, pillarID);
    }

    @Override
    public List<String> findMissingFiles() {
        return integrityModel.findMissingFiles();
    }

    @Override
    public List<String> getPillarsMissingFile(String fileId) {
        return integrityModel.getPillarsMissingFile(fileId);
    }

    @Override
    public List<String> getFilesWithInconsistentChecksums() {
        return integrityModel.getFilesWithInconsistentChecksums();
    }

    @Override
    public void setFilesWithConsistentChecksumToValid() {
        try {
            corruptFilesCache.clear();
        } catch (CacheException ce) {
            log.warn("Failed to clear cache.", ce);
        }
        integrityModel.setFilesWithConsistentChecksumToValid();
    }

    @Override
    public void setAllFilesToUnknownFileState() {
        integrityModel.setAllFilesToUnknownFileState();
    }

    @Override
    public void setOldUnknownFilesToMissing() {
        try {
            missingFilesCache.clear();
        } catch (CacheException ce) {
            log.warn("Failed to update cache.", ce);
        }
        integrityModel.setOldUnknownFilesToMissing();
    }

    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarId) {
        return integrityModel.getDateForNewestFileEntryForPillar(pillarId);
    }

    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarId) {
        return integrityModel.getDateForNewestChecksumEntryForPillar(pillarId);
    }

    /**
     * Will cause the cache to return null after <code>refreshPeriodAfterDirtyMark</code> has passed, since
     * the value was updated in the cache. This will ensure that the backend model isn't read more than
     *  each <code>refreshPeriodAfterDirtyMark</code> second.
     * @param cache The cache to use.
     * @param pillarIds The pillars to mark dirty
     */
    private void markPillarsDirty(JCS cache, Collection<String> pillarIds) {
        for (String pillarID:pillarIds) {
            try {
                cache.getElementAttributes(getCacheID(pillarID)).setMaxLifeSeconds(refreshPeriodAfterDirtyMark);
            } catch (CacheException ce) {
                log.warn("Failed to read cache.", ce);
            }
        }
    }

    /**
     * Updates the cache with the given value.
     */
    private void updateCache(JCS cache, String pillarID, Object value) {
        try {
            cache.put(getCacheID(pillarID), value);
        } catch (CacheException ce) {
            log.warn("Failed to update cache.", ce);
        }
    }

    /**
     * Creates IDs for elemets in the cache consistently.
     *
     * Note that the current implementations is trivial but will evolve.
     */
    private String getCacheID(String pillarID) {
        return pillarID;
    }
}
