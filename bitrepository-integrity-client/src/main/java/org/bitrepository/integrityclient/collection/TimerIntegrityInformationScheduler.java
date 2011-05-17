package org.bitrepository.integrityclient.collection;

import org.bitrepository.integrityclient.configuration.integrityclientconfiguration.CollectionConfiguration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Scheduler that uses Timer to trigger events.
 */
public class TimerIntegrityInformationScheduler implements IntegrityInformationScheduler {
    /** The timer that schedules events. */
    private final Timer timer;
    /** The period between testing whether triggers have triggered. */
    private final long interval;

    /** Setup a timer task for triggering all triggers at requested interval.
     *
     * @param configuration The configuration for the collection. Currently contains polling interval.
     */
    public TimerIntegrityInformationScheduler(CollectionConfiguration configuration) {
        this.interval = configuration.getPollingInterval();
        timer = new Timer("Integrity Information Scheduler", true);
    }

    @Override
    public void addTrigger(Trigger trigger) {
        TimerTask task = new TriggerTimerTask(trigger);
        // TODO: Should the interval rather be a suggestion from the trigger?
        // TODO: Should triggers be defined in configuration? How?
        timer.scheduleAtFixedRate(task, 0L, interval);
    }

    private static class TriggerTimerTask extends TimerTask {
        /** The trigger to test and run. */
        private Trigger trigger;

        /** Initialise a task that tests a trigger and runs it if it has triggered.
         *
         * @param trigger The trigger to test and run.
         */
        public TriggerTimerTask(Trigger trigger) {
            this.trigger = trigger;
        }

        @Override
        public void run() {
            if (trigger.isTriggered()) {
                trigger.trigger();
            }
        }
    }
}
