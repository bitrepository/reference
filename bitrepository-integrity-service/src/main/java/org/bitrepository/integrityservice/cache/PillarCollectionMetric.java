package org.bitrepository.integrityservice.cache;

/**
 * Class to carry information of collection specific pillar metrics.
 * The class exists as java is not able to handle simple tuples,
 * so the class is meant to carry data in a specific context.
 *
 * @see org.bitrepository.integrityservice.cache.database.IntegrityDAO#getPillarCollectionMetrics(String)
 */
public class PillarCollectionMetric {

    /**
     * The summed size of the files in a collection on the pillar.
     */
    private final long pillarCollectionSize;

    /**
     * The count of files present in a collection on a pillar
     */
    private final long pillarFileCount;

    public PillarCollectionMetric(Long pillarCollectionSize, Long pillarFileCount) {
        this.pillarCollectionSize = pillarCollectionSize == null ? 0 : pillarCollectionSize;
        this.pillarFileCount = pillarFileCount == null ? 0 : pillarFileCount;
    }

    public long getPillarCollectionSize() {
        return pillarCollectionSize;
    }

    public long getPillarFileCount() {
        return pillarFileCount;
    }

}
