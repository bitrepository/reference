package org.bitrepository.integrityclient.cache;

import org.bitrepository.bitrepositoryelements.ChecksumsData;
import org.bitrepository.bitrepositoryelements.FileIDsData;

/**
 * Store of cached integrity information.
 */
public interface CachedIntegrityInformationStorage {
    /**
     * Add file ID data to cache.
     * @param data The received data.
     */
    void addFileIDs(FileIDsData data);

    /**
     * Add checksum data to cache.
     * @param data The received data.
     */
    void addChecksums(ChecksumsData data);

    //TODO How to access the data
}
