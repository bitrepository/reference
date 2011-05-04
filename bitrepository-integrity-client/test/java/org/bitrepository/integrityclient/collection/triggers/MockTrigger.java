package org.bitrepository.integrityclient.collection.triggers;

/**
 * A trigger that triggers every other second, and remembers calls.
 */
public class MockTrigger extends IntervalTrigger {
    /**
     * Get the number of times isTriggered() is called.
     * @return The number of times triggered() is called.
     */
    public int getIsTriggeredCalled() {
        return isTriggeredCalled;
    }

    /**
     * Get the number of times trigger() is called.
     * @return The number of times trigger() is called.
     */
    public int getTriggerCalled() {
        return triggerCalled;
    }

    /** Number of times isTriggered() is called. */
    private int isTriggeredCalled = 0;
    /** Number of times trigger() is called. */
    private int triggerCalled = 0;

    /**
     * Initialise trigger.
     */
    public MockTrigger() {
        super(2000L);
    }

    @Override
    public boolean isTriggered() {
        isTriggeredCalled++;
        return super.isTriggered();
    }

    @Override
    public void trigger() {
        triggerCalled++;
        super.trigger();
    }

    @Override
    public void run() {
        // Do nothing
    }
}
