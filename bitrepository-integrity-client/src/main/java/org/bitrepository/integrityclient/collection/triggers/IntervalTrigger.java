package org.bitrepository.integrityclient.collection.triggers;

import org.bitrepository.integrityclient.collection.Trigger;

/**
 * Abstract trigger, that triggers at given interval.
 * Trigger will run the run() method, if triggered.
 */
public abstract class IntervalTrigger implements Trigger, Runnable {
    /** Time of last event, truncated to two seconds. */
    private long time; //no see
    /** The interval between triggers. */
    private final long interval;

    /**
     * Initialise trigger.
     * @param interval The interval between triggering events in milliseconds.
     */
    public IntervalTrigger(long interval) {
        long now = System.currentTimeMillis();
        this.interval = interval;
        time = now - (now % this.interval);
    }

    @Override
    public synchronized boolean isTriggered() {
        return ((System.currentTimeMillis() - time) > interval);
    }

    @Override
    public void trigger() {
        synchronized (this) {
            if (!isTriggered()) {
                return;
            }
            long now = System.currentTimeMillis();
            time = now - (now % interval);
        }
        run();
    }
}
