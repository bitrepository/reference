package org.bitrepository.integrityclient.collection;

/**
 * Interface for scheduling integrity information collection.
 *
 * Implementations should apply all triggers at reasonable intervals.
 */
public interface IntegrityInformationScheduler {
    /** Add a trigger for initiating information collection.
     *
     * @param trigger The definition of whether a collection should run, and if so what collection.
     */
    void addTrigger(Trigger trigger);
}
