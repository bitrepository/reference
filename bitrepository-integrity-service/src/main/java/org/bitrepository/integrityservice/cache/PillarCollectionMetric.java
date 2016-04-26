package org.bitrepository.integrityservice.cache;

/**
 * Class to carry information of collection specific pillar metrics.
 * The class exists as java is not able to handle simple tuples, 
 * so the class is meant to carry data in a specific context. 
 */
public class PillarCollectionMetric {

    /**
     * The summed size of the files in a collection on the pillar. 
     */
    private long pillarCollectionSize;
    
    /**
     * The count of files present in a collection on a pillar
     */
    private long pillarFileCount;
    
    public long getPillarCollectionSize() {
        return pillarCollectionSize;
    }
    
    public void setPillarCollectionSize(long pillarCollectionSize) {
        this.pillarCollectionSize = pillarCollectionSize;
    }
    
    public long getPillarFileCount() {
        return pillarFileCount;
    }
    
    public void setPillarFileCount(long pillarFileCount) {
        this.pillarFileCount = pillarFileCount;
    }
}
