package org.bitrepository.integrityclient.cache;

import org.bitrepository.bitrepositoryelements.ChecksumsData;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.integrityclient.configuration.integrityclientconfiguration.StorageConfiguration;

/**
 * A storage of configuration information that is backed by a database.
 */
public class DatabaseBackedCachedIntegrityInformationStorage implements CachedIntegrityInformationStorage {
    /**
     * Initialise storage.
     *
     * @param storageConfiguration Contains configuration for storage. Currently URL, user and pass for database.
     */
    public DatabaseBackedCachedIntegrityInformationStorage(StorageConfiguration storageConfiguration) {
    }

    @Override
    public void addFileIDs(FileIDsData data) {
        // TODO implement
    }

    @Override
    public void addChecksums(ChecksumsData data) {
        // TODO implement
    }
}
