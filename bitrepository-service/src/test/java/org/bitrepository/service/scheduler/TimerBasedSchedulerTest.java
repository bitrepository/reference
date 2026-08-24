/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2026 The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.service.scheduler;

import org.bitrepository.TestGroups;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.JobTimerTask;
import org.bitrepository.service.workflow.SchedulableJob;
import org.bitrepository.service.workflow.WorkflowContext;
import org.bitrepository.service.workflow.WorkflowState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;

class TimerBasedSchedulerTest {
    private TimerBasedScheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void scheduleWithPositiveIntervalRunsJobPromptlyTest() throws Exception {
        addDescription("Test that schedule() with a positive interval dispatches the job right away.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));

        scheduler.schedule(job, 1000L);

        Assertions.assertTrue(job.started.await(5, TimeUnit.SECONDS), "Job should have been started");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void scheduleWithNonPositiveIntervalRegistersButDoesNotRunTheJobTest() throws Exception {
        addDescription("Test that schedule() with a non-positive interval registers the job without triggering a run, " +
                "per the interval==never contract documented on JobScheduler#schedule.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));

        scheduler.schedule(job, 0L);

        Assertions.assertFalse(job.started.await(200, TimeUnit.MILLISECONDS), "Job should not have been started");
        Assertions.assertEquals(0, scheduler.getRunInterval(job.getJobID()), "The job should still be registered");
        Assertions.assertNull(scheduler.getNextRunInstant(job.getJobID()),
                "A job with a non-positive interval has no next run");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void cancelJobReturnsScheduledTaskAndStopsItTest() throws Exception {
        addDescription("Test that cancelJob() returns the scheduled task and removes it from the scheduler's bookkeeping.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));
        scheduler.schedule(job, 1000L);
        Assertions.assertTrue(job.started.await(5, TimeUnit.SECONDS), "Job should have been started");

        JobTimerTask cancelled = scheduler.cancelJob(job.getJobID());

        Assertions.assertNotNull(cancelled);
        Assertions.assertEquals(job.getJobID(), cancelled.getWorkflowID());
        Assertions.assertEquals(-1, scheduler.getRunInterval(job.getJobID()),
                "A cancelled job is no longer known to the scheduler");
        Assertions.assertNull(scheduler.cancelJob(job.getJobID()), "Cancelling an unknown job returns null");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void startJobStartsAnIdleJobImmediatelyTest() throws Exception {
        addDescription("Test that startJob() runs a job that isn't currently running right away.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));

        String result = scheduler.startJob(job);

        Assertions.assertEquals("Job scheduled", result);
        Assertions.assertTrue(job.started.await(5, TimeUnit.SECONDS), "Job should have been started");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void startJobRefusesAJobThatIsAlreadyRunningTest() {
        addDescription("Test that startJob() doesn't run a job that's already in a non-idle state.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));
        job.setCurrentState(WorkflowState.RUNNING);

        String result = scheduler.startJob(job);

        Assertions.assertEquals("Already running", result);
        Assertions.assertEquals(0, job.startCount.get(), "The already-running job should not have been started again");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void jobEventListenerIsNotifiedWhenJobFinishesTest() throws Exception {
        addDescription("Test that a registered JobEventListener is notified once the dispatched job finishes.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));
        CountDownLatch notified = new CountDownLatch(1);
        scheduler.addJobEventListener(new JobEventListener() {
            @Override
            public void jobStarted(SchedulableJob j) {
            }

            @Override
            public void jobFailed(SchedulableJob j) {
            }

            @Override
            public void jobFinished(SchedulableJob j) {
                if (j.getJobID().equals(job.getJobID())) {
                    notified.countDown();
                }
            }
        });

        scheduler.schedule(job, 1000L);

        Assertions.assertTrue(notified.await(5, TimeUnit.SECONDS), "Listener should have been notified");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getRunIntervalAndNextRunAreUnknownForAnUnscheduledJobTest() {
        addDescription("Test that querying a job that was never scheduled returns the documented \"unknown\" values.");
        scheduler = new TimerBasedScheduler();
        JobID unknownJob = new JobID("neverScheduled", "collection");

        Assertions.assertEquals(-1, scheduler.getRunInterval(unknownJob));
        Assertions.assertNull(scheduler.getNextRunInstant(unknownJob));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void shutdownStopsTheSchedulerTest() {
        addDescription("Test that shutdown() releases the scheduler's resources without throwing.");
        scheduler = new TimerBasedScheduler();
        FakeJob job = new FakeJob(new JobID("workflow", "collection"));
        scheduler.schedule(job, 1000L);

        Assertions.assertDoesNotThrow(() -> scheduler.shutdown());
    }

    private static class FakeJob implements SchedulableJob {
        private final JobID id;
        private final CountDownLatch started = new CountDownLatch(1);
        private final AtomicInteger startCount = new AtomicInteger();
        private volatile WorkflowState state = WorkflowState.NOT_RUNNING;

        FakeJob(JobID id) {
            this.id = id;
        }

        @Override
        public void start() {
            startCount.incrementAndGet();
            // Simulate a job that finishes synchronously, so repeated scheduling isn't blocked by its state.
            state = WorkflowState.NOT_RUNNING;
            started.countDown();
        }

        @Override
        public WorkflowState currentState() {
            return state;
        }

        @Override
        public void setCurrentState(WorkflowState newState) {
            state = newState;
        }

        @Override
        public String getHumanReadableState() {
            return state.toString();
        }

        @Override
        public String getDescription() {
            return "FakeJob for " + id;
        }

        @Override
        public JobID getJobID() {
            return id;
        }

        @Override
        public void initialise(WorkflowContext context, String collectionID) {
        }
    }
}
