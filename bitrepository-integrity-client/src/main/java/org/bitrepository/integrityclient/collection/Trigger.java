package org.bitrepository.integrityclient.collection;

/**
 * Interface for defining a trigger for collecting events.
 *
 * Implementations should trigger collection of integrity information using an
 * {@link IntegrityInformationCollector}.
 *
 * Conditions for the trigger should pull on configuration combined with data from the
 * {@link org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage}.
 */
public interface Trigger {
    /**
     * Whether the trigger triggers an event. Note, this is informational only, due to race conditions,
     * no event may be available when actually requesting the trigger.
     * This method will should be implemented to be fairly fast, and not require too many resources,
     * since it can be called quite frequently from the scheduler.
     *
     * @return True if this triggers an event. False otherwise.
     */
    boolean isTriggered();

    /**
     * Trigger a collection event.
     *
     * This should trigger the collection of information using an {@link IntegrityInformationCollector}.
     *
     * May do nothing, if triggering conditions do not apply.
     */
    void trigger();
}
