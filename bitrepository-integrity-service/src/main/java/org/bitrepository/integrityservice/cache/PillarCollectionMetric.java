package org.bitrepository.integrityservice.cache;

import java.time.Instant;

/**
 * Carries collection-specific pillar metrics.
 *
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
}
