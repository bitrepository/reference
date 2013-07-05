/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.referencepillar.archive;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.cache.database.ExtractedFileIDsResultSet;
import org.bitrepository.service.AlarmDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The full ReferencePillar managing of the checksum store with respect to the ReferenceArchive.
 * 
 */
public class ReferenceChecksumManager {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The storage of checksums.*/
    private final ChecksumStore cache;
    /** The file archives for the different collections.*/
    private final FileStore archives;
    /** The default checksum specification.*/
    private final ChecksumSpecTYPE defaultChecksumSpec;
    /** The dispatcher of alarms.*/
    private final AlarmDispatcher alarmDispatcher;
    /** The settings.*/
    private final Settings settings;

    /**
     * Constructor.
     * @param archive The archive with the data.
     * @param cache The storage for the checksums.
     * @param alarmDispatcher The alarm dispatcher.
     * @param defaultChecksumSpec The default specifications for the checksums.
     * @param maxAgeForChecksum The maximum age for the checksums.
     */
    public ReferenceChecksumManager(FileStore archives, ChecksumStore cache, AlarmDispatcher alarmDispatcher, 
            Settings settings) {
        this.cache = cache;
        this.archives = archives;
        this.alarmDispatcher = alarmDispatcher;
        this.settings = settings;
        this.defaultChecksumSpec = ChecksumUtils.getDefault(settings);
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
        if(csType.equals(defaultChecksumSpec)) {
            log.trace("Default checksum specification: {}.", csType);
            ensureChecksumState(fileId, collectionId);
            return cache.getEntry(fileId, collectionId);
        } else {
            log.trace("Non-default checksum specification: {}. Recalculating the checksums.", csType);
            recalculateChecksum(fileId, collectionId);

            FileInfo fi = archives.getFileInfo(fileId, collectionId);
            String checksum = ChecksumUtils.generateChecksum(fi, csType);
            return new ChecksumEntry(fileId, checksum, new Date());
        }
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
        if(csType == defaultChecksumSpec) {
            ensureChecksumState(fileId, collectionId);
            return cache.getChecksum(fileId, collectionId);            
        } else {
            recalculateChecksum(fileId, collectionId);

            FileInfo fi = archives.getFileInfo(fileId, collectionId);
            return ChecksumUtils.generateChecksum(fi, csType);
        }
    }

    /**
     * Recalculates the checksum of a given file based on the default checksum specification.
     * @param fileId The id of the file to recalculate its default checksum for.
     * @param collectionId The id of the collection of the file.
     */
    public void recalculateChecksum(String fileId, String collectionId) {
        log.info("Recalculating the checksum of file '" + fileId + "'.");
        FileInfo fi = archives.getFileInfo(fileId, collectionId);
        String checksum = ChecksumUtils.generateChecksum(fi, defaultChecksumSpec);
        cache.insertChecksumCalculation(fileId, collectionId, checksum, new Date());
    }

    /**
     * Removes the entry for the given file.
     * @param fileId The id of the file to remove.
     * @param collectionId The id of the collection of the file.
     */
    public void deleteEntry(String fileId, String collectionId) {
        cache.deleteEntry(fileId, collectionId);
    }

    /**
     * Calculates the checksum of a file within the tmpDir.
     * @param fileId The id of the file to calculate the checksum for.
     * @param collectionId The id of the collection of the file.
     * @param csType The specification for the type of checksum to calculate.
     * @return The checksum of the given type for the file with the given id.
     */
    public String getChecksumForTempFile(String fileId, String collectionId, ChecksumSpecTYPE csType) {
        FileInfo fi = archives.getFileInTmpDir(fileId, collectionId);
        return ChecksumUtils.generateChecksum(fi, csType);
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
     * Retrieves the entry for a given file with a given checksumSpec in the ChecksumDataForChecksumSpecTYPE format.
     * @param fileId The id of the file to retrieve the data from.
     * @param collectionId The id of the collection of the file.
     * @param csType The type of checksum to calculate.
     * @return The entry encapsulated in the ChecksumDataForChecksumSpecTYPE data format.
     */    
    public ChecksumDataForChecksumSpecTYPE getChecksumDataForChecksumSpec(String fileId, String collectionId,
            ChecksumSpecTYPE csType) {
        ChecksumEntry csEntry = getChecksumEntryForFile(fileId, collectionId, csType);
        ChecksumDataForChecksumSpecTYPE res = new ChecksumDataForChecksumSpecTYPE();
        res.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(csEntry.getCalculationDate()));
        res.setFileID(csEntry.getFileId());
        res.setChecksumValue(Base16Utils.encodeBase16(csEntry.getChecksum()));

        return res;
    }

    /**
     * Ensures, that all files are up to date, and retrieves the requested entries.
     * @param minTimeStamp The minimum date for the timestamp of the extracted checksum entries.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted checksum entries.
     * @param maxNumberOfResults The maximum number of results.
     * @param collectionId The id of the collection.
     * @param spec The checksum specification.
     * @return If default checksum, then the checksum entries from the store. Otherwise recalculate all the requested
     * checksums.
     */
    public ExtractedChecksumResultSet getEntries(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionId, ChecksumSpecTYPE spec) {
        if(spec.equals(defaultChecksumSpec)) {
            ensureStateOfAllData(collectionId);
            return cache.getEntries(minTimeStamp, maxTimeStamp, maxNumberOfResults, collectionId);
        } else {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();

            long i = 0;
            for(String fileId : cache.getAllFileIDs(collectionId)) {
                if(maxNumberOfResults != null && i > maxNumberOfResults) {
                    break;
                }
                i++;

                String checksum = getChecksumForFile(fileId, collectionId, spec);
                ChecksumEntry entry = new ChecksumEntry(fileId, checksum, new Date());
                res.insertChecksumEntry(entry);    			
            }

            return res;
        }
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
        for(String fileId : archives.getAllFileIds(collectionId)) {
            FileInfo fi = archives.getFileInfo(fileId, collectionId);
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
    public void ensureStateOfAllData(String collectionId) {
        for(String fileId : cache.getAllFileIDs(collectionId)) {
            ensureFileState(fileId, collectionId);
        }

        for(String fileId : archives.getAllFileIds(collectionId)) {
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
        if(!archives.hasFile(fileId, collectionId)) {
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
     * Ensures that the cache has an non-deprecated checksum for the given file.
     * Also validates, that the checksum is up to date with the file.
     * @param fileId The id of the file.
     * @param collectionId The id of the collection of the file.
     */
    private void ensureChecksumState(String fileId, String collectionId) {
        Long maxAgeForChecksums = settings.getReferenceSettings().getPillarSettings()
                .getMaxAgeForChecksums().longValue();
        if(!cache.hasFile(fileId, collectionId)) {
            log.debug("No checksum cached for file '" + fileId + "'. Calculating the checksum.");
            recalculateChecksum(fileId, collectionId);
        } else {
            long checksumDate = cache.getCalculationDate(fileId, collectionId).getTime();
            long minDateForChecksum = System.currentTimeMillis() - maxAgeForChecksums;
            if(checksumDate < minDateForChecksum) {
                log.info("The checksum for the file '" + fileId + "' is too old. Recalculating.");                
                recalculateChecksum(fileId, collectionId);
            } else if(checksumDate < archives.getFileInfo(fileId, collectionId).getMdate()) {
                log.info("The last modified date for the file is newer than the latest checksum.");                
                recalculateChecksum(fileId, collectionId);
            }
        }
    }
}
