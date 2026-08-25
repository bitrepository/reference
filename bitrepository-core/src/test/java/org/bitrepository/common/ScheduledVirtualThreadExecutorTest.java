/*
 * #%L
 * Bitrepository Protocol
 *
 * $Id$
 * $HeadURL$
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
package org.bitrepository.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledVirtualThreadExecutorTest {
    private ScheduledVirtualThreadExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) {
            executor.close();
        }
    }

    @Test
    @Tag("regressiontest")
    void scheduleAtFixedRateRunsRepeatedlyTest() throws Exception {
        addDescription("Test that scheduleAtFixedRate dispatches the task repeatedly.");
        executor = new ScheduledVirtualThreadExecutor("fixedRateTest", true);
        CountDownLatch latch = new CountDownLatch(3);

        executor.scheduleAtFixedRate(latch::countDown, 0, 20, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Task should have run at least 3 times");
    }

    @Test
    @Tag("regressiontest")
    void scheduleWithFixedDelayRunsRepeatedlyTest() throws Exception {
        addDescription("Test that scheduleWithFixedDelay dispatches the task repeatedly.");
        executor = new ScheduledVirtualThreadExecutor("fixedDelayTest", true);
        CountDownLatch latch = new CountDownLatch(3);

        executor.scheduleWithFixedDelay(latch::countDown, 0, 20, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Task should have run at least 3 times");
    }

    @Test
    @Tag("regressiontest")
    void scheduleRunsOnlyOnceTest() throws Exception {
        addDescription("Test that a one-shot schedule() call dispatches the task exactly once.");
        executor = new ScheduledVirtualThreadExecutor("onceTest", true);
        AtomicInteger count = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);

        executor.schedule(() -> {
            count.incrementAndGet();
            latch.countDown();
        }, 0, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Task should have run once");
        // Give a would-be re-run every chance to happen before asserting it didn't.
        Thread.sleep(100);
        Assertions.assertEquals(1, count.get());
    }

    @Test
    @Tag("regressiontest")
    void cancellingFutureStopsFurtherRunsTest() throws Exception {
        addDescription("Test that cancelling the returned ScheduledFuture stops further dispatches of that task.");
        executor = new ScheduledVirtualThreadExecutor("cancelTest", true);
        AtomicInteger count = new AtomicInteger();
        CountDownLatch firstRun = new CountDownLatch(1);

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            count.incrementAndGet();
            firstRun.countDown();
        }, 0, 20, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(firstRun.await(5, TimeUnit.SECONDS), "Task should have run at least once");
        future.cancel(false);
        int countAtCancellation = count.get();
        Thread.sleep(150);

        Assertions.assertEquals(countAtCancellation, count.get(), "No further runs should have happened after cancellation");
    }

    @Test
    @Tag("regressiontest")
    void taskRunsOnVirtualThreadTest() throws Exception {
        addDescription("Test that dispatched tasks run on their own virtual thread, not the ticker thread.");
        executor = new ScheduledVirtualThreadExecutor("virtualThreadTest", true);
        AtomicBoolean isVirtual = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        executor.schedule(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            latch.countDown();
        }, 0, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS), "Task should have run");
        Assertions.assertTrue(isVirtual.get(), "Dispatched task should run on a virtual thread");
    }

    @Test
    @Tag("regressiontest")
    void exceptionInTaskDoesNotStopFutureRunsTest() throws Exception {
        addDescription("Test that an uncaught exception in a dispatched task doesn't stop subsequent scheduled runs, " +
                "unlike java.util.Timer which silently kills the whole timer thread on the first uncaught exception.");
        executor = new ScheduledVirtualThreadExecutor("exceptionTest", true);
        CountDownLatch latch = new CountDownLatch(3);

        executor.scheduleAtFixedRate(() -> {
            latch.countDown();
            throw new RuntimeException("Deliberate failure from test");
        }, 0, 20, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS),
                "Task should keep running despite throwing every time");
    }

    @Test
    @Tag("regressiontest")
    void exceptionInTaskIsLoggedTest() throws Exception {
        addDescription("Test that an uncaught exception in a dispatched task is logged instead of vanishing silently.");
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        rootLogger.addAppender(mockAppender);

        try (AutoCloseable ignored = () -> rootLogger.detachAppender(mockAppender)) {
            executor = new ScheduledVirtualThreadExecutor("loggingTest", true);
            String failureMessage = "Deliberate failure for logging test";

            executor.schedule(() -> {
                throw new RuntimeException(failureMessage);
            }, 0, TimeUnit.MILLISECONDS);

            ArgumentCaptor<ILoggingEvent> argument = ArgumentCaptor.forClass(ILoggingEvent.class);
            verify(mockAppender, timeout(5000)).doAppend(argument.capture());
            ILoggingEvent logLine = argument.getValue();

            Assertions.assertEquals(Level.ERROR, logLine.getLevel());
            Assertions.assertEquals(ScheduledVirtualThreadExecutorTest.class.getName(), logLine.getLoggerName());
            Assertions.assertEquals(failureMessage, logLine.getThrowableProxy().getMessage());
        }
    }

    @Test
    @Tag("regressiontest")
    void closeStopsFurtherDispatchTest() throws Exception {
        addDescription("Test that close() stops the ticker so no further dispatches occur.");
        executor = new ScheduledVirtualThreadExecutor("closeTest", true);
        AtomicInteger count = new AtomicInteger();
        CountDownLatch firstRun = new CountDownLatch(1);

        executor.scheduleAtFixedRate(() -> {
            count.incrementAndGet();
            firstRun.countDown();
        }, 0, 20, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(firstRun.await(5, TimeUnit.SECONDS), "Task should have run at least once");
        executor.close();
        int countAtClose = count.get();
        Thread.sleep(150);

        Assertions.assertEquals(countAtClose, count.get(), "No further runs should have happened after close()");
    }
}
