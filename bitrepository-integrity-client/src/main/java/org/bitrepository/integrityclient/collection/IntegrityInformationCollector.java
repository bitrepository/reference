package org.bitrepository.integrityclient.collection;

import java.util.Collection;

/**
 * This is the interface for initiating collecting integrity information from pillars.
 *
 * It is expected to be called from a scheduler that generates events to collect specific data.
 * Results should be stored in the {@link org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage}
 */
public interface IntegrityInformationCollector {
    /**
     * Request all File IDs from the given pillar.
     *
     * @param slaID The SLA in question.
     * @param pillarID The pillar to request file IDs from. May be null, meaning all pillars.
     * @param fileIDs Only request info about the given fileIDs. May be mull, meaning all files.
     */
    void getFileIDs(String slaID, String pillarID, Collection<String> fileIDs);

    /**
     * Request the specified checksums from the given pillar.
     *
     * @param slaID The SLA in question.
     * @param pillarID The pillar to request checksums from. May be null, meaning all pillars.
     * @param fileIDs Only request info about the given fileIDs. May be mull, meaning all files.
     * @param checksumType The checksum algorithm used. May be null, in which case SHA1 is assumed.
     * @param salt Salt the checksum with the given bytes. May be null or empty, in which case the checksums aren't
     * salted.
     */
    void getChecksums(String slaID, String pillarID, Collection<String> fileIDs, String checksumType, byte[] salt);
}
