package org.bitrepository.integrityservice.scheduler.workflow.step;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * 
 * It goes through every file id available, extracts all the fileinfos and validates whether any pillars
 * are missing the given checksum (e.g. whether the ChecksumState is Unknown and the FileState is not Missing).
 * This is a very simple and definitely not optimized way of finding missing checksums.
 * 
 * TODO make a method in the database for extracting all the ids of all the files which have the checksum state 
 * Unknown for any pillar. 
 */
public class FindMissingChecksumsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The mapping between */
    private List<String> missingChecksums = new ArrayList<String>();
    
    /**
     * Constructor.
     * @param store The storage for the integrity data.
     */
    public FindMissingChecksumsStep(IntegrityModel store) {
        this.store = store;
    }
    
    @Override
    public String getName() {
        return "Finding missing checksums";
    }

    /**
     * Goes through all the file ids in the database and extract their respective fileinfos.
     * Then it goes through all the file infos to validate that the file at no pillar exists but has an unknown state 
     * for the checksum.
     * TODO needs optimization for the extraction of file ids.
     */
    @Override
    public synchronized void performStep() {
        for(String fileId : store.getAllFileIDs()) {
            // TODO: this needs optimization.
            for(FileInfo fileinfo : store.getFileInfos(fileId)) {
                if(fileinfo.getFileState() == FileState.EXISTING &&
                        fileinfo.getChecksumState() == ChecksumState.UNKNOWN) {
                    log.warn("Checksum is missing for the file '" + fileId + "', at least at pillar '" 
                            + fileinfo.getPillarId() + "'.");
                    missingChecksums.add(fileId);
                    break;
                }
            }
        }
    }

    /**
     * Creates and returns a list of the file ids where the checksums should be recollected.
     * @return The list of file ids which is missing the checksum for some pillar.
     */
    public List<String> getResults() {
        return new ArrayList<String>(missingChecksums);
    }
}
