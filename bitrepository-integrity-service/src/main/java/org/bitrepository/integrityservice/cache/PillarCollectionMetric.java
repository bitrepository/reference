package org.bitrepository.integrityservice.cache;

import java.time.Instant;

/**
 * Carries collection-specific pillar metrics.
 *
 * @param pillarCollectionSize    The summed size of the files in a collection on the pillar.
 * @param pillarFileCount         The count of files present in a collection on a pillar.
 * @param oldestChecksumTimestamp Timestamp of the oldest checksum on the pillar, or null if no checksums yet.
 * @see org.bitrepository.integrityservice.cache.database.IntegrityDAO#getPillarCollectionMetrics(String)
 */
public record PillarCollectionMetric(long pillarCollectionSize, long pillarFileCount, Instant oldestChecksumTimestamp) {

    /** Convenience constructor that treats null sizes/counts as zero. */
    public PillarCollectionMetric(Long pillarCollectionSize, Long pillarFileCount, Instant oldestChecksumTimestamp) {
        this(
            pillarCollectionSize == null ? 0L : pillarCollectionSize,
            pillarFileCount == null ? 0L : pillarFileCount,
            oldestChecksumTimestamp
        );
    }

    /**
     * @deprecated Use {@link #pillarCollectionSize()} instead
     */
    @Deprecated(forRemoval = true)
    public long getPillarCollectionSize() {
        return pillarCollectionSize;
    }

    /**
     * @deprecated Use {@link #pillarFileCount()} instead
     */
    @Deprecated(forRemoval = true)
    public long getPillarFileCount() {
        return pillarFileCount;
    }

    /**
     * @deprecated Use {@link #oldestChecksumTimestamp()} instead
     */
    @Deprecated(forRemoval = true)
    public Instant getOldestChecksumTimestamp() {
        return oldestChecksumTimestamp;
    }
}
