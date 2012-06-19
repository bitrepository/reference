package org.bitrepository.integrityservice.scheduler.workflow.step;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * 
 * It goes through every file id available, extracts all the fileinfos and validates the timestamp for the given 
 * checksums. If it is too old, then it will mark the given file id for retrieval of checksums.
 * 
 * TODO make a method in the database for extracting all the ids of all the files which have the checksum state 
 * Unknown for any pillar. 
 */
public class FindObsoleteChecksumsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The interval for a checksum timestamp to timeout and become obsolete.*/
    private final Long obsoleteTimeout;
    /** The mapping between */
    private List<String> obsoleteChecksums = new ArrayList<String>();
    
    /**
     * Constructor.
     * @param store The storage for the integrity data.
     * @param obsoleteTimeout The interval for a checksum timestamp to timeout and become obsolete.
     */
    public FindObsoleteChecksumsStep(IntegrityModel store, long obsoleteTimeout) {
        this.store = store;
        this.obsoleteTimeout = obsoleteTimeout;
    }
    
    @Override
    public String getName() {
        return "Finding obsolete checksums";
    }

    /**
     * Goes through all the file ids in the database and extract their respective fileinfos.
     * Then it goes through all the file infos to validate that the timestamp for the checksum calculation.
     * TODO needs optimization for the extraction of file ids.
     */
    @Override
    public synchronized void performStep() {
        long currentTimeoutTime = System.currentTimeMillis() - obsoleteTimeout;
        for(String fileId : store.getAllFileIDs()) {
            // TODO: this needs optimization.
            for(FileInfo fileinfo : store.getFileInfos(fileId)) {
                if(fileinfo.getChecksumState() == ChecksumState.VALID && CalendarUtils.convertFromXMLGregorianCalendar(
                        fileinfo.getDateForLastChecksumCheck()).getTime() < currentTimeoutTime) {
                    log.warn("Checksum is obsolete for the file '" + fileId + "', at least at pillar '" 
                            + fileinfo.getPillarId() + "'.");
                    obsoleteChecksums.add(fileId);
                    break;
                }
            }
        }
    }

    /**
     * Creates and returns a list of the file ids where the checksums should be recollected.
     * @return The list of file ids which have obsolete checksums for some pillar(s).
     */
    public List<String> getResults() {
        return new ArrayList<String>(obsoleteChecksums);
    }
}
